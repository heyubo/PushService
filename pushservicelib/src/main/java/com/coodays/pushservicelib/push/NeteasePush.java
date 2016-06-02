package com.coodays.pushservicelib.push;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import com.coodays.pushservicelib.bean.NetResult;
import com.coodays.pushservicelib.bean.NeteaseRegisterBean;
import com.coodays.pushservicelib.components.modules.AppModule;
import com.coodays.pushservicelib.network.IHttpApiService;
import com.coodays.pushservicelib.push.netease.CheckSumBuilder;
import com.coodays.pushservicelib.utils.JsonUtils;
import com.coodays.pushservicelib.utils.LogUtils;
import com.coodays.pushservicelib.utils.SharedPreferencesUtils;
import com.coodays.pushservicelib.utils.Tools;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author zhuj
 * @time 2016/3/30 16:32
 * 云信推送
 * 用app_user_id当做云信账号
 * 包括登入, 注册, 上传token, 接收推送消息等
 */
class NeteasePush extends BasePush {

  private final static String TAG = NeteasePush.class.getSimpleName();

  /**
   * 最后解析的一条数据， 有时候会一次推送收到两条，或多条
   * 解析过的不再重复解析
   */
  private IMMessage mLastMessage;
  private String mAccout;//云信注册账号
  private AbortableFuture<LoginInfo> loginRequest;

  NeteasePush(Context context) {
    super(context);
  }

  @Override
  public void init() {
    NIMClient.init(mContext, loginInfo(), null);
    addMessageCallback();
  }
  /**
   * @param appUserId  账号 或 token
   */
  @Override public void login( String appUserId, String updateTokenUrl ) {
    if (appUserId == null || appUserId.equals("")) {
      LogUtils.e(TAG, " push login account is empty " + appUserId);
      return;
    }
    LoginInfo info = loginInfo();
    if( info != null && info.getAccount().equals(appUserId)) {//云信对已经登入成功的账号 有自动登入,保活机制
      LogUtils.v(TAG, " push loginInfo !=null , don't need login " + appUserId + " "+getStatus());
      return;
    }
    SharedPreferencesUtils.put(mContext, SharedPreferencesUtils.KEY_TOKEN_UPLOAD_URL , updateTokenUrl);
    //下面是第一次登入
    info = new LoginInfo(appUserId, DEFAULT_PASSWORD); // config...
    LogUtils.d(TAG, " netease push login() " + appUserId + " current status:" + getStatus());
    this.mAccout = appUserId;
    if (!isLogin()) {
      //针对 正在连接中的情况，先退出再登入
      if (getStatus().equals(StatusCode.LOGINING.toString()) || getStatus().equals(StatusCode.CONNECTING.toString())) {
        //NIMClient.getService(AuthService.class).logout();
        LogUtils.e(TAG, " push login isloinging " + appUserId);
      } else {
        loginRequest = NIMClient.getService(AuthService.class).login(info);
        loginRequest.setCallback(callback);
        NIMClient.toggleNotification(true);
        LogUtils.d(TAG, " push login dologin " + appUserId);
      }
    }
    else {
      //AbortableFuture<LoginInfo> loginRequest = NIMClient.getService(AuthService.class).login(info);
      if(loginRequest!=null) {
        loginRequest.setCallback(callback);
      }
    }
  }

  @Override
  public void loginOut() {
    NIMClient.getService(AuthService.class).logout();
    removeMessageCallback();
    loginOutClean();
  }

  @Override
  public String getStatus() {
    return NIMClient.getStatus().toString();
  }

  @Override
  public boolean isLogin() {
    return NIMClient.getStatus().equals(StatusCode.LOGINED) || NIMClient.getStatus().equals(StatusCode.SYNCING);
  }

  @Override public void uploadToken(String app_user_id, String token) {
    startUploadToken( app_user_id, token, "" + PushManager.PHONE_TYPE_DEFAULT);
  }

  public void addMessageCallback() {
    if(Tools.isMainProcess(mContext)) {
      LoginInfo loginInfo = loginInfo();
      if(loginInfo!=null) {
        //这句话只能添加到主线程里
        NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(incomingMessageObserver, true);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(
            new Observer<StatusCode> () {
              public void onEvent(StatusCode status) {
                LogUtils.v(TAG, "Netease push User status changed to: " + status);
                parseMessageDebug("云信登入状态:"+status.toString());
              }
            }, true);
        uploadToken(loginInfo.getAccount(), loginInfo.getAccount());
      }
    }
  }

  public void removeMessageCallback() {
    NIMClient.getService(MsgServiceObserve.class).observeReceiveMessage(incomingMessageObserver, false);
  }

  // 如果已经存在用户登录信息，返回LoginInfo，否则返回null即可
  private LoginInfo loginInfo() {
    String accout = (String) SharedPreferencesUtils.get(mContext, SharedPreferencesUtils.KEY_TOKEN, "");
    if (accout != null && !accout.equals("")) {
      return new LoginInfo(accout, DEFAULT_PASSWORD);
    }
    return null;
  }

  /**
   * 注册云信账号
   * @param account
   * @param password
   */
  private void register(final String account, final String password) {
    if (account == null || account.equals("")) {
      LogUtils.e(TAG, "push register fail , account is empty " + account);
      return;
    }
    //String appKey = "aca00f3be88dfe5de5d00fda44fc0113";
    String appSecret = "e07267dcf7d5";
    String nonce = "12345";
    String curTime = String.valueOf((new Date()).getTime() / 1000L);
    String checkSum = CheckSumBuilder.getCheckSum(appSecret, nonce, curTime);//参考 计算CheckSum的java代码

    final IHttpApiService httpApiService = AppModule.getInstantce(mContext).provideAuthenticationService();
    Observable<NeteaseRegisterBean> observable = httpApiService.neteaseRegister(curTime,  checkSum, account, password);
    observable.subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .unsubscribeOn(AndroidSchedulers.mainThread())
        .flatMap(new Func1<NeteaseRegisterBean, Observable<NetResult>>() {
          @Override public Observable<NetResult> call(NeteaseRegisterBean s) {
            parseMessageDebug("云信注册:"+s.getCode());
            String updateTokenUrl = (String) SharedPreferencesUtils.get(mContext, SharedPreferencesUtils.KEY_TOKEN_UPLOAD_URL, "");
            if(s.getCode()==200) {
              login(account, updateTokenUrl);
              //注册成功， 上传token到服务器, token就用的 云信id 即app_user_id
              ArrayMap<String, String> data = new ArrayMap<>();
              data.put("app_user_id", account);
              data.put("token", account);
              data.put("phone_brand", ""+PushManager.PHONE_TYPE_DEFAULT);
              String params = JsonUtils.arrayMapToJson(data);
              return httpApiService.setToken(updateTokenUrl,params);
            } else if(s.getCode()== 414) { //此部分应该是处理密码错误，
              if(s.getDesc().equals("already register")) {//414参数错误，中有重复注册"))
                login(account, updateTokenUrl);
              }
            }
            return null;
          }
        }).subscribe(new Subscriber<NetResult>() {
          @Override public void onCompleted() {
            LogUtils.v("test", " onCompleted " );
          }

          @Override public void onError(Throwable e) {
            if(e!=null) {
              LogUtils.v(TAG, " onError " +e.getMessage());
            }
            uploadToken(account, account);
          }

          @Override public void onNext(NetResult s) {
            LogUtils.d(TAG, "upload push token " + s.getResult().getCode());

            parseMessageDebug("云信上传token:"+s.toString());
            if(s.isNetResultCodeOk()) {
              SharedPreferencesUtils.put(mContext, account, account);
            } else{
              onError(null);
            }
          }
        });
  }

  /**
   * 登入成功的回调
   */
  RequestCallback<LoginInfo> callback = new RequestCallback<LoginInfo>() {
    @Override
    public void onSuccess(LoginInfo loginInfo) {
      LogUtils.d(TAG, "push login Success");
      //loginSuccess();
      // 初始化消息提醒
      NIMClient.toggleNotification(true);
      SharedPreferencesUtils.put(mContext, SharedPreferencesUtils.KEY_TOKEN, loginInfo.getAccount());
      init();
    }

    @Override
    public void onFailed(int i) {
      //如果达到最大测试免费账号100个，也会返回302 ，状态是密码错误, 没注册也是302
      LogUtils.d(TAG, "push login fail, errorCode: " + i);
      String updateTokenUrl = (String) SharedPreferencesUtils.get(mContext, SharedPreferencesUtils.KEY_TOKEN_UPLOAD_URL, "");
      //注册账号
      if(i==302 || i == 404) {//302 用户名或密码错误, 404对象不存在
        register(mAccout, DEFAULT_PASSWORD);
      } else if (i==415 || i==408) {//客户端网络, 408 超时
        NIMClient.getService(AuthService.class).logout();
        login(mAccout, updateTokenUrl);
      } else {
        login(mAccout, updateTokenUrl);
      }
    }

    @Override
    public void onException(Throwable throwable) {
      LogUtils.d(TAG, " login exception " + throwable.getMessage());
    }
    // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用
  };

  // 更新消息提醒设置
  //NIMClient.updateStatusBarNotificationConfig(config);
  private Observer<List<IMMessage>> incomingMessageObserver = new Observer<List<IMMessage>>() {
    @Override
    public void onEvent(List<IMMessage> messages) {
      // 处理新收到的消息，为了上传处理方便，SDK 保证参数 messages 全部来自同一个聊天对象。
      IMMessage message;
      //Log.v(TAG, this.hashCode()+"接收消息receiver个数: " + messages.size());
      for (int i = 0; i < messages.size(); i++) {
        message = messages.get(i);
        processMessage(message);
      }
    }
  };

  /**
   * 处理消息
   * @param message
   */
  private synchronized void processMessage(IMMessage message) {
    if (mLastMessage != null) {
      if (message.getUuid().equals(mLastMessage.getUuid())) {
        return;
      }
    }
    mLastMessage = message;
    LogUtils.v(TAG, "接收消息receiver: " + message.getFromAccount() + message.getContent() + " " + message.getPushContent() + " " + message.getPushPayload());
    if (message.getFromAccount().equals("qqqqq2")) {//后台的推送账号
      String pushContet = message.getPushContent();
      if (pushContet == null || pushContet.equals("")) {
        //因为之前定义的 getPushContent最大字节数只能150，  导致部分推送就无法发送
        //这里针对大于150字节的内容，就用pushPayload 来做消息推送体， 最大字节2k
        //这里还有问题 ，map转json  [] {}等不完全
        //pushContet = Utils.simpleMapToJsonStr(message.getPushPayload());
        pushContet = JsonUtils.hashMapToJson((HashMap) message.getPushPayload());
      }
      parseMessage(pushContet);
    }
  }

}
