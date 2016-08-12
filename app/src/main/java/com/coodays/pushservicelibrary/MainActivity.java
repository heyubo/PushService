package com.coodays.pushservicelibrary;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.coodays.pushservicelib.push.PushManager;

public class MainActivity extends Activity {

  //gradlew clean build bintrayUpload -PbintrayUser=heyubo -PbintrayKey=daba6de8e4f078aff202aa14dac413af20ad7992 -PdryRun=false

  @Bind(R.id.et_appUserId) EditText mEtAppUserId; //输入用户上传到服务器的 ， AppUserId
  @Bind(R.id.btn_login) Button mBtnLogin;
  @Bind(R.id.btn_loginOut) Button mBtnLoginOut;
  @Bind(R.id.tv_content) TextView mTvContent;
  @Bind(R.id.btn_clean) Button mBtnClean;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

  }

  @OnClick({ R.id.btn_login, R.id.btn_loginOut }) public void onClick(View view) {
    switch (view.getId()) {
      case R.id.btn_login:
        PushManager.getInstance(this).login(mEtAppUserId.getText().toString(), "password");
        //"http://si.51xiuj.com/v1/token/set",
        break;
      case R.id.btn_loginOut:
        PushManager.getInstance(this).loginOut();
        break;
    }
  }

  @OnClick(R.id.btn_clean) public void onClick() {
    mTvContent.setText("");
  }

  @Override protected void onStart() {
    registerReceiver(myBroadcast, new IntentFilter(PushManager.ACTION_PUSH_MESSAGE));
    super.onStart();
  }

  @Override protected void onStop() {
    unregisterReceiver(myBroadcast);
    super.onStop();
  }

  BroadcastReceiver myBroadcast = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action.equals(PushManager.ACTION_PUSH_MESSAGE)) {
        String content = intent.getStringExtra("push_message");
        mTvContent.append(content+"\n");
      }
    }
  };

}
