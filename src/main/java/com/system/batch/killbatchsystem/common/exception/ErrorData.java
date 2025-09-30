package com.system.batch.killbatchsystem.common.exception;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class ErrorData {

  private final ErrorCode errorCode;
  private final List<String> rejectedValues;
  private final String field;

  public static ErrorData of(ErrorCode errorCode, List<String> rejectedValues, String field) {
    return ErrorData.builder()
        .errorCode(errorCode)
        .rejectedValues(rejectedValues)
        .field(field)
        .build();
  }
}
