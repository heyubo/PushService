package com.coodays.pushservicelib.network;

import com.coodays.pushservicelib.bean.CdNetResult;
import com.coodays.pushservicelib.bean.CdNeteaseRegisterBean;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;
import rx.Observable;

/**
 * 网络API块口
 * Created by panliuting on 16/3/16.
 */
public interface CdIHttpApiService {

  /**
   * 上传消息读取接口
   * @param url  上传的url
   * @param content 内容
   */
  @FormUrlEncoded @POST Observable<CdNetResult> setReadMessage(@Url String url, @Field("content") String content);

  /**
   * 云信注册
   * @param accid 账号
   * @param token 密码
   */
  @Headers({
      "appKey:aca00f3be88dfe5de5d00fda44fc0113",
      "Nonce:12345",
      "Content-Type:application/x-www-form-urlencoded;charset=utf-8"
  })
  @FormUrlEncoded @POST("https://api.netease.im/nimserver/user/create.action")
  Observable<CdNeteaseRegisterBean> neteaseRegister(@Header("CurTime") String curTime,
      @Header("CheckSum") String checkSum, @Field("accid") String accid,
      @Field("token") String token);

}









