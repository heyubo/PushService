package com.coodays.pushservicelib.push;

import android.content.Context;
import android.content.Intent;
import com.coodays.pushservicelib.utils.CdSharedPreferencesUtils;

/**
 * @author zhuj
 *         基本推送类型,上传token, 解析推送消息
 *
 *         UPLOAD_TOKEN保存当前的获得token
 *         app_user_id 对应的value  是上传过的token
 */public abstract class BasePush {

  private final static String TAG = BasePush.class.getSimpleName();

  private String mAppUserId;
  /**
   * 服务器的账号
   */
  final String DEFAULT_SERVER_ACCOUNT = "coodays";

  Context mContext;

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
  public abstract void login(String app_user_id, String password);

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
        Intent intent = new Intent(PushManager.ACTION_PUSH_MESSAGE);
        intent.putExtra("push_message", content);
        mContext.sendBroadcast(intent);
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

}
