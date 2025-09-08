package com.system.batch.killbatchsystem.reaction.domain;

import jakarta.validation.constraints.NotNull;

public record CreateReaction(
    @NotNull(message = "ai_summary_id는 필수입니다")
    Long aiSummaryId,

    String userId,

    @NotNull(message = "type 입력은 필수입니다")
    ReactionType type
    ) {

  public CreateReaction changeUserId(String userId) {
    return new CreateReaction(this.aiSummaryId, userId, this.type);
  }
}
