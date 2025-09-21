package com.system.batch.killbatchsystem.summary.application;

import com.system.batch.killbatchsystem.article.batch.common.ArticleSource;
import com.system.batch.killbatchsystem.summary.domain.AISummary;
import com.system.batch.killbatchsystem.summary.domain.CreateSummary;
import com.system.batch.killbatchsystem.summary.domain.GetAISummary;
import com.system.batch.killbatchsystem.summary.domain.PageResponse;
import com.system.batch.killbatchsystem.summary.domain.SummaryContent;
import com.system.batch.killbatchsystem.summary.domain.TopKeyword;
import com.system.batch.killbatchsystem.summary.infrastructure.jpa.SortBy;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

  private final AISummaryRepository aiSummaryRepository;

  private final AISummarize aisummarize;

  @Value("${keywords.top.max-limit:100}")
  private int maxLimit;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @CacheEvict(cacheNames = {"summaryList", "topKeywords"}, allEntries = true)
  public void createAIArticle(CreateSummary createSummary) {
    SummaryContent summary = aisummarize.summarize(createSummary.content());

    AISummary aiSummary = AISummary.createSummary(summary, createSummary);

    aiSummaryRepository.save(aiSummary);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "summaryDetail", key = "#id")
  public AISummary findAISummaryById(Long id) {
    return aiSummaryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("AISummary not found"));
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "topKeywords", key = "#limit")
  public List<TopKeyword> topKeywords(int limit) {
    return aiSummaryRepository.findTopKeywords(topPage(limit));
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      cacheNames = "summaryList",
      key = "'k='+#keyword+'|s='+#sourceName+'|p='+#pageable.pageNumber+'|z='+#pageable.pageSize+'|o='+#sortBy",
      condition = "#pageable.pageNumber <= 2"
  )
  public PageResponse<GetAISummary> getArticles(@Nullable String keyword, @Nullable ArticleSource sourceName,
      Pageable pageable, SortBy sortBy) {
    Page<AISummary> summaries = aiSummaryRepository.findPage(keyword, sourceName, pageable, sortBy);
    Page<GetAISummary> summariesDTO = summaries.map(GetAISummary::fromArticle);
    return PageResponse.of(summariesDTO);
  }

  private Pageable topPage(int limit) {
    int size = Math.max(1, Math.min(limit, maxLimit));
    return PageRequest.of(0, size);
  }
}
