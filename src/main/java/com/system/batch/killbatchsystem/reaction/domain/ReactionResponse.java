package com.system.batch.killbatchsystem.reaction.domain;

import lombok.Builder;

@Builder
public record ReactionResponse(
    Long likeCount,
    Long dislikeCount,
    ReactionType reactionType,
    Reaction userReaction
) {

}
