package com.coodays.pushservicelib.push;

import android.content.Context;
import com.coodays.pushservicelib.utils.CdLogUtils;
import com.coodays.pushservicelib.utils.CdSharedPreferencesUtils;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;
import java.util.List;

/**
 * @author zhuj
 * 小米推送类型广播
 */
public class MiuiPushMessageReceiver extends PushMessageReceiver {

  private String mRegId;

  private final static String TAG = "miui_push_receiver";
  private String lastMessageId = "";

  //透传消息
  @Override public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
    super.onReceivePassThroughMessage(context, miPushMessage);
    try {
      String content = miPushMessage.getContent();
      CdLogUtils.v(TAG, "接收到消息 id："+ miPushMessage.getMessageId() + " " +content);
      if (!lastMessageId.equals(miPushMessage.getMessageId())) {
        MiuiPush.getInstance(context).parseMessage(content);
        lastMessageId = miPushMessage.getMessageId();
      }
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
    CdLogUtils.v(TAG, " miui register " + cmdArg1);
    if (MiPushClient.COMMAND_REGISTER.equals(command)) {
      if (message.getResultCode() == ErrorCode.SUCCESS) {
        mRegId = cmdArg1;
        String app_user_id = (String) CdSharedPreferencesUtils.get(context, CdSharedPreferencesUtils.KEY_APP_USER_ID, "");
        CdSharedPreferencesUtils.savaToken(context, ""+PushManager.PHONE_TYPE_MIUI, mRegId);

        //广播发送获得token
        PushManager.sendBroadcastToken(context);
      }
    }
  }
}
