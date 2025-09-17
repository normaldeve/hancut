package com.system.batch.killbatchsystem.summary.domain;

import com.system.batch.killbatchsystem.article.infrastructure.batch.common.ArticleSource;
import java.time.LocalDateTime;
import java.util.List;

public record GetAISummary(
    Long id,
    String title,
    List<String> summary,
    List<String> keyword,
    LocalDateTime publishedAt,
    String league,
    String team,
    String thumbnailUrl,
    String url,
    ArticleSource sourceName
) {

  public static GetAISummary fromArticle(AISummary aiSummary) {
    return new GetAISummary(
        aiSummary.id(),
        aiSummary.title(),
        aiSummary.summary(),
        aiSummary.keyword(),
        aiSummary.publishedAt(),
        aiSummary.league(),
        aiSummary.team(),
        aiSummary.thumbnailUrl(),
        aiSummary.url(),
        aiSummary.sourceName()
    );
  }
}
