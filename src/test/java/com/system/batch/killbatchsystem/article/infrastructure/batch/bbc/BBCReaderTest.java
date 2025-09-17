package com.system.batch.killbatchsystem.article.infrastructure.batch.bbc;

import com.system.batch.killbatchsystem.article.domain.Article;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class BBCReaderTest {

  @Test
  void bbcReader_fetchAndParse_shouldReturnArticles() throws Exception {
    // given
    String feed = "https://feeds.bbci.co.uk/sport/football/rss.xml";
    int limit = 3;
    BBCReader reader = new BBCReader(feed, limit);

    // when
    List<Article> got = new ArrayList<>();
    for (;;) {
      Article a = reader.read();
      if (a == null) break;
      got.add(a);
    }

    // then
    assertThat(got).isNotEmpty();

    Article first = got.get(0);
    assertThat(first.url()).isNotBlank();
    assertThat(first.content()).isNotBlank();
    assertThat(first.publishedAt()).isNotNull();

    System.out.println("Fetched " + got.size() + " articles");
    System.out.println("content: " + first.content());
    System.out.println("articleId: " + first.articleId());
    System.out.println("thumbnail:\n" + first.thumbnailUrl());
  }
}