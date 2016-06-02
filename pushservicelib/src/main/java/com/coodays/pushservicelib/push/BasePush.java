package com.coodays.pushservicelib.push;

import android.content.Context;
import android.content.Intent;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import com.coodays.pushservicelib.BuildConfig;
import com.coodays.pushservicelib.bean.NetResult;
import com.coodays.pushservicelib.network.CustomException;
import com.coodays.pushservicelib.utils.JsonUtils;
import com.coodays.pushservicelib.utils.LogUtils;
import com.coodays.pushservicelib.utils.SharedPreferencesUtils;
import com.coodays.pushservicelib.utils.Tools;
import com.google.gson.Gson;
import java.io.IOException;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author zhuj
 * 基本推送类型,上传token, 解析推送消息
 */
public abstract class BasePush {

  private final static String TAG = BasePush.class.getSimpleName();

  /**
   * 默认密码
   */
  final String DEFAULT_PASSWORD = "coodays";

  Context mContext;

  BasePush(Context mContext) {
    this.mContext = mContext;
  }

  /**
   * 初始化
   */
  public abstract void init();

  /**
   * 登入，使用的是默认密码
   * @param  app_user_id 用户的id
   * @param uploadTokenUrl  上传token 接口url ， content = value
   */
  public abstract void login(String app_user_id, String uploadTokenUrl);

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
   * 上传token
   * @param app_user_id 用户id
   * @param token token值
   * @return
   */
  abstract void uploadToken(String app_user_id, String token);

  /**
   * 上传token到服务器
   * @param app_user_id  用户账号
   * @param token token值
   * @param phone_brand 手机类型 1小米 2华为 3其他
   */
  void startUploadToken( final String app_user_id, final String token, final String phone_brand) {
    String uploadToken = (String) SharedPreferencesUtils.get(mContext, app_user_id, "");
    final String uploadTokenUrl = (String) SharedPreferencesUtils.get(mContext, SharedPreferencesUtils.KEY_TOKEN_UPLOAD_URL, "");
    parseMessageDebug("开始上传 " + app_user_id + " token: " + token + " phone_brand: " + phone_brand  + " url:"+uploadTokenUrl);
    LogUtils.d(TAG,"startUploadToken， 并开始上传 " + app_user_id + " token: " + token + " phone_brand: " + phone_brand + " url:"+uploadTokenUrl);
    if (TextUtils.isEmpty(app_user_id) || TextUtils.isEmpty(uploadTokenUrl)) {
      return ;
    }
    if (!uploadToken.equals(token)) {//同一个token表示已经上传过了
      if (Tools.isNetworkAvailable(mContext)) {
        Observable<NetResult> observable = Observable.create(new Observable.OnSubscribe<NetResult>() {
          @Override public void call(Subscriber<? super NetResult> subscriber) {
            ArrayMap<String, String> data = new ArrayMap<>();
            data.put("app_user_id", app_user_id);
            data.put("token", token);
            data.put("phone_brand", phone_brand);
            String params = JsonUtils.arrayMapToJson(data);

            //retrofit2 只能动态替换固定url中某一段值， 或固定写死url
            //无法完全 动态替换url ，所以使用 okhttp的形式
            OkHttpClient okHttpClient = new OkHttpClient();
            FormBody body = new FormBody.Builder()
                .add("content", params)
                .build();
            Request request = new Request.Builder()
                .url(uploadTokenUrl)
                .post(body)
                .build();
            try {
              Response response = okHttpClient.newCall(request).execute();
              Gson gson = new Gson();
              String str = response.body().string();
              LogUtils.d(TAG, " 返回：" + str);
              if (response.isSuccessful()) {
                NetResult netResult = gson.fromJson(str, NetResult.class);
                if (netResult.isNetResultCodeOk()) {
                  subscriber.onNext(netResult);
                } else {
                  subscriber.onError(new CustomException(netResult.getResult()));
                }
              } else {
                subscriber.onError(new IOException("Unexpected code " + response));
              }
            } catch (IOException e) {
              e.printStackTrace();
              subscriber.onError(e);
            }
          }
        });
        observable.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .unsubscribeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<NetResult>() {
              @Override public void onCompleted() {

              }

              @Override public void onError(Throwable e) {
                LogUtils.d(TAG, "error" + e.getMessage());
              }

              @Override public void onNext(NetResult netResult) {
                LogUtils.d(TAG, "upload push token " + netResult.getResult().getCode());
                if(netResult.isNetResultCodeOk()) {
                  parseMessageDebug("上传token结果："+netResult.toString());
                  SharedPreferencesUtils.put(mContext, app_user_id, token);
                }
              }
            });
      }
    }
  }

  //调试信息发送广播，方便测试软件 界面上 显示具体 进度
  public void parseMessageDebug(String content) { //发送广播
    if (BuildConfig.DEBUG) {
      Intent intent = new Intent(PushManager.ACTION_PUSH_MESSAGE);
      intent.putExtra("push_message", content);
      mContext.sendBroadcast(intent);
    }
  }

  /**
   * 将接收到的数据发送广播
   * @param content
   */
  public void parseMessage(String content) { //发送广播
    Intent intent = new Intent(PushManager.ACTION_PUSH_MESSAGE);
    intent.putExtra("push_message", content);
    mContext.sendBroadcast(intent);
  }

  void loginOutClean( ) {
    String appUserId = (String) SharedPreferencesUtils.get(mContext, SharedPreferencesUtils.KEY_APP_USER_ID, "");
    SharedPreferencesUtils.put(mContext, appUserId, "");
    SharedPreferencesUtils.put(mContext, SharedPreferencesUtils.KEY_TOKEN, "");
    SharedPreferencesUtils.put(mContext, SharedPreferencesUtils.KEY_APP_USER_ID, "");
  }

}
