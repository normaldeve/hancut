package com.system.batch.killbatchsystem.reaction.infrastructure;

import com.system.batch.killbatchsystem.reaction.domain.Reaction;
import com.system.batch.killbatchsystem.reaction.domain.ReactionType;
import com.system.batch.killbatchsystem.reaction.infrastructure.jpa.ReactionEntity;
import com.system.batch.killbatchsystem.reaction.infrastructure.jpa.ReactionJpaRepository;
import com.system.batch.killbatchsystem.reaction.application.ReactionRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReactionRepositoryImpl implements ReactionRepository {

  private final ReactionJpaRepository reactionJpaRepository;
  private final JpaContext jpaContext;

  @Override
  public List<Reaction> findByAiSummaryId(Long aiSummaryId) {
    return reactionJpaRepository.findByAiSummaryId(aiSummaryId).stream()
        .map(ReactionEntity::toModel)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Reaction> findByAiSummaryIdAndUserId(Long aiSummaryId, String userId) {
    return reactionJpaRepository.findByAiSummaryIdAndUserId(aiSummaryId, userId)
        .map(ReactionEntity::toModel);
  }

  @Override
  public void delete(Reaction reaction) {
    ReactionEntity reactionEntity = ReactionEntity.fromModel(reaction);

    reactionJpaRepository.deleteByAiSummaryIdAndUserId(reactionEntity.getAiSummaryId(), reactionEntity.getUserId());

  }

  @Override
  public void save(Reaction reaction) {
    ReactionEntity reactionEntity = ReactionEntity.fromModel(reaction);

    reactionJpaRepository.save(reactionEntity);
  }

  @Override
  public void updateType(ReactionType type, Long aiSummaryId, String userId) {
    ReactionEntity reactionEntity = reactionJpaRepository.findByAiSummaryIdAndUserId(aiSummaryId, userId)
        .orElseThrow(() -> new RuntimeException("Can not found reaction"));

    reactionEntity.changeType(type);
  }

  @Override
  public long countByAiSummaryIdAndType(Long aiSummaryId, ReactionType type) {
    return reactionJpaRepository.countByAiSummaryIdAndType(aiSummaryId, type);
  }
}
