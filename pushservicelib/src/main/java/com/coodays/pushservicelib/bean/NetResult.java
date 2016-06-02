package com.coodays.pushservicelib.bean;

import lombok.Data;

/**
 * 通用接口返回数据
 * Created by panliuting on 16/3/16.
 */
@Data public class NetResult {

  /**
   * code : 服务器返回码
   * info : 服务器返回说明
   */
  public static final String NET_RESULT_CODE_OK = "2000";//服务器正常返回码
  public static final String UPDATE_PHONE_NET_RESULT = "UpdateWorkerPhone";//更新手机号码返回
  public static final String ASK_VERCODE_NET_RESULT = "AskVerCode";//请求验证码返回
  private ResultEntity result;
  private String tag = "";

  public boolean isNetResultCodeOk() {
    return getResult().getCode().equals(NetResult.NET_RESULT_CODE_OK);
  }

  @Data public static class ResultEntity {
    private String code;
    private String info;
  }

}
