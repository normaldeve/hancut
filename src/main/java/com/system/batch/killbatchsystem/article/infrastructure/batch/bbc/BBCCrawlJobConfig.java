package com.system.batch.killbatchsystem.article.infrastructure.batch.bbc;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.article.infrastructure.batch.common.SummarizeTasklet;
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
public class BBCCrawlJobConfig {

  private final PlatformTransactionManager tx;
  private final JobRepository jobRepository;
  private final SummarizeTasklet summarizeTasklet;

  @Bean
  public Job crawlBBCJob(
      Step crawlBBCStep
  ) {
    return new JobBuilder("crawlBBCJob", jobRepository)
        .start(crawlBBCStep)
        .next(bbcSummarizeStep())
        .build();
  }

  @Bean
  public Step crawlBBCStep(
      ItemReader<Article> bbcReader,
      ItemWriter<Article> bbcWriter
  ) {
    return new StepBuilder("crawlBBCStep", jobRepository)
        .<Article, Article>chunk(20, tx)
        .reader(bbcReader)
        .writer(bbcWriter)
        .faultTolerant()
        .retry(Exception.class).retryLimit(3)
        .skip(Exception.class).skipLimit(50)
        .build();
  }

  @Bean
  public Step bbcSummarizeStep() {
    return new StepBuilder("bbc.summarizeStep", jobRepository)
        .tasklet(summarizeTasklet, tx)
        .build();
  }

}
