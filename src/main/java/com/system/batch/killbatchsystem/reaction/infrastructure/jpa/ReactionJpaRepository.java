package com.system.batch.killbatchsystem.reaction.infrastructure.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionJpaRepository extends JpaRepository<ReactionEntity, Long> {

  List<ReactionEntity> findByAiSummaryId(Long aiSummaryId);

  Optional<ReactionEntity> findByAiSummaryIdAndUserId(Long aiSummaryId, String userId);

  void deleteByAiSummaryId(Long aiSummaryId);
}
