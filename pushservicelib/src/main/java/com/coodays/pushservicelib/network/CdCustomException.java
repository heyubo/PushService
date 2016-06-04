package com.coodays.pushservicelib.network;

import com.coodays.pushservicelib.bean.CdNetResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhuj
 * 访问服务器网络成功，但业务错误
 * 针对服务器code 除2000以外的非正常情况
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class CdCustomException extends Exception {

  private CdNetResult.ResultEntity resultEntity;//包括 code 和 info

  private CdCustomException() {}

  public CdCustomException(CdNetResult.ResultEntity resultEntity) {
    this.resultEntity = resultEntity;
  }

  @Override public String getMessage() {
    return resultEntity.getInfo();
  }
}
