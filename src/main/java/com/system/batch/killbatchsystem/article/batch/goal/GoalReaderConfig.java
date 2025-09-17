package com.system.batch.killbatchsystem.article.batch.goal;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class GoalReaderConfig {

  @Bean
  @StepScope
  public ItemReader<String> goalReader(
      @Value("${article.goal.kr-url}") String goalKrUrl,
      @Value("${article.limit}") int limit
  ) {
    return new GoalReader(goalKrUrl, limit);
  }
}
