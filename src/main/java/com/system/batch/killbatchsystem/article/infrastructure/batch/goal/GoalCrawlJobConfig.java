package com.system.batch.killbatchsystem.article.infrastructure.batch.goal;

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
public class GoalCrawlJobConfig {

  private final SummarizeTasklet summarizeTasklet;
  private final PlatformTransactionManager tx;
  private final JobRepository jobRepository;

  @Bean
  public Job crawlGoalJob(
      Step crawlGoalStep
  ) {
    return new JobBuilder("crawlGoalJob", jobRepository)
        .start(crawlGoalStep)
        .next(goalSummarizeStep())
        .build();
  }

  @Bean
  public Step crawlGoalStep(
      ItemReader<String> goalReader,
      ItemWriter<Article> articleItemWriter
  ) {
    return new StepBuilder("crawlGoalStep", jobRepository)
        .<String, Article>chunk(20, tx)
        .reader(goalReader)
        .writer(articleItemWriter)
        .faultTolerant()
        .retry(Exception.class).retryLimit(3)
        .skip(Exception.class).skipLimit(50)
        .build();
  }

  @Bean
  public Step goalSummarizeStep() {
    return new StepBuilder("goal.summarizeStep", jobRepository)
        .tasklet(summarizeTasklet, tx)
        .build();
  }
}
