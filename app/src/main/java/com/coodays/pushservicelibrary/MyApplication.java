package com.coodays.pushservicelibrary;

import android.app.Application;
import com.coodays.pushservicelib.push.PushManager;

/**
 * @author zhuj
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
