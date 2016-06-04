package com.coodays.pushservicelib.push;

import android.content.Context;
import android.text.TextUtils;
import com.coodays.pushservicelib.utils.CdLogUtils;
import com.coodays.pushservicelib.utils.CdSharedPreferencesUtils;
import com.coodays.pushservicelib.utils.CdTools;

/**
 * @author zhuj
 */
class HuaweiPush extends BasePush {

  HuaweiPush(Context mContext) {
    super(mContext);
  }

  @Override public void init() {
    if(CdTools.isMainProcess(mContext)) {
      CdLogUtils.v("huaweiPush", "huawei push init");
      com.huawei.android.pushagent.api.PushManager.requestToken(mContext);
      com.huawei.android.pushagent.api.PushManager.enableReceiveNormalMsg(mContext, true);
    }
  }

  @Override public void login(String app_user_id, String uploadTokenUrl) {
    CdSharedPreferencesUtils.put(mContext, CdSharedPreferencesUtils.KEY_TOKEN_UPLOAD_URL, uploadTokenUrl);
    CdSharedPreferencesUtils.put(mContext, CdSharedPreferencesUtils.KEY_APP_USER_ID, app_user_id);
    String token = (String) CdSharedPreferencesUtils.get(mContext, CdSharedPreferencesUtils.KEY_TOKEN , "");
    //登入时， 还未获取到token值，
    if (TextUtils.isEmpty(token)) { //请求一次token， 请求成功，会再次触发上传token
      com.huawei.android.pushagent.api.PushManager.requestToken(mContext);
    } else { //直接上传
      startUploadToken(app_user_id, token, ""+PushManager.PHONE_TYPE_HUAWEI);
    }
  }

  @Override public void loginOut() {
    com.huawei.android.pushagent.api.PushManager.enableReceiveNormalMsg(mContext, false);

    loginOutClean();
  }

  @Override public String getStatus() {
    return null;
  }

  @Override public boolean isLogin() {
    return true;
  }

  @Override void uploadToken(String app_user_id, String token) {
    startUploadToken(app_user_id, token, ""+PushManager.PHONE_TYPE_HUAWEI);
  }
}
