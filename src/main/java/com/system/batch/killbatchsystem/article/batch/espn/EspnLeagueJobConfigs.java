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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class EspnLeagueJobConfigs {

  private final SummarizeTasklet summarizeTasklet;
  private final PlatformTransactionManager tx;
  private final JobRepository jobRepository;

  // ================================
  // EPL Job
  // ================================
  @Bean
  public Job crawlEspnEplJob(
      @Qualifier("crawlEspnEplStep") Step crawlEspnEplStep) {
    return new JobBuilder("crawlEspnEplJob", jobRepository)
        .start(crawlEspnEplStep)
        .next(espnSummarizeStep())
        .build();
  }

  @Bean("crawlEspnEplStep")
  public Step crawlEspnEplStep(
      @Qualifier("espnEPLReader") ItemReader<Article> reader,
      @Qualifier("articleItemWriter") ItemWriter<Article> writer) {
    return new StepBuilder("crawlEspnEplStep", jobRepository)
        .<Article, Article>chunk(20, tx)
        .reader(reader)
        .writer(writer)
        .faultTolerant()
        .retry(Exception.class).retryLimit(3)
        .skip(Exception.class).skipLimit(50)
        .build();
  }

  // ================================
  // La Liga Job
  // ================================
  @Bean
  public Job crawlEspnLaLigaJob(
      @Qualifier("crawlEspnLaLigaStep") Step crawlEspnLaLigaStep) {
    return new JobBuilder("crawlEspnLaLigaJob", jobRepository)
        .start(crawlEspnLaLigaStep)
        .next(espnSummarizeStep())
        .build();
  }

  @Bean("crawlEspnLaLigaStep")
  public Step crawlEspnLaLigaStep(
      @Qualifier("espnLaLigaReader") ItemReader<Article> reader,
      @Qualifier("articleItemWriter") ItemWriter<Article> writer) {
    return new StepBuilder("crawlEspnLaLigaStep", jobRepository)
        .<Article, Article>chunk(20, tx)
        .reader(reader)
        .writer(writer)
        .faultTolerant()
        .retry(Exception.class).retryLimit(3)
        .skip(Exception.class).skipLimit(50)
        .build();
  }

  @Bean
  public Step espnSummarizeStep() {
    return new StepBuilder("espn.summarizeStep", jobRepository)
        .tasklet(summarizeTasklet, tx)
        .build();
  }
}
