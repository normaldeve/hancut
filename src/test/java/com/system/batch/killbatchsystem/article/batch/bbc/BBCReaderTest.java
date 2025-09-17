package com.system.batch.killbatchsystem.article.batch.bbc;

import org.assertj.core.api.Assertions;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.article.batch.common.NewsCrawler;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class BBCReaderTest {

  @Test
  @DisplayName("BBC Sports에서 축구 기사 3개 가져오기")
  void bbcReader_getArticles_success() throws Exception {
    // given
    String feed = "https://feeds.bbci.co.uk/sport/football/rss.xml";
    int limit = 3;
    NewsCrawler newsCrawler = new BBCNewsCrawler();
    BBCReader reader = new BBCReader(newsCrawler, feed, limit);

    // when
    List<Article> got = new ArrayList<>();
    for (;;) {
      Article a = reader.read();
      if (a == null) break;
      got.add(a);
    }

    // then
    Assertions.assertThat(got).isNotEmpty();

    Article first = got.get(0);
    Assertions.assertThat(first.url()).isNotBlank();
    Assertions.assertThat(first.content()).isNotBlank();
    Assertions.assertThat(first.publishedAt()).isNotNull();

    System.out.println("Fetched " + got.size() + " articles");
    System.out.println("content: " + first.content());
    System.out.println("articleId: " + first.articleId());
    System.out.println("thumbnail:\n" + first.thumbnailUrl());
  }
}