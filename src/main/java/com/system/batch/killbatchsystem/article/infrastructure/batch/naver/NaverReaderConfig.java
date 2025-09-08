package com.system.batch.killbatchsystem.article.infrastructure.batch.naver;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NaverReaderConfig {

  @Bean
  @StepScope
  public ItemReader<String> naverSoccerJsonListReader(
      @Value("#{jobParameters['targetDate']}") String targetDate,
      @Value("#{jobParameters['maxPages']}") Integer maxPages,
      @Value("#{jobParameters['league']}") String league,
      @Value("${crawler.list.wfootball}") String wfootballUrl,
      @Value("${crawler.throttleMs}") Integer throttleMs
  ) {
    String date = (targetDate == null ? "" : targetDate.replace("-", ""));
    return new NaverSoccerJsonListReader(
        wfootballUrl, date, (maxPages == null ? 1 : maxPages),
        (throttleMs == null ? 200 : throttleMs),
        league
    );
  }
}
