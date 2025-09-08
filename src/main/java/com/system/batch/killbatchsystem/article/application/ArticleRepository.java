package com.system.batch.killbatchsystem.article.application;

import com.system.batch.killbatchsystem.article.domain.Article;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface ArticleRepository {

  void completedSummary(Article article);

  List<Article> findBatchForSummarize(Pageable pageable);

  default List<Article> findBatchForSummarize(int limit) {
    return findBatchForSummarize(PageRequest.of(0, limit, Sort.by("id").ascending()));
  }

  boolean existsByArticleId(String articleId);

  Article save(Article article);
}
