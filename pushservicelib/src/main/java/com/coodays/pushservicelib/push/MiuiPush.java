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

  @Override public void init() {
    //小米4  token
    if(CdTools.isMainProcess(mContext)) {
      String appUserId = (String) CdSharedPreferencesUtils.get(mContext, CdSharedPreferencesUtils.KEY_APP_USER_ID, "");
      String uploadToken = (String) CdSharedPreferencesUtils.get(mContext, appUserId, "");
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

  @Override public void login(String app_user_id, String uploadTokenUrl) {
    CdSharedPreferencesUtils.put(mContext, CdSharedPreferencesUtils.KEY_TOKEN_UPLOAD_URL, uploadTokenUrl);
    CdSharedPreferencesUtils.put(mContext, CdSharedPreferencesUtils.KEY_APP_USER_ID, app_user_id);
    String token = (String) CdSharedPreferencesUtils.get(mContext, CdSharedPreferencesUtils.KEY_TOKEN , "");
    //登入时， 还未获取到token值，
    if (TextUtils.isEmpty(token)) { //请求一次token， 请求成功，会再次触发上传token
      MiPushClient.registerPush(mContext, APP_ID, APP_KEY);
    } else { //直接上传
      startUploadToken(app_user_id, token, ""+PushManager.PHONE_TYPE_MIUI);
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

  @Override void uploadToken(String app_user_id, String token) {
    startUploadToken(app_user_id, token, ""+PushManager.PHONE_TYPE_MIUI);
  }
}
