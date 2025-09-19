package com.system.batch.killbatchsystem.article.batch.espn;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.article.batch.common.SummarizeTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class EspnCrawlJobConfig {

  private final SummarizeTasklet summarizeTasklet;
  private final PlatformTransactionManager tx;
  private final JobRepository jobRepository;

  @Bean
  public Job crawlEspnEplJob(
      Step listToArticleStepForEspn,
      Step goalSummarizeStep) {
    return new JobBuilder("crawlEspnEplJob", jobRepository)
        .start(listToArticleStepForEspn)
        .next(espnSummarizeStep())
        .build();
  }

  @Bean
  public Step listToArticleStepForEspn(
      ItemReader<Article> espnReader,
      ItemWriter<Article> articleItemWriter
  ) {
    return new StepBuilder("listToArticleStepForEspn", jobRepository)
        .<Article, Article>chunk(20, tx)
        .reader(espnReader)
        .writer(articleItemWriter)
        .build();
  }

  @Bean
  public Step espnSummarizeStep() {
    return new StepBuilder("espn.summarizeStep", jobRepository)
        .tasklet(summarizeTasklet, tx)
        .build();
  }
}
