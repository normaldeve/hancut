package com.system.batch.killbatchsystem.article.batch.espn;

import com.system.batch.killbatchsystem.article.batch.common.NewsCrawler;
import com.system.batch.killbatchsystem.article.domain.Article;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class EspnReaderTest {

  @Test
  @DisplayName("ESPN Sports에서 축구 기사 1개 가져오기")
  void espnReader_getArticles_success() throws Exception {
    //given
    String feed = "https://site.api.espn.com/apis/site/v2/sports/soccer/eng.1/news";
    int limit = 1;
    NewsCrawler newsCrawler = new EspnNewsCrawler();
    EspnReader espnReader = new EspnReader(newsCrawler, feed, limit);

    //when
    List<Article> articles = new ArrayList<>();
    for (; ; ) {
      Article article = espnReader.read();
      if (article == null){ break;}
      articles.add(article);
    }

    //then
    Assertions.assertThat(articles).hasSize(limit);

    Article first = articles.get(0);
    Assertions.assertThat(first.url()).isNotBlank();
    Assertions.assertThat(first.content()).isNotBlank();
    Assertions.assertThat(first.publishedAt()).isNotNull();

    System.out.println("Fetched: " + articles.size() + " articles");
    System.out.println("content: " + first.content());
    System.out.println("articleId: " + first.articleId());
    System.out.println("thumbnail:\n" + first.thumbnailUrl());
  }
}