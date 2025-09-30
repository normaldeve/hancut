package com.system.batch.killbatchsystem.summary.api;

import com.system.batch.killbatchsystem.app.SummaryApi;
import com.system.batch.killbatchsystem.model.ArticleSource;
import com.system.batch.killbatchsystem.model.PageResponseGetAISummary;
import com.system.batch.killbatchsystem.model.SortBy;
import com.system.batch.killbatchsystem.model.TopKeyword;
import com.system.batch.killbatchsystem.summary.application.SummaryService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SummaryController implements SummaryApi {

  private final SummaryService summaryService;

  @Timed(value = "api.summary.list", description = "AI 뉴스 요약 기사를 조회합니다")
  @Override
  public PageResponseGetAISummary getAISummaries(String keyword,
      ArticleSource sourceName, SortBy sortBy, Integer page, Integer size) throws Exception {
    Pageable pageable = PageRequest.of(page, size);

    return summaryService.getArticles(keyword, sourceName, pageable,
        sortBy);
  }

  @Timed(value = "api.summary.keyword", description = "가장 많이 나온 키워드 Top 10을 조회합니다.")
  @Override
  public TopKeyword getTopKeywords(Integer limit) throws Exception {
    return SummaryApi.super.getTopKeywords(limit);
  }
}
