package com.system.batch.killbatchsystem.common.exception;

import lombok.Builder;

@Builder
public record ErrorResponse(
    String code,
    String message
) {

  public static ErrorResponse of(ErrorCode errorCode) {
    return ErrorResponse.builder()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .build();
  }
}
