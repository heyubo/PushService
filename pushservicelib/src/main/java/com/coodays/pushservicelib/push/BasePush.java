package com.coodays.pushservicelib.push;

import android.content.Context;
import android.content.Intent;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import com.coodays.pushservicelib.bean.CdNetResult;
import com.coodays.pushservicelib.bean.MessageEntity;
import com.coodays.pushservicelib.components.modules.CdAppModule;
import com.coodays.pushservicelib.network.CdIHttpApiService;
import com.coodays.pushservicelib.utils.CdJsonUtils;
import com.coodays.pushservicelib.utils.CdLogUtils;
import com.coodays.pushservicelib.utils.CdSharedPreferencesUtils;
import com.coodays.pushservicelib.utils.CdTools;
import com.google.gson.Gson;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * @author zhuj
 *         基本推送类型,上传token, 解析推送消息
 *
 *         UPLOAD_TOKEN保存当前的获得token
 *         app_user_id 对应的value  是上传过的token
 */public abstract class BasePush {

  private Subscription mSubscription;

  private final static String TAG = BasePush.class.getSimpleName();

  private String mAppUserId;
  /**
   * 默认密码
   */
  final String DEFAULT_PASSWORD = "coodays";

  Context mContext;
  Gson gson = new Gson();

  BasePush(Context mContext) {
    this.mContext = mContext;
  }

  /**
   * 获得token，
   */
  public String getToken() {
    String token = CdSharedPreferencesUtils.getToken(mContext);
    return token;
  }

  /**
   * 初始化
   */
  public abstract void init();

  /**
   * 登入，使用的是默认密码
   *
   * @param app_user_id 用户的id
   */
  public abstract void login(String app_user_id);

  /**
   * 登出
   */
  public abstract void loginOut();

  /**
   * 状态
   */
  public abstract String getStatus();

  /**
   * 是否登入
   */
  public abstract boolean isLogin();

  /**
   * 将接收到的数据发送广播
   */
  void parseMessage(String content) { //发送广播
    String str = content;
    MessageEntity entity = gson.fromJson(content, MessageEntity.class);
    if (mAppUserId==null) {
      mAppUserId = (String) CdSharedPreferencesUtils.get(mContext, CdSharedPreferencesUtils.KEY_APP_USER_ID, "");
    }
    if (entity.getApp_user_id()!=null && entity.getApp_user_id().equalsIgnoreCase(mAppUserId)) { //用户过滤
      String keys = (String) CdSharedPreferencesUtils.get(mContext, "receiver_key", "");
      String messageKey = entity.getMessage_key();
      if (!keys.contains(messageKey)) {    //消息过滤
        readMessageSet(entity.getMessage_id());
        CdSharedPreferencesUtils.put(mContext, "receiver_key", keys+","+messageKey);
        Intent intent = new Intent(PushManager.ACTION_PUSH_MESSAGE);
        intent.putExtra("push_message", content);
        mContext.sendBroadcast(intent);
      }
    }

  }

  /**
   * 清除登入信息
   */
  void loginOutClean() {
    String appUserId = (String) CdSharedPreferencesUtils.get(mContext, CdSharedPreferencesUtils.KEY_APP_USER_ID, "");
    CdSharedPreferencesUtils.put(mContext, appUserId, "");
    CdSharedPreferencesUtils.cleanToken(mContext);
    CdSharedPreferencesUtils.put(mContext, CdSharedPreferencesUtils.KEY_APP_USER_ID, "");
  }

  /**
   * 设置 消息已经读到
   * @param messageId 对应的消息id（唯一）
   */
  void readMessageSet(String messageId) {
    String appUserId = (String) CdSharedPreferencesUtils.get(mContext, CdSharedPreferencesUtils.KEY_APP_USER_ID, "");
    String readMessageUrl = (String) CdSharedPreferencesUtils.get(mContext, CdSharedPreferencesUtils.KEY_READ_MESSAGE_URL, "");

    ArrayMap<String, String> data = new ArrayMap<>();
    data.put("app_user_id", appUserId);
    data.put("message_id", messageId);
    String params = CdJsonUtils.arrayMapToJson(data);
    CdIHttpApiService httpApiService = CdAppModule.getInstantce(mContext).provideAuthenticationService();
    Observable<CdNetResult> observable = httpApiService.setReadMessage(readMessageUrl, params);
    observable.subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .unsubscribeOn(Schedulers.io())
        .subscribe(new Observer<CdNetResult>() {
          @Override public void onCompleted() {

          }

          @Override public void onError(Throwable e) {
            e.printStackTrace();
          }

          @Override public void onNext(CdNetResult cdNetResult) {

          }
        });
  }
}
