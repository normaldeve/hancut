package com.system.batch.killbatchsystem.article.infrastructure.batch.espn;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EspnReaderConfig {

  @Bean
  @StepScope
  public ItemReader<String> espnEplJsonListReader(
      @Value("${espn.article-url}") String apiUrl,
      @Value("${espn.limit}") int limit
  ) {
    return new EspnEplJsonListReader(apiUrl, limit);
  }
}
