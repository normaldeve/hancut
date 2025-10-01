package com.system.batch.killbatchsystem.common.exception;

import lombok.Getter;

/**
 * 커스텀 예외 처리
 *
 * @author : junwo
 * @fileName : CustomException
 * @since : 2025-10-01
 */
@Getter
public class CustomException extends RuntimeException{
  private final ErrorCode errorCode;

  public CustomException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
