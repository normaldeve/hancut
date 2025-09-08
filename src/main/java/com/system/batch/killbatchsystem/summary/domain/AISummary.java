package com.system.batch.killbatchsystem.summary.domain;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record AISummary(
    Long id,
    String title,
    String league,
    String team,
    List<String> summary,
    List<String> keyword,
    LocalDateTime publishedAt,
    String thumbnailUrl,
    String url,
    String sourceName
) {

  public static AISummary createSummary(SummaryContent s, CreateSummary c) {
    return AISummary.builder()
        .title(s.title())
        .league(s.league())
        .team(s.team())
        .summary(s.summary())
        .keyword(s.keyword())
        .publishedAt(c.publishedAt())
        .thumbnailUrl(c.thumbnailUrl())
        .url(c.url())
        .sourceName(c.sourceName())
        .build();
  }
}
