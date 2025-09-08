package com.system.batch.killbatchsystem.comment.domain;

import java.time.LocalDateTime;

public record CommentCursor(
    LocalDateTime createdAt,
    Long id
) {

}
