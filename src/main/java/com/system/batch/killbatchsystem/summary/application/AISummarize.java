package com.system.batch.killbatchsystem.summary.application;

import com.system.batch.killbatchsystem.summary.domain.SummaryContent;

public interface AISummarize {

  SummaryContent summarize(String content);

}
