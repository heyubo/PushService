package com.coodays.pushtest;

import android.app.Application;
import com.coodays.pushservicelib.push.PushManager;

/**
 * @author zhuj
 * @time 2016/6/1 14:42
 */
public class MyApplication extends Application{

  @Override public void onCreate() {
    super.onCreate();
    PushManager.getInstance(getApplicationContext()).init();
  }

  @Override public void onTerminate() {
    PushManager.getInstance(getApplicationContext()).loginOut();
    super.onTerminate();
  }
}
