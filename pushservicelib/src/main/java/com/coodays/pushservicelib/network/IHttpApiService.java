package com.coodays.pushservicelib.network;

import com.coodays.pushservicelib.bean.NetResult;
import com.coodays.pushservicelib.bean.NeteaseRegisterBean;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * 网络API块口
 * Created by panliuting on 16/3/16.
 */
public interface IHttpApiService {

  /**
   * 同步TOKEN接口
   *
   * @param content 提交的内容
   */
  @FormUrlEncoded @POST("haha/{url}") Observable<NetResult> setToken(
      @Path("url") String url,
      @Field("content") String content);

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
  Observable<NeteaseRegisterBean> neteaseRegister(@Header("CurTime") String curTime,
      @Header("CheckSum") String checkSum, @Field("accid") String accid,
      @Field("token") String token);

}









