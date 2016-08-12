package com.coodays.pushservicelib.push;

import android.content.Context;
import android.content.Intent;
import com.coodays.pushservicelib.utils.CdLogUtils;
import com.coodays.pushservicelib.utils.CdSharedPreferencesUtils;

/**
 * @author zhuj
 *         获得推送
 */
public class PushManager {

  public final static int PHONE_TYPE_MIUI = 1;//小米
  public final static int PHONE_TYPE_HUAWEI = 2;//华为
  public final static int PHONE_TYPE_DEFAULT = 3;//其他手机

  /**
   * 接收到消息
   */
  public final static String ACTION_PUSH_MESSAGE = "com.coodays.pushservicelib.receiver_message";
  /**
   * 踢人
   */
  public final static String ACTION_PUSH_KICK = "com.coodays.pushservicelib.receiver_kick";
  /**
   * 获得token
   */
  public final static String ACTION_PUSH_GET_TOKEN = "com.coodays.pushservicelib.get_token";

  private static volatile PushManager mBashPush;
  private Context mContext;

  private PushManager(Context context){
    this.mContext = context;
  }

  /**
   * 根据手机型号、获得推送类型的实例
   *
   * @return 小米推送、华为推送、云信推送
   */
  public static PushManager getInstance(Context context) {
    if (mBashPush == null) {
      synchronized (PushManager.class) {
        if (mBashPush == null) {
          mBashPush = new PushManager(context);
        }
      }
    }
    return mBashPush;
  }

  public void login(String app_user_id, String password) {
    NeteasePush.getInstance(mContext).login(app_user_id, password);
    HuaweiPush.getInstance(mContext).login(app_user_id, password);
    MiuiPush.getInstance(mContext).login(app_user_id, password);
  }

  public void loginOut() {
    NeteasePush.getInstance(mContext).loginOut();
    HuaweiPush.getInstance(mContext).loginOut();
    MiuiPush.getInstance(mContext).loginOut();
  }

  public void init() {
    NeteasePush.getInstance(mContext).init();
    HuaweiPush.getInstance(mContext).init();
    MiuiPush.getInstance(mContext).init();
    //清除缓存的 ， 收到过的消息 需要过滤的message_key
    CdSharedPreferencesUtils.put(mContext, "receiver_key", "");
  }

  public String getToken() {
    return CdSharedPreferencesUtils.getToken(mContext);
  }

  public static void sendBroadcastToken(Context context) {
    String token = CdSharedPreferencesUtils.getToken(context);
    CdLogUtils.d("pushManager", "send token " + token);
    Intent intent = new Intent(PushManager.ACTION_PUSH_GET_TOKEN);
    intent.putExtra("token", token);
    context.sendBroadcast(intent);
  }

  ///**
  // * 设置 回传收到消息 接口
  // * @param readMessageUrl
  // */
  //public void setReadMessageUrl(String readMessageUrl) {
  //  CdSharedPreferencesUtils.put(mContext , CdSharedPreferencesUtils.KEY_READ_MESSAGE_URL, readMessageUrl);
  //}
}
