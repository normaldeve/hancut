package com.system.batch.killbatchsystem.summary.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AISummaryJpaRepositoryImpl extends JpaRepository<AISummaryEntity, Long>, AISummaryQueryRepository {

}
