package com.system.batch.killbatchsystem.summary.domain;

import java.time.LocalDateTime;

public record CreateSummary(
    String content,
    LocalDateTime publishedAt,
    String thumbnailUrl,
    String url,
    String sourceName
) {

}
