package com.system.batch.killbatchsystem.article.batch.goal;

import com.system.batch.killbatchsystem.article.batch.common.NewsCrawler;
import com.system.batch.killbatchsystem.article.domain.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@RequiredArgsConstructor
public class GoalReaderConfig {

  @Qualifier("goalNewsCrawler")
  private final NewsCrawler newsCrawler;

  @Bean
  @StepScope
  public ItemReader<Article> goalReader(
      @Value("${article.goal.en-url}") String goalKrUrl,
      @Value("${article.limit}") int limit
  ) {
    return new GoalReader(newsCrawler, goalKrUrl, limit);
  }
}
