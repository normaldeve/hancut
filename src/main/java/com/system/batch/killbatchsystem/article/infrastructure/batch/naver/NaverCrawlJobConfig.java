package com.system.batch.killbatchsystem.article.infrastructure.batch.naver;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.article.infrastructure.batch.common.SummarizeTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
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
@RequiredArgsConstructor
public class NaverCrawlJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager tx;
  private final SummarizeTasklet summarizeTasklet;

  @Bean
  public Job crawlNaverSoccerJob(Step listToArticleStep) {
    return new JobBuilder("crawlNaverSoccerJob", jobRepository)
        .start(listToArticleStep)
        .next(naverSummarizeStep())
        .build();
  }

  @Bean
  public Step listToArticleStep(ItemReader<String> naverSoccerJsonListReader,
      @Qualifier("naverArticleProcessor") ItemProcessor<String, Article> articleProcessor,
      ItemWriter<Article> articleItemWriter) {
    return new StepBuilder("listToArticleStep", jobRepository)
        .<String, Article>chunk(20, tx)
        .reader(naverSoccerJsonListReader)
        .processor(articleProcessor)
        .writer(articleItemWriter)
        .listener(new StepExecutionListenerSupport() {
          @Override
          public ExitStatus afterStep(org.springframework.batch.core.StepExecution se) {
            int r = (int) se.getReadCount(), w = (int) se.getWriteCount();
            // 읽은 URL이 0이면 실패로 종료(빠른 인지)
            return (r == 0) ? ExitStatus.FAILED : se.getExitStatus();
          }
        })
        .faultTolerant()
        .retry(Exception.class).retryLimit(3)
        .skip(Exception.class).skipLimit(50)
        .build();
  }

  @Bean
  public Step naverSummarizeStep() {
    return new StepBuilder("naver.summarizeStep", jobRepository)
        .tasklet(summarizeTasklet, tx)
        .build();
  }
}
