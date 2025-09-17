package com.system.batch.killbatchsystem.article.infrastructure.batch.goal;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class GoalSitemapReaderConfig {

  @Bean
  @StepScope
  public GoalNewsSitemapReader goalNewsSitemapReader(
      @Value("${goal.article-url}") String sitemapUrl,
      @Value("${goal.limit}") int limit
  ) {
    return new GoalNewsSitemapReader(sitemapUrl, limit);
  }
}
