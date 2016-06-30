package com.coodays.pushservicelib.push;

import android.content.Context;
import android.text.TextUtils;
import com.coodays.pushservicelib.utils.CdLogUtils;
import com.coodays.pushservicelib.utils.CdSharedPreferencesUtils;
import com.coodays.pushservicelib.utils.CdTools;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

/**
 * @author zhuj
 * 小米推送
 */
class MiuiPush extends BasePush{

  private final static String TAG = "MiuiPush";

  private final static String APP_ID = "2882303761517469134";
  private final static String APP_KEY = "5631746994134";

  MiuiPush(Context mContext) {
    super(mContext);
  }

  private static volatile BasePush mBashPush;

  public static BasePush getInstance(Context context){
    if (mBashPush==null) {
      synchronized (MiuiPush.class) {
        if(mBashPush==null) {
          mBashPush = new MiuiPush(context);
        }
      }
    }
    return mBashPush;
  }

  @Override public void init() {
    //小米4  token
    if(CdTools.isMainProcess(mContext)) {
      String appUserId = (String) CdSharedPreferencesUtils.get(mContext, CdSharedPreferencesUtils.KEY_APP_USER_ID, "");
      String uploadToken = CdSharedPreferencesUtils.getTokenSingle(mContext, ""+PushManager.PHONE_TYPE_MIUI);
      if(TextUtils.isEmpty(uploadToken)) {//没有上传过， 才注册上传token
        MiPushClient.registerPush(mContext, APP_ID, APP_KEY);
      }
      CdLogUtils.v("miuiPush", " init(). token " +uploadToken);
      Logger.setLogger(mContext, newLogger);
    }
  }

  LoggerInterface newLogger = new LoggerInterface() {

    @Override
    public void setTag(String tag) {
      // ignore
    }

    @Override
    public void log(String content, Throwable t) {
      CdLogUtils.d(TAG, content + " " + t.getMessage());
    }

    @Override
    public void log(String content) {
      CdLogUtils.d(TAG, content);
    }
  };

  @Override public void login(String app_user_id) {
    CdSharedPreferencesUtils.put(mContext, CdSharedPreferencesUtils.KEY_APP_USER_ID, app_user_id);
    String token = CdSharedPreferencesUtils.getTokenSingle(mContext, ""+PushManager.PHONE_TYPE_MIUI);
    //登入时， 还未获取到token值，
    if (TextUtils.isEmpty(token)) { //请求一次token， 请求成功，会再次触发上传token
      MiPushClient.registerPush(mContext, APP_ID, APP_KEY);
    }
  }

  @Override public void loginOut() {
    MiPushClient.unregisterPush(mContext);
    loginOutClean();
    //MiPushClient.setUserAccount(mContext, "" ,null);
  }

  @Override public String getStatus() {
    return "";
  }

  @Override public boolean isLogin() {
    String appUserId = (String) CdSharedPreferencesUtils.get(mContext, CdSharedPreferencesUtils.KEY_APP_USER_ID, "");
    String regId = (String) CdSharedPreferencesUtils.get(mContext, appUserId, "");
    return TextUtils.isEmpty(regId);
  }

}
