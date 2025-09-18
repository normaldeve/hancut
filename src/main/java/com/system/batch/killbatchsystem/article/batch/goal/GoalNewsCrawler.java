package com.system.batch.killbatchsystem.article.batch.goal;

import com.system.batch.killbatchsystem.article.batch.common.ArticleSource;
import com.system.batch.killbatchsystem.article.batch.common.NewsCrawler;
import com.system.batch.killbatchsystem.article.domain.Article;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

/**
 * TODO Selenium 사용하여 크롤링 하기
 */
@Slf4j
@Component(value = "goalNewsCrawler")
public class GoalNewsCrawler implements NewsCrawler {

  @Override
  public List<Article> getArticles(String url, int limit) throws Exception {

    Document rss = Jsoup.connect(url)
        .userAgent("Mozilla/5.0")
        .parser(Parser.xmlParser())
        .timeout(100000)
        .get();

    List<Article> articles = new ArrayList<>();
    Elements items = rss.select("url");
    int count = 0;

    for (Element element : items) {
      if (count >= limit) {
        break;
      }

      String link = text(element.selectFirst("loc"));

      String articleId = null;
      if (link != null && !link.isBlank()) {
        articleId = link.substring(link.lastIndexOf("/") + 1);
      }

      String pubDateStr = text(element.selectFirst("news|publication_date"));
      LocalDateTime publishedAt = LocalDateTime.now();
      if (pubDateStr != null && !pubDateStr.isBlank()) {
        try {
          ZonedDateTime zdt = ZonedDateTime.parse(pubDateStr, DateTimeFormatter.ISO_DATE_TIME);
          publishedAt = zdt.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        } catch (Exception e) {
          log.warn("날짜 파싱 실패: {}", pubDateStr, e);
        }
      }

      String thumbnailUrl = text(element.selectFirst("image|loc"));

      if (link == null || link.isBlank()) continue;

      Document doc = Jsoup.connect(link)
          .userAgent("Mozilla/5.0")
          .referrer("https://www.google.com")
          .timeout(15000)
          .get();

      if (!link.contains("lists")) continue;

      String content = doc.select("article p").stream()
          .map(Element::text)
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .distinct()
          .collect(Collectors.joining("\n\n"));

      Article article = Article.createArticle(
          articleId,
          link,
          content,
          thumbnailUrl,
          ArticleSource.GOAL_ENG,
          publishedAt
      );

      articles.add(article);
      count++;
    }

    return articles;
  }

  private String text(Element element) {
    return element == null ? null : element.text();
  }
}
