/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.coodays.pushservicelib.components.modules;

import android.content.Context;
import com.coodays.pushservicelib.network.CdIHttpApiService;
import com.coodays.pushservicelib.utils.CdSharedPreferencesUtils;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CdAppModule {

  private static final int DEFAULT_TIMEOUT = 15;//超时时间 单位秒

  private Retrofit mRetrofit;
  private OkHttpClient.Builder mHttpClientBuilder;
  private Context mContext;
  private static volatile CdAppModule mAppMoudle;

  private CdAppModule() {}

  public static CdAppModule getInstantce(Context context) {
    if (mAppMoudle == null) {
      synchronized (CdAppModule.class) {
        if (mAppMoudle == null) {
          mAppMoudle = new CdAppModule(context);
        }
      }
    }
    return mAppMoudle;
  }

  private CdAppModule(Context context) {
    this.mContext = context;
    //File httpCacheDirectory = new File(mContext.getCacheDir(),  "responses");
    //int cacheSize = 10 * 1024 * 1024; // 10 MiB
    //Cache cache = new Cache(httpCacheDirectory, cacheSize);

    //手动创建一个OkHttpClient并设置超时时间
    mHttpClientBuilder = new OkHttpClient.Builder();
    mHttpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    mHttpClientBuilder.addNetworkInterceptor(REWRITE_RESPONSE_INTERCEPTOR)
        //.addInterceptor(OFFLINE_INTERCEPTOR)
        //.addInterceptor(TOKEN_INTERCEPTOR)
        //.cache(cache)
        .build();
    mRetrofit = new Retrofit.Builder().client(mHttpClientBuilder.build())
        .baseUrl("http://sit.51xiuj.com")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build();
  }

  public CdIHttpApiService provideAuthenticationService() {
    return mRetrofit.create(CdIHttpApiService.class);
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


  private  final Interceptor TOKEN_INTERCEPTOR = new Interceptor() {
    @Override public Response intercept(Chain chain) throws IOException {
      Request request = chain.request();
      Request.Builder requestBuilder = request.newBuilder();
      String toekn =  CdSharedPreferencesUtils.getToken(mContext);

      if (request.method().equals("POST")) {
        requestBuilder.url(String.format("%s?tokens=%s", request.url(), toekn));
      } else {
        requestBuilder.url(String.format("%s&tokens=%s", request.url(), toekn));
      }

      return chain.proceed(requestBuilder.build());
    }
  };

}
