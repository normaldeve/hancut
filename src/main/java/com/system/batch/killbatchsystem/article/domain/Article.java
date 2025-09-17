package com.system.batch.killbatchsystem.article.domain;

import com.system.batch.killbatchsystem.article.batch.common.ArticleSource;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record Article(
    Long id,
    String articleId,
    String url,
    String content,
    String thumbnailUrl,
    ArticleSource sourceName,
    LocalDateTime publishedAt,
    LocalDateTime createdAt,
    SummarizeStatus summarizeStatus
) {

  public static Article createArticle(String articleId, String url, String content,
      String thumbnailUrl, ArticleSource sourceName, LocalDateTime publishedAt) {
    return Article.builder()
        .articleId(articleId)
        .url(url)
        .content(content)
        .thumbnailUrl(thumbnailUrl)
        .sourceName(sourceName)
        .publishedAt(publishedAt)
        .summarizeStatus(SummarizeStatus.PENDING)
        .build();
  }

}
