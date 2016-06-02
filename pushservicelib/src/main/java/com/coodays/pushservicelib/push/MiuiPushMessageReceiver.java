package com.coodays.pushservicelib.push;

import android.content.Context;
import com.coodays.pushservicelib.utils.LogUtils;
import com.coodays.pushservicelib.utils.SharedPreferencesUtils;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;
import java.util.List;

/**
 * @author zhuj
 * @time 2016/4/22 10:50
 * 小米推送类型广播
 */
public class MiuiPushMessageReceiver extends PushMessageReceiver {

  private String mRegId;

  private final static String TAG = "miui_push_receiver";

  //透传消息
  @Override public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
    super.onReceivePassThroughMessage(context, miPushMessage);
    try {
      String content = miPushMessage.getContent();
      LogUtils.v(TAG, "接收到消息："+content);
      PushManager.getInstance(context).parseMessage(content);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //通知栏点击
  @Override public void onNotificationMessageClicked(Context context, MiPushMessage miPushMessage) {
    super.onNotificationMessageClicked(context, miPushMessage);
  }

  //接收到通知
  @Override public void onNotificationMessageArrived(Context context, MiPushMessage miPushMessage) {
    super.onNotificationMessageArrived(context, miPushMessage);
  }

  //接收到消息
  @Override public void onReceiveMessage(Context context, MiPushMessage miPushMessage) {
    super.onReceiveMessage(context, miPushMessage);
  }

  //客户端发送命令的响应
  @Override
  public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
    super.onCommandResult(context, miPushCommandMessage);
  }

  //接收注册消息
  @Override
  public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
    super.onReceiveRegisterResult(context, message);
    String command = message.getCommand();
    List<String> arguments = message.getCommandArguments();
    String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
    String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
    LogUtils.v(TAG, " miui register " + cmdArg1);
    if (MiPushClient.COMMAND_REGISTER.equals(command)) {
      if (message.getResultCode() == ErrorCode.SUCCESS) {
        mRegId = cmdArg1;
        String app_user_id = (String) SharedPreferencesUtils.get(context, SharedPreferencesUtils.KEY_APP_USER_ID, "");
        SharedPreferencesUtils.put(context, SharedPreferencesUtils.KEY_TOKEN, mRegId);
        PushManager.getInstance(context).uploadToken(app_user_id, mRegId );
      }
    }
  }
}
