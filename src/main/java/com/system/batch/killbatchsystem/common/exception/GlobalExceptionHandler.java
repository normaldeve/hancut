package com.system.batch.killbatchsystem.common.exception;

import com.system.batch.killbatchsystem.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ApiResponse<ErrorResponse>> handleException(
      Exception e,
      jakarta.servlet.http.HttpServletRequest request
  ) {
    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

    String msg = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
    log.error("[GlobalExceptionHandler] {} - message: {}", errorCode.getCode(), msg, e);

    ErrorResponse errorResponse = ErrorResponse.builder()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .build();

    ApiResponse<ErrorResponse> body = ApiResponse.<ErrorResponse>builder()
        .path(request != null ? request.getRequestURI() : "") // null 금지
        .data(errorResponse)
        .build();

    return ResponseEntity
        .status(errorCode.getHttpStatus())
        .body(body);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleNoHandler(NoResourceFoundException e,
      HttpServletRequest request) {
    ErrorCode errorCode = ErrorCode.NOT_FOUND;
    String msg = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
    log.error("[GlobalExceptionHandler] {} - message: {}", errorCode.getCode(), msg, e);

    ApiResponse<ErrorResponse> body = ApiResponse.<ErrorResponse>builder()
        .path(request.getRequestURI())
        .data(ErrorResponse.builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .build())
        .build();

    return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
  }
}