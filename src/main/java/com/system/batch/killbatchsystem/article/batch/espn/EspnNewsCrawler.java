package com.system.batch.killbatchsystem.article.batch.espn;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.batch.killbatchsystem.article.batch.common.NewsCrawler;
import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.model.ArticleSource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Slf4j
@Component(value = "espnNewsCrawler")
public class EspnNewsCrawler implements NewsCrawler {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public List<Article> getArticles(String url, int limit) throws Exception {

    // 1. ESPN JSON 가져오기
    String json = Jsoup.connect(url)
        .ignoreContentType(true)
        .userAgent("Mozilla/5.0")
        .timeout(10000)
        .execute()
        .body();

    JsonNode root = mapper.readTree(json);
    JsonNode articlesNode = root.get("articles");

    List<Article> articles = new ArrayList<>();
    int count = 0;

    if (articlesNode != null && articlesNode.isArray()) {
      for (JsonNode node : articlesNode) {
        if (count >= limit) break;

        // ── (1) 링크 정보
        String link = node.path("links").path("web").path("href").asText(null);
        if (link == null || link.isBlank()) continue;

        // ── (2) 기본 메타
        String articleId = String.valueOf(node.path("id").asLong());
        String pubDateStr = node.path("published").asText(null);
        LocalDateTime publishedAt = parsePublishedAt(pubDateStr);

        String thumbnailUrl = null;
        if (node.has("images") && node.get("images").isArray() && node.get("images").size() > 0) {
          thumbnailUrl = node.get("images").get(0).path("url").asText(null);
        }

        // ── (3) 본문 페이지 크롤링
        String content = fetchArticleBody(link);
        if (content == null || content.isBlank()) {
          log.warn("본문 추출 실패: {}", link);
          continue;
        }

        // ── (4) Article 생성
        Article article = Article.createArticle(
            articleId,
            link,
            content,
            thumbnailUrl,
            ArticleSource.ESPN,
            publishedAt
        );

        articles.add(article);
        count++;
      }
    }

    return articles;
  }

  /** ESPN 기사 본문 가져오기 */
  private String fetchArticleBody(String link) {
    try {
      Document doc = Jsoup.connect(link)
          .userAgent("Mozilla/5.0")
          .referrer("https://www.google.com")
          .timeout(15000)
          .get();

      return doc.select("article p").stream()
          .map(Element::text)
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .distinct()
          .collect(Collectors.joining("\n\n"));

    } catch (Exception e) {
      log.warn("본문 페이지 크롤링 실패: {}", link, e);
      return null;
    }
  }

  // String -> LocalDateTime 변환
  private LocalDateTime parsePublishedAt(String pubDateStr) {
    LocalDateTime publishedAt = LocalDateTime.now();
    if (pubDateStr != null && !pubDateStr.isBlank()) {
      try {
        ZonedDateTime zdt = ZonedDateTime.parse(pubDateStr, DateTimeFormatter.ISO_DATE_TIME);
        publishedAt = zdt.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
      } catch (Exception e) {
        log.warn("날짜 파싱 실패: {}", pubDateStr, e);
      }
    }
    return publishedAt;
  }
}
