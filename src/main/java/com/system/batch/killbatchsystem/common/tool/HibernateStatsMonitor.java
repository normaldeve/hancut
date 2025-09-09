package com.system.batch.killbatchsystem.common.tool;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HibernateStatsMonitor {

  @Autowired
  private EntityManagerFactory entityManagerFactory;

  public void startMeasurement() {
    Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    stats.setStatisticsEnabled(true);
    stats.clear();
  }

  public void endMeasurement(String operation) {
    Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

    long queryCount = stats.getQueryExecutionCount();

    log.info("[PERF] {}: {}개 쿼리 실행", operation, queryCount);

    stats.clear();
  }
}

