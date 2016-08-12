package com.coodays.pushservicelib.push;

import android.content.Context;
import android.text.TextUtils;
import com.coodays.pushservicelib.bean.CdNeteaseRegisterBean;
import com.coodays.pushservicelib.components.modules.CdAppModule;
import com.coodays.pushservicelib.network.CdIHttpApiService;
import com.coodays.pushservicelib.push.netease.CheckSumBuilder;
import com.coodays.pushservicelib.utils.CdJsonUtils;
import com.coodays.pushservicelib.utils.CdLogUtils;
import com.coodays.pushservicelib.utils.CdSharedPreferencesUtils;
import com.coodays.pushservicelib.utils.CdTools;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author zhuj
 *         云信推送
 *         用app_user_id当做云信账号， 云信账号就是 token
 *         登入成功后，会广播发送 token（也就是云信账号）
 *         包括登入, 注册, 上传token, 接收推送消息等
 *
 *
 */
class NeteasePush extends BasePush {
  private String APP_KEY = "";
  private String APP_SECRET = "";
  private String APP_Nonce = "";
  private final static String TAG = NeteasePush.class.getSimpleName();

  /**
   * 最后解析的一条数据， 有时候会一次推送收到两条，或多条
   * 解析过的不再重复解析
   */
  private IMMessage mLastMessage;
  private String mAccout;//云信注册账号
  private String mPassword;//云信账号密码
  private AbortableFuture<LoginInfo> loginRequest;

  private boolean isRegister;
  private boolean isUpdate;

  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private NeteasePush(Context context) {
    super(context);
  }

  private static volatile BasePush mBashPush;

  public static BasePush getInstance(Context context) {
    if (mBashPush == null) {
      synchronized (NeteasePush.class) {
        if (mBashPush == null) {
          mBashPush = new NeteasePush(context);
        }
      }
    }
    return mBashPush;
  }

  @Override public void init() {
    NIMClient.init(mContext, loginInfo(), null);
    addMessageCallback();
  }

  /**
   * @param appUserId 账号 或 token
   */
  @Override public void login(String appUserId, String password) {
    if (appUserId == null || appUserId.equals("")) {
      CdLogUtils.e(TAG, " push login account is empty " + appUserId);
      return;
    }
    LoginInfo info = loginInfo();
    if (info != null && info.getAccount().equals(appUserId)) {//云信对已经登入成功的账号 有自动登入,保活机制
      CdLogUtils.v(TAG, " push loginInfo !=null , don't need login " + appUserId + " " + getStatus());
      return;
    }
    NIMClient.getService(MsgService.class).clearChattingHistory(appUserId, SessionTypeEnum.None);
    CdSharedPreferencesUtils.put(mContext, CdSharedPreferencesUtils.KEY_APP_USER_ID, appUserId);
    CdSharedPreferencesUtils.savaToken(mContext, "" + PushManager.PHONE_TYPE_DEFAULT, appUserId);

    //下面是第一次登入
    info = new LoginInfo(appUserId, password); // config...
    CdLogUtils.d(TAG, " netease push login() " + appUserId + " current status:" + getStatus());
    this.mAccout = appUserId;
    this.mPassword = password;
    if (!isLogin()) {
      //针对 正在连接中的情况，先退出再登入
      if (getStatus().equals(StatusCode.LOGINING.toString()) || getStatus().equals(
          StatusCode.CONNECTING.toString())) {
        //NIMClient.getService(AuthService.class).logout();
        CdLogUtils.e(TAG, " push login islogining " + appUserId);
        if (loginRequest==null) {
          loginRequest = NIMClient.getService(AuthService.class).login(info);
        }
        loginRequest.setCallback(callback);
      } else {
        loginRequest = NIMClient.getService(AuthService.class).login(info);
        loginRequest.setCallback(callback);
        NIMClient.toggleNotification(true);
        CdLogUtils.d(TAG, " push login dologin " + appUserId);
      }
    } else {
      //AbortableFuture<LoginInfo> loginRequest = NIMClient.getService(AuthService.class).login(info);
      if (loginRequest != null) {
        loginRequest.setCallback(callback);
      }
    }
  }

  @Override public void loginOut() {
    isRegister =false;
    isUpdate = false;
    NIMClient.getService(AuthService.class).logout();
    removeMessageCallback();
    CdSharedPreferencesUtils.remove(mContext, "account");
    loginOutClean();
  }

  @Override public String getStatus() {
    return NIMClient.getStatus().toString();
  }

  @Override public boolean isLogin() {
    return NIMClient.getStatus().equals(StatusCode.LOGINED) || NIMClient.getStatus()
        .equals(StatusCode.SYNCING);
  }

  private void addMessageCallback() {
    if (CdTools.isMainProcess(mContext)) {
      LoginInfo loginInfo = loginInfo();
      if (loginInfo != null) {
        //这句话只能添加到主线程里
        NIMClient.getService(MsgServiceObserve.class)
            .observeReceiveMessage(incomingMessageObserver, true);
        NIMClient.getService(AuthServiceObserver.class)
            .observeOnlineStatus(new Observer<StatusCode>() {
              public void onEvent(StatusCode status) {
                CdLogUtils.v(TAG, "Netease push User status changed to: " + status);
                //因为现在同时 启用三套推送，
                // 用了云信本身 踢下线机制的话， 其他通道还是会收到 服务器发送的下线推送 消息，（相当于两次下线）
                // 这里就先去除云信自带的(这里没发送推送消息， 也就不会弹通知栏） ， 只用我们服务器的
                //if (status.equals(StatusCode.KICKOUT)) {
                //  Intent intent = new Intent(PushManager.ACTION_PUSH_KICK);
                //  mContext.sendBroadcast(intent);
                //}
              }
            }, true);
      }
    }
  }

  private void removeMessageCallback() {
    NIMClient.getService(MsgServiceObserve.class)
        .observeReceiveMessage(incomingMessageObserver, false);
  }

  // 如果已经存在用户登录信息，返回LoginInfo，否则返回null即可
  private LoginInfo loginInfo() {
    String accout = (String) CdSharedPreferencesUtils.get(mContext, "account", "");
    String password = (String) CdSharedPreferencesUtils.get(mContext, "password", "");
    if (accout != null && !accout.equals("")) {
      return new LoginInfo(accout, password);
    }
    return null;
  }

  /**
   * 注册云信账号,
   * 注册要后台服务器去注册， 这里暂时保留 ，兼容以前使用微守护的appkey转 51xiu appkey 过度
   */
  private void register(final String account, final String password) {
    if (account == null || account.equals("")) {
      CdLogUtils.e(TAG, "push register fail , account is empty " + account);
      return;
    }
    getSDKInfo();
    String curTime = String.valueOf((new Date()).getTime() / 1000L);
    String checkSum = CheckSumBuilder.getCheckSum(APP_SECRET, APP_Nonce, curTime);//参考 计算CheckSum的java代码

    final CdIHttpApiService httpApiService = CdAppModule.getInstantce(mContext).provideAuthenticationService();
    Observable<CdNeteaseRegisterBean> observable = httpApiService.neteaseRegister(APP_KEY, APP_Nonce, curTime, checkSum, account, password);
    observable.subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .unsubscribeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<CdNeteaseRegisterBean>() {
          @Override public void onCompleted() {
            CdLogUtils.v("test", " onCompleted ");
          }

          @Override public void onError(Throwable e) {
            if (e != null) {
              CdLogUtils.v(TAG, " onError " + e.getMessage());
            }
          }

          @Override public void onNext(CdNeteaseRegisterBean s) {
            CdLogUtils.d(TAG, "register  push token " + s.getCode() + " " + s.getDesc() );
            isRegister = true;
            if (s.getCode() == 200) {
              if (!isRegister) {
                login(account, password);
              }
              //注册成功， 上传token到服务器, token就用的 云信id 即app_user_id
            } else if (s.getCode() == 414) {
              if (s.getDesc()!=null && s.getDesc().equals("already register")) {//414参数错误，中有重复注册"))
                login(account, password);
              }
            }
          }
        });
  }

  private void updateToken(final String account, final String password) {
    if (account == null || account.equals("")) {
      CdLogUtils.e(TAG, "push register fail , account is empty " + account);
      return;
    }
    getSDKInfo();
    String curTime = String.valueOf((new Date()).getTime() / 1000L);
    String checkSum = CheckSumBuilder.getCheckSum(APP_SECRET, APP_Nonce, curTime);//参考 计算CheckSum的java代码

    final CdIHttpApiService httpApiService = CdAppModule.getInstantce(mContext).provideAuthenticationService();
    Observable<CdNeteaseRegisterBean> observable = httpApiService.neteaseUpdateToken(APP_KEY, APP_Nonce, curTime, checkSum, account, password);
    observable.subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .unsubscribeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<CdNeteaseRegisterBean>() {
          @Override public void onCompleted() {
            CdLogUtils.v("TAG", " onCompleted ");
          }

          @Override public void onError(Throwable e) {
            if (e != null) {
              CdLogUtils.v(TAG, " onError " + e.getMessage());
            }
          }

          @Override public void onNext(CdNeteaseRegisterBean s) {
            CdLogUtils.d(TAG, "update token " + s.getCode() + s.getDesc() + " pass" + password);
            isUpdate = true;
            if (s.getCode() == 200) {
              login(account, password);
            } else if (s.getCode() == 414) {
              if (s.getDesc().contains("not register")) {
                register(account, password);
              }
            }
          }
        });
  }

  /**
   * 登入成功的回调
   */
  private final RequestCallback<LoginInfo> callback = new RequestCallback<LoginInfo>() {
    @Override public void onSuccess(LoginInfo loginInfo) {
      CdLogUtils.d(TAG, "push login Success");
      //loginSuccess();
      // 初始化消息提醒
      NIMClient.toggleNotification(true);
      //广播发送token值， 云信账号 就是token
      CdSharedPreferencesUtils.put(mContext, "account", loginInfo.getAccount());
      CdSharedPreferencesUtils.put(mContext, "password", loginInfo.getToken());
      PushManager.sendBroadcastToken(mContext);
      init();
    }

    @Override public void onFailed(int i) {
      //如果达到最大测试免费账号100个，也会返回302 ，状态是密码错误, 没注册也是302
      CdLogUtils.d(TAG, "push login fail, errorCode: " + i);
      //注册账号
      if (i == 302 || i == 404) {//302 用户名或密码错误(有可能是没注册）, 404对象不存在
        if (!isRegister) {  //试试注册，注册成功，继续登入
          register(mAccout, mPassword);
        } else if (!isUpdate) { //注册过了， 还是302，就试试修改密码
          updateToken(mAccout, mPassword);
        }
      } else if (i == 415 || i == 408) {//客户端网络, 408 超时
        NIMClient.getService(AuthService.class).logout();
        login(mAccout, mPassword);
      } else {
        login(mAccout, mPassword);
      }
    }

    @Override public void onException(Throwable throwable) {
      CdLogUtils.d(TAG, " login exception " + throwable.getMessage());
    }
    // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用
  };

  // 更新消息提醒设置
  //NIMClient.updateStatusBarNotificationConfig(config);
  private final Observer<List<IMMessage>> incomingMessageObserver = new Observer<List<IMMessage>>() {
    @Override public void onEvent(List<IMMessage> messages) {
      // 处理新收到的消息，为了上传处理方便，SDK 保证参数 messages 全部来自同一个聊天对象。
      IMMessage message;
      for (int i = 0; i < messages.size(); i++) {
        message = messages.get(i);
        processMessage(message);
      }
    }
  };

  private void getSDKInfo() {
    if (TextUtils.isEmpty(APP_KEY)) {
      APP_KEY = CdTools.getMetaDataValue(mContext, "com.netease.nim.appKey");
      APP_SECRET = CdTools.getMetaDataValue(mContext, "com.netease.nim.appSecret");
      APP_Nonce = CdTools.getMetaDataValue(mContext, "com.netease.nim.appNonce");
    }
  }

  /**
   * 处理消息
   */
  private synchronized void processMessage(IMMessage message) {
    if (mLastMessage != null) {
      if (message.getUuid().equals(mLastMessage.getUuid())) {
        return;
      }
    }
    mLastMessage = message;
    CdLogUtils.v(TAG, "接收消息receiver: "
        + message.getFromAccount()
        + message.getContent()
        + " "
        + message.getPushContent()
        + " "
        + message.getPushPayload() + " time:" + sdf.format(new Date(message.getTime())));
    //if (message.getFromAccount().equals(DEFAULT_SERVER_ACCOUNT)) {//后台的推送账号
    //用pushPayload 来做消息推送体， 最大字节2k
    String pushContet = CdJsonUtils.hashMapToJson((HashMap) message.getPushPayload());
    CdLogUtils.dWriteLog(TAG, pushContet, "推送log");
    parseMessage(pushContet);
    //}
  }

}
