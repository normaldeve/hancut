package com.system.batch.killbatchsystem.reaction.infrastructure.jpa;

import com.system.batch.killbatchsystem.reaction.domain.ReactionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReactionJpaRepository extends JpaRepository<ReactionEntity, Long> {

  List<ReactionEntity> findByAiSummaryId(Long aiSummaryId);

  Optional<ReactionEntity> findByAiSummaryIdAndUserId(Long aiSummaryId, String userId);

  void deleteByAiSummaryIdAndUserId(Long aiSummaryId, String userId);

  @Query("SELECT COUNT(r) FROM ReactionEntity r WHERE r.aiSummaryId = :aiSummaryId AND r.type = :type")
  long countByAiSummaryIdAndType(@Param("aiSummaryId") Long aiSummaryId, @Param("type") ReactionType type);
}
