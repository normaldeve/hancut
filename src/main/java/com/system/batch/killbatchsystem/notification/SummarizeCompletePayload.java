package com.system.batch.killbatchsystem.notification;

public record SummarizeCompletePayload(
    int count,
    String finishedAt
) {

}
