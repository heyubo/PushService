package com.coodays.pushservicelib.push;

import android.content.Context;
import com.coodays.pushservicelib.utils.LogUtils;
import com.coodays.pushservicelib.utils.OSUtils;
import com.coodays.pushservicelib.utils.SharedPreferencesUtils;

/**
 * @author zhuj
 * @time 2016/3/30 16:25
 * 获得推送
 */
public class PushManager {

  private static volatile BasePush mBashPush;

  public final static int PHONE_TYPE_MIUI = 1;//小米
  public final static int PHONE_TYPE_HUAWEI = 2;//华为
  public final static int PHONE_TYPE_DEFAULT = 3;//其他手机

  public final static String ACTION_PUSH_MESSAGE = "com.coodays.pushservicelib.receiver_message";

  /**
   * 根据手机型号、获得推送类型的实例
   * @param mContext
   * @return  小米推送、华为推送、云信推送
   */
  public static BasePush getInstance(Context mContext) {
    if(mBashPush == null) {
      synchronized (PushManager.class) {
        if(mBashPush == null) {
          //读取缓存 的 推送类型
          int phone_broad = (int) SharedPreferencesUtils.get(mContext, "phone_broad", 0);
          if(phone_broad==0) { //没有缓存的类型， 就根据手机获得推送类型
            if(OSUtils.isMIUI()) {   //小米系统
              phone_broad = PHONE_TYPE_MIUI;
              LogUtils.v("pushManager", " is MiuiPush");
            }else if(OSUtils.isEMUI()) {//华为系统
              phone_broad = PHONE_TYPE_HUAWEI;
              LogUtils.v("pushManager", " is HuaweiPush");
            } else { //其他用 云信推送
              phone_broad = PHONE_TYPE_DEFAULT;
              LogUtils.v("pushManager", " is NeteasePush");
            }
            SharedPreferencesUtils.put(mContext, "phone_broad", phone_broad);
          }
          switch(phone_broad) {
            case PHONE_TYPE_MIUI:
              mBashPush = new MiuiPush(mContext);
              break;
            case PHONE_TYPE_HUAWEI:
              mBashPush = new HuaweiPush(mContext);
              break;
            default:
              mBashPush = new NeteasePush(mContext);
              break;
          }
        }
      }
    }
    return mBashPush;
  }
}
