package com.system.batch.killbatchsystem.summary.domain;

import com.system.batch.killbatchsystem.article.batch.common.ArticleSource;
import java.time.LocalDateTime;

public record CreateSummary(
    String content,
    LocalDateTime publishedAt,
    String thumbnailUrl,
    String url,
    ArticleSource sourceName
) {

}
