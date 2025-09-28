package com.system.batch.killbatchsystem.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ErrorResponse> handleException(Exception e) {

    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    log.error("[GlobalExceptionHandler] {} - message: {}",
        errorCode.getCode(), e.getMessage(), e);

    return ResponseEntity
        .status(errorCode.getHttpStatus())
        .body(ErrorResponse.of(errorCode));
  }
}