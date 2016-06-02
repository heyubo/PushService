package com.coodays.pushservicelib.push;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.coodays.pushservicelib.utils.LogUtils;
import com.coodays.pushservicelib.utils.SharedPreferencesUtils;
import com.huawei.android.pushagent.api.PushEventReceiver;

/**
 * @author zhuj
 * 华为推送接收广播
 */
public class HuaweiPushMessageReceiver extends PushEventReceiver {

  private final static String TAG = "HuaweiPush";

  @Override
  public void onToken(Context context, String token, Bundle extras){
    String belongId = extras.getString("belongId");
    String content = "获取token和belongId成功，token = " + token + ",belongId = " + belongId;
    LogUtils.d(TAG, " getTOken："+content);
    String app_user_id = (String) SharedPreferencesUtils.get(context, SharedPreferencesUtils.KEY_APP_USER_ID, "");
    SharedPreferencesUtils.put(context, SharedPreferencesUtils.KEY_TOKEN, token);
    PushManager.getInstance(context).uploadToken(app_user_id,token);
    //showPushMessage(PustDemoActivity.RECEIVE_TOKEN_MSG, content);
  }


  @Override
  public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
    try {
      String content =new String(msg, "UTF-8");
      LogUtils.d(TAG, "接收到消息"+content);
      PushManager.getInstance(context).parseMessage(content);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public void onEvent(Context context, Event event, Bundle extras) {
    if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
      int notifyId = extras.getInt(BOUND_KEY.pushNotifyId, 0);
      if (0 != notifyId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notifyId);
      }
      String content = "收到通知附加消息： " + extras.getString(BOUND_KEY.pushMsgKey);
      Log.d(TAG, content);
    } else if (Event.PLUGINRSP.equals(event)) {
      final int TYPE_LBS = 1;
      final int TYPE_TAG = 2;
      int reportType = extras.getInt(BOUND_KEY.PLUGINREPORTTYPE, -1);
      boolean isSuccess = extras.getBoolean(BOUND_KEY.PLUGINREPORTRESULT, false);
      String message = "";
      if (TYPE_LBS == reportType) {
        message = "LBS report result :";
      } else if(TYPE_TAG == reportType) {
        message = "TAG report result :";
      }
      LogUtils.d(TAG, message + isSuccess);
    }
    super.onEvent(context, event, extras);
  }

}
