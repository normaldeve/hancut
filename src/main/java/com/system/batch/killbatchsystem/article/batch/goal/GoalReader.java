package com.system.batch.killbatchsystem.article.batch.goal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;

@Slf4j
@RequiredArgsConstructor
public class GoalReader implements ItemReader<String> {

  private final String espnUrl;
  private final int limit;

  @Override
  public String read() throws Exception {
    return null;
  }
}
