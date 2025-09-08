package com.system.batch.killbatchsystem.reaction.domain;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record Reaction(
    Long id,
    Long aiSummaryId,
    String userId,
    ReactionType type,
    LocalDateTime createdAt
) {

  public static Reaction createReaction(CreateReaction c) {
    return Reaction.builder()
        .aiSummaryId(c.aiSummaryId())
        .userId(c.userId())
        .type(c.type())
        .build();
  }
}
