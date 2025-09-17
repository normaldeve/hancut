package com.system.batch.killbatchsystem.article.infrastructure.batch.espn;

import com.system.batch.killbatchsystem.article.domain.Article;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component("espnArticleProcessor")
@StepScope
public class EspnArticleProcessor implements ItemProcessor<String, Article> {

  @Override
  public Article process(String url) throws Exception {
    return null;
  }
}
