package com.system.batch.killbatchsystem.article.infrastructure;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.article.infrastructure.jpa.ArticleEntity;
import com.system.batch.killbatchsystem.article.infrastructure.jpa.ArticleJpaRepository;
import com.system.batch.killbatchsystem.article.application.ArticleRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepository {

  private final ArticleJpaRepository articleJpaRepository;

  @Override
  public void completedSummary(Article article) {
    ArticleEntity articleEntity = articleJpaRepository.findById(article.id())
        .orElseThrow(() -> new RuntimeException("Can not Found Article"));

    articleEntity.completedSummary();
  }

  @Override
  public void failedSummary(Article article) {
    ArticleEntity articleEntity = articleJpaRepository.findById(article.id())
        .orElseThrow(() -> new RuntimeException("Can not Found Article"));

    articleEntity.failedSummary();

  }

  @Override
  public List<Article> findBatchForSummarize(Pageable pageable) {
    return articleJpaRepository.findBatchForSummarize(pageable).stream()
        .map(ArticleEntity::toModel)
        .collect(Collectors.toList());
  }

  @Override
  public boolean existsByArticleId(String articleId) {
    return articleJpaRepository.existsByArticleId(articleId);
  }

  @Override
  public Article save(Article article) {
    ArticleEntity articleEntity = ArticleEntity.fromModel(article);
    ArticleEntity save = articleJpaRepository.save(articleEntity);

    return ArticleEntity.toModel(save);
  }
}
