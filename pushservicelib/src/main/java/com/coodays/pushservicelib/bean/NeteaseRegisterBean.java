package com.coodays.pushservicelib.bean;

import lombok.Data;

/**
 * @author zhuj
 * @time 2016/4/21 20:40
 * 网易云信 注册账号 返回信息
 */
@Data  public class NeteaseRegisterBean {
  /**
   * desc : 信息
   * code : 信息编号
   */
  private String desc;
  private int code;
}
