package com.system.batch.killbatchsystem.summary.domain;

import java.util.List;

public record SummaryContent(
    String title,
    String league,
    String team,
    List<String> summary,
    List<String> keyword
) {

}
