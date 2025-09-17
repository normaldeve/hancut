package com.system.batch.killbatchsystem.article.batch.common;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.article.application.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleItemWriter implements ItemWriter<Article> {

  private final ArticleRepository repository;

  @Override
  @Transactional
  public void write(Chunk<? extends Article> chunk) {
    int wrote = 0;
    for (Article a : chunk) {
      if (!repository.existsByArticleId(a.articleId())) {
        repository.save(a);
        wrote++;
      } else {
        log.debug("Duplicate skip: {}", a.articleId());
      }
    }
    log.info("Wrote {} articles (chunk size: {})", wrote, chunk.size());
  }
}
