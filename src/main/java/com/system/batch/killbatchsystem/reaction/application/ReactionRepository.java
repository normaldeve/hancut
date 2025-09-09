package com.system.batch.killbatchsystem.reaction.application;

import com.system.batch.killbatchsystem.reaction.domain.Reaction;
import com.system.batch.killbatchsystem.reaction.domain.ReactionType;
import java.util.List;
import java.util.Optional;

public interface ReactionRepository {

  List<Reaction> findByAiSummaryId(Long aiSummaryId);

  Optional<Reaction> findByAiSummaryIdAndUserId(Long aiSummaryId, String userId);

  void delete(Reaction reaction);

  void save(Reaction reaction);

  void updateType(ReactionType type, Long aiSummaryId, String userId);
}
