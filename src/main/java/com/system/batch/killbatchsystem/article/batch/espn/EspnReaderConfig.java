package com.system.batch.killbatchsystem.article.batch.espn;

import com.system.batch.killbatchsystem.article.batch.common.NewsCrawler;
import com.system.batch.killbatchsystem.article.domain.Article;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EspnReaderConfig {

  private final NewsCrawler newsCrawler;

  public EspnReaderConfig(@Qualifier("espnNewsCrawler") NewsCrawler newsCrawler) {
    this.newsCrawler = newsCrawler;
  }

  @Bean
  @StepScope
  public ItemReader<Article> espnEPLReader(
      @Value("${article.espn.epl-url}") String apiUrl,
      @Value("${article.limit}") int limit
  ) {
    return new EspnReader(newsCrawler, apiUrl, limit);
  }

  @Bean
  @StepScope
  public ItemReader<Article> espnLaLigaReader(
      @Value("${article.espn.esp-url}") String apiUrl,
      @Value("${article.limit}") int limit
  ) {
    return new EspnReader(newsCrawler, apiUrl, limit);
  }
}
