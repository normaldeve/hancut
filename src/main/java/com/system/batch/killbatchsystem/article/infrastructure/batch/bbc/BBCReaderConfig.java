package com.system.batch.killbatchsystem.article.infrastructure.batch.bbc;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.article.infrastructure.batch.common.NewsCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BBCReaderConfig {

  @Qualifier("bbcNewsCrawler")
  private final NewsCrawler newsCrawler;

  @Bean
  @StepScope
  public ItemReader<Article> bbcReader(
      @Value("${article.bbc.url}") String bbcUrl,
      @Value("${article.limit}") int limit
  ) {
    return new BBCReader(newsCrawler, bbcUrl, limit);
  }
}
