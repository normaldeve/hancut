package com.system.batch.killbatchsystem.article.infrastructure.batch.bbc;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.article.infrastructure.batch.common.NewsCrawler;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

@Slf4j
@RequiredArgsConstructor
public class BBCReader implements ItemReader<Article> {

  private final NewsCrawler newsCrawler;
  private final String url;
  private final int limit;

  private Iterator<Article> articleIterator;

  @Override
  public Article read() throws Exception {
    if (articleIterator == null) {
      List<Article> articles = newsCrawler.getArticles(url, limit);
      articleIterator = articles.iterator();
    }

    if (articleIterator.hasNext()) {
      return articleIterator.next();
    } else {
      return null;
    }
  }
}
