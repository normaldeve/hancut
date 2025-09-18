package com.system.batch.killbatchsystem.notification;

import com.system.batch.killbatchsystem.article.batch.common.ArticleSource;

public record SummarizeCompletePayload(
    ArticleSource articleSource,
    int count,
    String finishedAt
) {

}
