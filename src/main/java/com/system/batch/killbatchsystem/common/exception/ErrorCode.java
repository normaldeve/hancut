package com.system.batch.killbatchsystem.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  // 공통
  INTERNAL_SERVER_ERROR("COMMON_101", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  NOT_FOUND("COMMON_102", "잘못된 API 주소로 요청하였습니다.", HttpStatus.NOT_FOUND),

  SUMMARY_NOT_FOUND("SM_101", "ID에 해당하는 뉴스 요약을 찾을 수 없습니다", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;

  ErrorCode(String code, String message, HttpStatus httpStatus) {
    this.code = code;
    this.message = message;
    this.httpStatus = httpStatus;
  }
}
