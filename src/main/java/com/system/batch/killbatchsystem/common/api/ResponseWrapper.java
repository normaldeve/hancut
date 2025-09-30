package com.system.batch.killbatchsystem.common.api;

import com.system.batch.killbatchsystem.common.exception.ErrorCode;
import com.system.batch.killbatchsystem.common.exception.ErrorData;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseWrapper implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response
  ) {
    String path = request.getURI().getPath();

    if (body instanceof ErrorData errorData) {
      ErrorCode errorCode = errorData.getErrorCode();
      response.setStatusCode(errorCode.getHttpStatus());

      return ApiResponse.builder()
          .path(path)
          .data(errorData.getRejectedValues())
          .message(errorCode.getMessage())
          .build();
    }

    return ApiResponse.builder()
        .path(path)
        .data(body)
        .message("성공적으로 처리되었습니다")
        .build();
  }
}
