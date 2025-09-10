package com.system.batch.killbatchsystem.article.infrastructure.batch.goal;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.article.infrastructure.batch.common.SummarizeTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
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
  public Job crawlGoalNewsJob(
      Step listToArticleStepForGoal
  ) {
    return new JobBuilder("crawlGoalNewsJob", jobRepository)
        .start(listToArticleStepForGoal)
        .build();
  }

  @Bean
  public Step listToArticleStepForGoal(
      GoalNewsSitemapReader goalNewsSitemapReader,                // Reader: GoalSitemapItem
      ItemProcessor<GoalSitemapItem, Article> goalArticleProcessor, // Processor: sitemap-aware
      ItemWriter<Article> articleItemWriter
  ) {
    return new StepBuilder("listToArticleStepForGoal", jobRepository)
        .<GoalSitemapItem, Article>chunk(20, tx)
        .reader(goalNewsSitemapReader)
        .processor(goalArticleProcessor)
        .writer(articleItemWriter)
        .faultTolerant()
        .retry(Exception.class).retryLimit(3)
        .skip(Exception.class).skipLimit(50)
        .build();
  }

  @Bean
  public Step summarizeStep() {
    return new StepBuilder("goal.summarizeStep", jobRepository)
        .tasklet(summarizeTasklet, tx)
        .build();
  }
}
