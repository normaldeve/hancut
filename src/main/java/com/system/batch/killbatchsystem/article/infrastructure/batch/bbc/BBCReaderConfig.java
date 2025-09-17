package com.system.batch.killbatchsystem.article.infrastructure.batch.bbc;

import com.system.batch.killbatchsystem.article.domain.Article;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BBCReaderConfig {

  @Bean
  @StepScope
  public ItemReader<Article> bbcReader(
      @Value("${article.bbc.url}") String bbcUrl,
      @Value("${article.limit}") int limit
  ) {
    return new BBCReader(bbcUrl, limit);
  }

}
