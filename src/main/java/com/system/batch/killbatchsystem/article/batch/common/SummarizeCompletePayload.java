package com.system.batch.killbatchsystem.article.batch.common;

import com.system.batch.killbatchsystem.model.ArticleSource;

public record SummarizeCompletePayload(
    ArticleSource articleSource,
    int count,
    String finishedAt
) {

}
