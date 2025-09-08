package com.system.batch.killbatchsystem.article.application;

import com.system.batch.killbatchsystem.article.domain.Article;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

  private final ArticleRepository articleRepository;

  @Override
  @Transactional
  public void completeSummarize(Article article) {

    articleRepository.completedSummary(article);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Article> findBatchForSummarize(int limit) {
    return articleRepository.findBatchForSummarize(limit);
  }
}
