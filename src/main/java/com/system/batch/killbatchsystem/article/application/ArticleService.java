package com.system.batch.killbatchsystem.article.application;

import com.system.batch.killbatchsystem.article.domain.Article;
import java.util.List;

public interface ArticleService {

  void completeSummarize(Article article);

  List<Article> findBatchForSummarize(int limit);
}
