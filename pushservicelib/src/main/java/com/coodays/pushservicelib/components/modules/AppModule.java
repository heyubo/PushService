/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.coodays.pushservicelib.components.modules;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.coodays.pushservicelib.network.IHttpApiService;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppModule {
  public static final String DOMAIN_TEST_sit = "http://";//测试服务器http://123.57.253.109/51xiu/
  public static final String DOMAIN_TEST_uat = "http://uat.51xiuj.com/";//一期http://www.51xiuj.com:8080/
  public static final String DOMAIN = "http://www.51xiuj.com/";//正式服务器地址
  private static final int DEFAULT_TIMEOUT = 15;

  private Retrofit mRetrofit;
  private OkHttpClient.Builder mHttpClientBuilder;
  private Context mContext;
  private static volatile AppModule mAppMoudle;

  private AppModule() {}

  public static AppModule getInstantce(Context context) {
    if (mAppMoudle == null) {
      synchronized (AppModule.class) {
        if (mAppMoudle == null) {
          mAppMoudle = new AppModule(context);
        }
      }
    }
    return mAppMoudle;
  }

  private AppModule(Context context) {
    this.mContext = context;
    File httpCacheDirectory = new File(mContext.getCacheDir(),  "responses");
    int cacheSize = 10 * 1024 * 1024; // 10 MiB
    Cache cache = new Cache(httpCacheDirectory, cacheSize);

    //手动创建一个OkHttpClient并设置超时时间
    mHttpClientBuilder = new OkHttpClient.Builder();
    mHttpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    mHttpClientBuilder.addNetworkInterceptor(REWRITE_RESPONSE_INTERCEPTOR)
        .addInterceptor(OFFLINE_INTERCEPTOR)
        .cache(cache).build();
    mRetrofit = new Retrofit.Builder().client(mHttpClientBuilder.build())
        .baseUrl(DOMAIN_TEST_sit)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build();
  }

  public IHttpApiService provideAuthenticationService() {
    return mRetrofit.create(IHttpApiService.class);
  }

  Retrofit provideRetrofit() {
    return mRetrofit;
  }

  OkHttpClient.Builder provideOkHttpClient() {
    return mHttpClientBuilder;
  }

  private static final Interceptor REWRITE_RESPONSE_INTERCEPTOR = new Interceptor() {
    @Override public Response intercept(Chain chain) throws IOException {
      Request originalRequest = chain.request();
      Request.Builder request = originalRequest.newBuilder();
      if (originalRequest.header("fresh") != null) {
        request.cacheControl(CacheControl.FORCE_NETWORK);
      }
      Response originalResponse = chain.proceed(chain.request());
      String cacheControl = originalResponse.header("Cache-Control");

      if ((cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
          cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0"))
          && originalRequest.header("CacheControlMaxAge")!=null) {
        return originalResponse.newBuilder()
            .header("Cache-Control", "public, max-age=" + originalRequest.header("CacheControlMaxAge"))
            .build();
      } else {
        return originalResponse;
      }
    }
  };

  private final Interceptor OFFLINE_INTERCEPTOR = new Interceptor() {
    @Override public Response intercept(Chain chain) throws IOException {
      Request request = chain.request();

      if (!isOnline(mContext)) {
        int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
        request = request.newBuilder()
            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
            .build();
      }

      return chain.proceed(request);
    }
  };

  private static boolean isOnline(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
        Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    return netInfo != null && netInfo.isConnectedOrConnecting();
  }

  public OkHttpClient.Builder getOkHttpClient() {
    return mHttpClientBuilder;
  }
}
