package com.coodays.pushservicelib.network;

import com.coodays.pushservicelib.bean.CdNeteaseRegisterBean;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rx.Observable;

/**
 * 网络API块口
 * Created by panliuting on 16/3/16.
 */
public interface CdIHttpApiService {

  /**
   * 云信注册
   *
   * @param accid 账号
   * @param token 密码
   */
  @Headers({
      "Content-Type:application/x-www-form-urlencoded;charset=utf-8"
  }) @FormUrlEncoded @POST("https://api.netease.im/nimserver/user/create.action")
  Observable<CdNeteaseRegisterBean> neteaseRegister(
      @Header("appKey") String appKey,
      @Header("Nonce") String nonce,
      @Header("CurTime") String curTime,
      @Header("CheckSum") String checkSum,
      @Field("accid") String accid,
      @Field("token") String token);

  /**
   * 云信修改token 也就是密码
   *
   * @param accid 账号
   * @param token 密码
   */
  @Headers({
      "Content-Type:application/x-www-form-urlencoded;charset=utf-8"
  }) @FormUrlEncoded @POST("https://api.netease.im/nimserver/user/update.action")
  Observable<CdNeteaseRegisterBean> neteaseUpdateToken(
      @Header("appKey") String appKey,
      @Header("Nonce") String nonce,
      @Header("CurTime") String curTime,
      @Header("CheckSum") String checkSum,
      @Field("accid") String accid,
      @Field("token") String token);
}









