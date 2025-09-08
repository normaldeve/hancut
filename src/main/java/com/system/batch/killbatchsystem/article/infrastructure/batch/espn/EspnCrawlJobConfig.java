package com.system.batch.killbatchsystem.article.infrastructure.batch.espn;

import com.system.batch.killbatchsystem.article.domain.Article;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class EspnCrawlJobConfig {

  @Bean
  public Job crawlEspnEplJob(
      JobRepository jobRepository,
      Step listToArticleStepForEspn
  ) {
    return new JobBuilder("crawlEspnEplJob", jobRepository)
        .start(listToArticleStepForEspn)
        .build();
  }

  @Bean
  public Step listToArticleStepForEspn(
      JobRepository jobRepository,
      PlatformTransactionManager tx,
      ItemReader<String> espnEplJsonListReader,
      @Qualifier("espnArticleProcessor") ItemProcessor<String, Article> espnArticleProcessor,
      ItemWriter<Article> articleItemWriter // 기존 ArticleItemWriter 재사용
  ) {
    return new StepBuilder("listToArticleStepForEspn", jobRepository)
        .<String, Article>chunk(20, tx)
        .reader(espnEplJsonListReader)
        .processor(espnArticleProcessor)
        .writer(articleItemWriter)
        .faultTolerant()
        .retry(Exception.class).retryLimit(3)
        .skip(Exception.class).skipLimit(50)
        .build();
  }
}
