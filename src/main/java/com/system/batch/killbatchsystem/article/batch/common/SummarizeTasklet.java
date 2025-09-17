package com.system.batch.killbatchsystem.article.batch.common;

import com.system.batch.killbatchsystem.article.application.ArticleService;
import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.notification.SummarizeCompletePayload;
import com.system.batch.killbatchsystem.notification.sse.SseHub;
import com.system.batch.killbatchsystem.summary.application.SummaryService;
import com.system.batch.killbatchsystem.summary.domain.CreateSummary;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class SummarizeTasklet implements Tasklet {

  private final ArticleService articleService;
  private final SummaryService summaryService;
  private final SseHub sseHub;


  @Value("${summary.scheduler.batch-size}")
  private int batchSize;
  @Value("${summary.scheduler.max-per-tick}")
  private int maxPerTick;
  @Value("${summary.scheduler.throttle-ms}")
  private long throttleMs;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    int processedTotal = 0;

    while (processedTotal < maxPerTick) {
      List<Article> batch = articleService.findBatchForSummarize(batchSize);
      if (batch.isEmpty()) {
        break;
      }

      for (Article a : batch) {
        boolean created = false;
        try {
          CreateSummary req = new CreateSummary(a.content(), a.publishedAt(), a.thumbnailUrl(),
              a.url(), a.sourceName());
          summaryService.createAIArticle(req);
          created = true;

        } catch (Exception e) {
          log.warn("[summarize] failed. articleId={}, err={}", a.id(), e.toString());
          try {
            articleService.failedSummarize(a);
          } catch (Exception e2) {
            log.error("[summarize] failSummarize failed. articleId={}, err={}", a.id(),
                e2.toString());
          }
        }
        if (created) {
          try {
            articleService.completeSummarize(a);
            processedTotal++;
          } catch (Exception e2) {
            log.error("[summarize] complete failed. articleId={}, err={}", a.id(), e2.toString());
          }
        }

        if (throttleMs > 0) {
          Thread.sleep(throttleMs);
        }
        if (processedTotal >= maxPerTick) {
          break;
        }
      }
    }
    if (processedTotal > 0) {
      log.info("[summarize] done. processed={}", processedTotal);
      sseHub.sendToAll("summarize.complete",
          new SummarizeCompletePayload(processedTotal, Instant.now().toString()));
    } else {
      log.info("[summarize] no work to do.");
    }

    return RepeatStatus.FINISHED;
  }
}