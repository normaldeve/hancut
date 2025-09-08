package com.system.batch.killbatchsystem.reaction.application;

import com.system.batch.killbatchsystem.reaction.domain.CreateReaction;
import com.system.batch.killbatchsystem.reaction.domain.ReactionResponse;

public interface ReactionService {

  ReactionResponse getReactions(Long aiSummaryId, String userId);

  ReactionResponse toggleReaction(CreateReaction request);
}
