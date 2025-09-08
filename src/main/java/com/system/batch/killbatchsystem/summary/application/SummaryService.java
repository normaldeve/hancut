package com.system.batch.killbatchsystem.summary.application;

import com.system.batch.killbatchsystem.summary.domain.AISummary;
import com.system.batch.killbatchsystem.summary.domain.CreateSummary;
import com.system.batch.killbatchsystem.summary.domain.GetAISummary;
import com.system.batch.killbatchsystem.summary.domain.TopKeyword;
import com.system.batch.killbatchsystem.summary.infrastructure.jpa.SortBy;
import jakarta.annotation.Nullable;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SummaryService {

  void createAIArticle(CreateSummary createSummarize);

  AISummary findAISummaryById(Long id);

  List<TopKeyword> topKeywords(int limit);

  Page<GetAISummary> getArticles(@Nullable String keyword, @Nullable String sourceName, Pageable pageable, SortBy sortBy);

}
