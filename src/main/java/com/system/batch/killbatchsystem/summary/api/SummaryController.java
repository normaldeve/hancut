package com.system.batch.killbatchsystem.summary.api;

import com.system.batch.killbatchsystem.common.tool.HibernateStatsMonitor;
import com.system.batch.killbatchsystem.summary.domain.GetAISummary;
import com.system.batch.killbatchsystem.summary.domain.PageResponse;
import com.system.batch.killbatchsystem.summary.domain.TopKeyword;
import com.system.batch.killbatchsystem.summary.application.SummaryService;
import com.system.batch.killbatchsystem.summary.infrastructure.jpa.SortBy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/summary")
@RequiredArgsConstructor
public class SummaryController {

  @Autowired
  private HibernateStatsMonitor statsMonitor;

  private final SummaryService summaryService;

  @GetMapping
  public ResponseEntity<PageResponse<GetAISummary>> getArticles(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sourceName,
      @RequestParam(name = "sortBy", defaultValue = "LATEST") SortBy sortBy,
      @PageableDefault(size = 20, sort = "publishedAt") Pageable pageable
  ) {

    long startTime = System.currentTimeMillis();
    statsMonitor.startMeasurement();

    Page<GetAISummary> articles = summaryService.getArticles(keyword, sourceName, pageable, sortBy);

    PageResponse<GetAISummary> body = PageResponse.of(articles);

    long duration = System.currentTimeMillis() - startTime;
    statsMonitor.endMeasurement("getArticles");
    log.info("[PERF] getArticles: {}ms 소요", duration);

    return ResponseEntity.ok(body);
  }

  @GetMapping("/keywords/top")
  public ResponseEntity<List<TopKeyword>> getTopKeywords(
      @RequestParam(defaultValue = "10") int limit
  ) {
    List<TopKeyword> body = summaryService.topKeywords(limit);

    return ResponseEntity.ok(body);
  }

}
