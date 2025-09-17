package com.system.batch.killbatchsystem.summary.application;

import com.system.batch.killbatchsystem.article.infrastructure.batch.common.ArticleSource;
import com.system.batch.killbatchsystem.summary.domain.AISummary;
import com.system.batch.killbatchsystem.summary.domain.TopKeyword;
import com.system.batch.killbatchsystem.summary.infrastructure.jpa.SortBy;
import io.micrometer.common.lang.Nullable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AISummaryRepository {

  AISummary save(AISummary aiArticle);

  Optional<AISummary> findById(Long id);

  List<TopKeyword> findTopKeywords(Pageable pageable);

  Page<AISummary> findPage(@Nullable String keyword, @Nullable ArticleSource sourceName, Pageable pageable, SortBy sortBy);

}
