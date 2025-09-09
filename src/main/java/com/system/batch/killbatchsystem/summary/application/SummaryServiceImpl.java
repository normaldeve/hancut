package com.system.batch.killbatchsystem.summary.application;

import com.system.batch.killbatchsystem.comment.application.CommentRepository;
import com.system.batch.killbatchsystem.summary.domain.AISummary;
import com.system.batch.killbatchsystem.summary.domain.CreateSummary;
import com.system.batch.killbatchsystem.summary.domain.GetAISummary;
import com.system.batch.killbatchsystem.summary.domain.SummaryContent;
import com.system.batch.killbatchsystem.summary.domain.TopKeyword;
import com.system.batch.killbatchsystem.summary.infrastructure.jpa.SortBy;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

  private final AISummaryRepository aiSummaryRepository;

  private final AISummarize aisummarize;

  @Value("${keywords.top.max-limit:100}")
  private int maxLimit;

  @Override
  @Transactional
  public void createAIArticle(CreateSummary createSummary) {
    SummaryContent summary = aisummarize.summarize(createSummary.content());

    AISummary aiSummary = AISummary.createSummary(summary, createSummary);

    aiSummaryRepository.save(aiSummary);
  }

  @Override
  @Transactional(readOnly = true)
  public AISummary findAISummaryById(Long id) {
    return aiSummaryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("AISummary not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public List<TopKeyword> topKeywords(int limit) {
    return aiSummaryRepository.findTopKeywords(topPage(limit));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<GetAISummary> getArticles(@Nullable String keyword, @Nullable String sourceName,
      Pageable pageable, SortBy sortBy) {
    Page<AISummary> page = aiSummaryRepository.findPage(keyword, sourceName, pageable, sortBy);
    return page.map(summary -> {
      return GetAISummary.fromArticle(summary);
    });
  }


  private Pageable topPage(int limit) {
    int size = Math.max(1, Math.min(limit, maxLimit));
    return PageRequest.of(0, size);
  }
}
