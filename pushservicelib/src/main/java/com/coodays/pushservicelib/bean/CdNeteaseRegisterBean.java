package com.coodays.pushservicelib.bean;

import lombok.Data;

/**
 * @author zhuj
 * 网易云信 注册账号 返回信息
 */
@Data public class CdNeteaseRegisterBean {
  /**
   * desc : 信息
   * code : 信息编号
   */
  private String desc;
  private int code;
}
