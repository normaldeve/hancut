package com.system.batch.killbatchsystem.notification.sse;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseHub {

  private final Set<SseEmitter> emitters = new CopyOnWriteArraySet<>();

  private final AtomicReference<Object> lastSummarizeComplete = new AtomicReference<>();

  public SseEmitter subscribe(Long timeoutMillis) {
    SseEmitter emitter = new SseEmitter(timeoutMillis);
    emitters.add(emitter);

    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError((ex) -> emitters.remove(emitter));

    try {
      emitter.send(SseEmitter.event().name("connected").data(Instant.now().toString()));
      Object cached = lastSummarizeComplete.get();
      if (cached != null) {
        emitter.send(SseEmitter.event().name("summarize.complete").data(cached));
      }
    } catch (IOException ignored) {
      emitters.remove(emitter);
    }
    return emitter;
  }

  public void sendToAll(String eventName, Object payload) {
    if ("summarize.complete".equals(eventName)) {
      lastSummarizeComplete.set(payload);
    }
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name(eventName).data(payload));
      } catch (IOException e) {
        emitter.complete();
        emitters.remove(emitter);
      }
    }
  }
}
