package com.system.batch.killbatchsystem.comment.domain;

import com.system.batch.killbatchsystem.summary.domain.AISummary;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record Comments(
    Long id,
    Long aiSummaryId,
    String author,
    String content,
    LocalDateTime createdAt
) {

  public static Comments createComment(CreateComment c, AISummary a) {
    return Comments.builder()
        .aiSummaryId(a.id())
        .author(c.author())
        .content(c.content())
        .build();
  }
}
