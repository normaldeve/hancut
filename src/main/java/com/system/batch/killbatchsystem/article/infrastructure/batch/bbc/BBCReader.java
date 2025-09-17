package com.system.batch.killbatchsystem.article.infrastructure.batch.bbc;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.article.infrastructure.batch.common.ArticleSource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.batch.item.ItemReader;

@Slf4j
@RequiredArgsConstructor
public class BBCReader implements ItemReader<Article> {

  private final String bbcUrl;
  private final int limit;

  private Iterator<Article> articleIterator;

  @Override
  public Article read() throws Exception {
    if (articleIterator == null) {
      List<Article> articles = getArticles();
      articleIterator = articles.iterator();
    }

    if (articleIterator.hasNext()) {
      return articleIterator.next();
    } else {
      return null;
    }
  }

  private List<Article> getArticles() throws Exception {

    Document rss = Jsoup.connect(bbcUrl)
        .userAgent("Mozilla/5.0")
        .parser(Parser.xmlParser())
        .timeout(100000)
        .get();

    List<Article> articles = new ArrayList<>();
    Elements item = rss.select("item");
    int count = 0;

    for (Element element : item) {
      if (count >= limit) {
        break;
      }
      // 원본 기사 링크
      String link = text(element.selectFirst("guid"));

      // 기사 id
      String articleId = null;
      if (link != null && !link.isBlank()) {
        articleId = link.substring(link.lastIndexOf("/") + 1);

        // 뒤에 # 붙어 있는 부분 제거
        int hashIndex = articleId.indexOf('#');
        if (hashIndex != -1) {
          articleId = articleId.substring(0, hashIndex);
        }
      }

      // 발행 일자를 LocalDateTime으로 변환하고 한국 날짜로 저장
      String pubDateStr = text(element.selectFirst("pubDate"));
      LocalDateTime publishedAt = LocalDateTime.now();
      if (pubDateStr != null && !pubDateStr.isBlank()) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(pubDateStr,
            DateTimeFormatter.RFC_1123_DATE_TIME);
        publishedAt = zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
      } else {
        log.warn("발행 시간을 찾을 수 없습니다: {}", element);
      }

      // 썸네일 이미지 URL
      String thumbnailUrl = attr(element.selectFirst("media|thumbnail")); // 링크 형식

      // 만일 원본 링크가 없다면 본문을 가져올 수 없다
      if (link == null || link.isBlank()) {
        continue;
      }

      Document doc = Jsoup.connect(link)
          .userAgent("Mozilla/5.0")
          .referrer("https://www.google.com")
          .timeout(15000)
          .get();

      String content = doc.select("article p").stream()
          .filter(p ->
              p.closest("aside, figure, [data-component=links-block], [data-component=image-block]")
                  == null)
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
          ArticleSource.BBC,
          publishedAt
      );

      articles.add(article);
      count++;
    }

    return articles;
  }

  // text 가져오기
  private String text(Element element) {
    return element == null ? null : element.text();
  }

  // attribute 가져오기
  private String attr(Element element) {
    return element == null ? null : element.attr("url");
  }
}
