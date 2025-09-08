package com.system.batch.killbatchsystem.article.infrastructure.jpa;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticleJpaRepository extends JpaRepository<ArticleEntity, Long> {

  boolean existsByArticleId(String articleId);

  @Query("select a from ArticleEntity a where a.summarizeStatus = 'PENDING' order by a.id asc")
  List<ArticleEntity> findBatchForSummarize(Pageable pageable);

  default List<ArticleEntity> findBatchForSummarize(int limit) {
    return findBatchForSummarize(PageRequest.of(0, limit));
  }
}