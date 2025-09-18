package com.system.batch.killbatchsystem.article.batch.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlSchedulers {

  private final JobLauncher jobLauncher;
  private final Job crawlBBCJob;
  private final Job crawlEspnEplJob;
  private final Job crawlGoalJob;

  @Scheduled(cron = "${scheduler.bbc.cron}", zone = "Asia/Seoul")
  public void runBBC() {
    run(crawlBBCJob, "bbc");
  }

  @Scheduled(cron = "${scheduler.goal.cron}", zone = "Asia/Seoul")
  public void runGoal() {
    run(crawlGoalJob, "goal");
  }

  @Scheduled(cron = "${scheduler.espn.cron}", zone = "Asia/Seoul")
  public void runEspn() {
    run(crawlEspnEplJob, "goal");
  }

  private void run(Job job, String tag) {
    try {
      JobParameters params = new JobParametersBuilder()
          .addLong("timestamp", System.currentTimeMillis())
          .addString("source", tag)
          .toJobParameters();

      jobLauncher.run(job, params);
      log.info("{} 크롤링 작업이 성공했습니다", tag);
    } catch (Exception e) {
      log.error("{} 크롤링 작업이 실패했습니다.", tag, e);
    }
  }
}
