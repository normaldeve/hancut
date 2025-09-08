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
      @Value("${goal.sitemap-url:https://www.goal.com/en/google-news/page}") String sitemapUrl,
      @Value("${goal.max-items:40}") int maxItems,
      @Value("${goal.recent-hours:48}") int recentHours,
      @Value("${goal.throttleMs:400}") Integer throttleMs
  ) {
    return new GoalNewsSitemapReader(sitemapUrl, maxItems, recentHours, throttleMs);
  }
}
