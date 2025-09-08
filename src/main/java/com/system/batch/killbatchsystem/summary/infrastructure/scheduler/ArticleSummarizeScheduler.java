package com.system.batch.killbatchsystem.summary.infrastructure.scheduler;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.article.application.ArticleService;
import com.system.batch.killbatchsystem.notification.SummarizeCompletePayload;
import com.system.batch.killbatchsystem.notification.sse.SseHub;
import com.system.batch.killbatchsystem.summary.domain.CreateSummary;
import com.system.batch.killbatchsystem.summary.application.SummaryService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleSummarizeScheduler {

  private final ArticleService articleService;
  private final SummaryService summaryService;
  private final SseHub sseHub;

  @Value("${summary.scheduler.batch-size}")
  private int batchSize;

  @Value("${summary.scheduler.max-per-tick}")
  private int maxPerTick;

  @Value("${summary.scheduler.throttle-ms}")
  private long throttleMs;

  @Value("${summary.scheduler.enabled}")
  private boolean enabled;

  @Scheduled(fixedDelayString = "${summary.scheduler.fixed-delay-ms}")
  public void run() {
    log.info("[summarizer] tick: enabled={}, batchSize={}, maxPerTick={}, throttleMs={}",
        enabled, batchSize, maxPerTick, throttleMs);
    if (!enabled) return;

    int processedTotal = 0;
    while (processedTotal < maxPerTick) {
      List<Article> batch = articleService.findBatchForSummarize(batchSize);
      log.info("[summarizer] processed {} articles", batch.size());
      if (batch.isEmpty()) {
        if (processedTotal > 0) log.info("Summarize done. processed={}", processedTotal);
        break;
      }

      for (Article a : batch) {
        try {
          CreateSummary createSummarize = new CreateSummary(a.content(), a.publishedAt(),
              a.thumbnailUrl(), a.url(), a.sourceName());
          summaryService.createAIArticle(createSummarize);
          articleService.completeSummarize(a);
          processedTotal++;

          if (throttleMs > 0) Thread.sleep(throttleMs);
          if (processedTotal >= maxPerTick) break;

        } catch (Exception e) {
          log.warn("Summarize failed. articleId={}, err={}", a.id(), e.toString());
        }
      }
    }

    if (processedTotal > 0) {
      log.info("Summarize done. processed={}", processedTotal);
      SummarizeCompletePayload payload = new SummarizeCompletePayload(processedTotal, Instant.now().toString());
      sseHub.sendToAll("summarize.complete", payload);
    }
  }
}
