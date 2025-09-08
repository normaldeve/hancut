package com.system.batch.killbatchsystem.notification.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.system.batch.killbatchsystem.notification.sse.SseHub;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final SseHub sseHub;

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter stream() {
    // 30분 타임아웃 (원하면 더 길게)
    return sseHub.subscribe(30 * 60 * 1000L);
  }
}