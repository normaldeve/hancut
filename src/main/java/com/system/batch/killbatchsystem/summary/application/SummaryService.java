package com.system.batch.killbatchsystem.summary.application;

import com.system.batch.killbatchsystem.model.ArticleSource;
import com.system.batch.killbatchsystem.model.PageResponseGetAISummary;
import com.system.batch.killbatchsystem.model.SortBy;
import com.system.batch.killbatchsystem.model.TopKeyword;
import com.system.batch.killbatchsystem.summary.domain.AISummary;
import com.system.batch.killbatchsystem.summary.domain.CreateSummary;
import jakarta.annotation.Nullable;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface SummaryService {

  void createAIArticle(CreateSummary createSummarize);

  AISummary findAISummaryById(Long id);

  List<TopKeyword> topKeywords(int limit);

  PageResponseGetAISummary getArticles(@Nullable String keyword, @Nullable ArticleSource sourceName, Pageable pageable, SortBy sortBy);

}
