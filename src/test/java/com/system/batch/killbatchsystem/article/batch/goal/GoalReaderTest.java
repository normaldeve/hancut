package com.system.batch.killbatchsystem.article.batch.goal;

import com.system.batch.killbatchsystem.article.batch.common.NewsCrawler;
import com.system.batch.killbatchsystem.article.domain.Article;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class GoalReaderTest {

  @Test
  @DisplayName("Goal Sports에서 축구 기사 3개 가져오기")
  void goalReader_getArticles_success() throws Exception {
    //given
    String feed = "https://www.goal.com/en-gb/google-news/page";
    int limit = 3;
    NewsCrawler crawler = new GoalNewsCrawler();
    GoalReader reader = new GoalReader(crawler, feed, limit);

    //when
    List<Article> articles = new ArrayList<>();
    for (; ; ) {
      Article article = reader.read();
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