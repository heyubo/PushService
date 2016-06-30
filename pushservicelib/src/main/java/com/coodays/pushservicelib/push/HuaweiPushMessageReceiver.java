package com.coodays.pushservicelib.push;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.coodays.pushservicelib.utils.CdLogUtils;
import com.coodays.pushservicelib.utils.CdSharedPreferencesUtils;
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
    CdLogUtils.d(TAG, " getTOken：" + token + ",belongId = " + belongId);
    CdSharedPreferencesUtils.savaToken(context, ""+PushManager.PHONE_TYPE_HUAWEI, token);
    PushManager.sendBroadcastToken(context);
  }


  @Override
  public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
    try {
      String content =new String(msg, "UTF-8");
      CdLogUtils.d(TAG, "接收到消息"+content);
      HuaweiPush.getInstance(context).parseMessage(content);
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
      CdLogUtils.d(TAG, message + isSuccess);
    }
    super.onEvent(context, event, extras);
  }

}
