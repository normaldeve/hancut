package com.system.batch.killbatchsystem.common.api;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {

  private final LocalDateTime timestamp = LocalDateTime.now();
  private String path;
  private T data;
}
