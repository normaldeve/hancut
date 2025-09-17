package com.system.batch.killbatchsystem.summary.infrastructure.jpa;

import com.system.batch.killbatchsystem.article.batch.common.ArticleSource;
import com.system.batch.killbatchsystem.summary.domain.TopKeyword;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AISummaryQueryRepository {

  List<TopKeyword> findTopKeywords(Pageable pageable);

  Page<AISummaryEntity> findPage(String keyword, ArticleSource sourceName, Pageable pageable, SortBy sortBy);
}
