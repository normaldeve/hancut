package com.system.batch.killbatchsystem.summary.api;

import com.system.batch.killbatchsystem.app.SummaryApi;
import com.system.batch.killbatchsystem.model.ArticleSource;
import com.system.batch.killbatchsystem.model.PageResponseGetAISummary;
import com.system.batch.killbatchsystem.model.SortBy;
import com.system.batch.killbatchsystem.model.TopKeyword;
import com.system.batch.killbatchsystem.summary.application.AISummarize;
import com.system.batch.killbatchsystem.summary.application.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SummaryController implements SummaryApi {

  private final SummaryService summaryService;

  private final AISummarize aiSummarize;

  @Override
  public PageResponseGetAISummary getAISummaries(String keyword,
      ArticleSource sourceName, SortBy sortBy, Integer page, Integer size) throws Exception {
    Pageable pageable = PageRequest.of(page, size);

    return summaryService.getArticles(keyword, sourceName, pageable,
        sortBy);
  }

  @Override
  public TopKeyword getTopKeywords(Integer limit) throws Exception {
    return SummaryApi.super.getTopKeywords(limit);
  }

  //  @Override
//  public ResponseEntity<GetAISummary> getAISummaries(String keyword, ArticleSource sourceName,
//      SortBy sortBy, Integer size, Integer page) throws Exception {
//    Pageable pageable = PageRequest.of(page, size);
//
//    PageResponse<GetAISummary> articles = summaryService.getArticles(keyword, sourceName, pageable,
//        sortBy);
//
//    return ResponseEntity.ok(articles);
//  }
//
//  @Override
//  public ResponseEntity<TopKeyword> getTopKeywords(Integer limit) throws Exception {
//    return SummaryApi.super.getTopKeywords(limit);
//  }

  //  @Timed(value = "api.summary.list", description = "AI 뉴스 요약 기사를 조회합니다")
//  @GetMapping
//  public ResponseEntity<PageResponse<GetAISummary>> getArticles(
//      @RequestParam(required = false) String keyword,
//      @RequestParam(required = false) ArticleSource sourceName,
//      @RequestParam(name = "sortBy", defaultValue = "LATEST") SortBy sortBy,
//      @PageableDefault(size = 20, sort = "publishedAt") Pageable pageable
//  ) {
//
//    PageResponse<GetAISummary> articles = summaryService.getArticles(keyword, sourceName, pageable,
//        sortBy);
//
//    return ResponseEntity.ok(articles);
//  }

//  @Timed(value = "api.summary.keyword", description = "가장 많이 나온 키워드 Top 10을 조회합니다.")
//  @GetMapping("/keywords/top")
//  public ResponseEntity<List<TopKeyword>> getTopKeywords(
//      @RequestParam(defaultValue = "10") int limit
//  ) {
//    List<TopKeyword> body = summaryService.topKeywords(limit);
//
//    return ResponseEntity.ok(body);
//  }

//  @PostMapping(value = "/ai")
//  public ResponseEntity<SummaryContent> summarize(@RequestBody SummaryRequest request) {
//    SummaryContent result = aiSummarize.summarize(request.content());
//
//    return ResponseEntity.ok(result);
//  }
}
