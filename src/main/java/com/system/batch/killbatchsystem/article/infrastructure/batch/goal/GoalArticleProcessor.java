package com.system.batch.killbatchsystem.article.infrastructure.batch.goal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.batch.killbatchsystem.article.domain.Article;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;

@Slf4j
@Component("goalArticleProcessor")
@StepScope
public class GoalArticleProcessor implements ItemProcessor<GoalSitemapItem, Article> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Value("${goal.min-content-length:180}")
  private int minContentLength;

  @Override
  public Article process(GoalSitemapItem item) throws Exception {
    final String url = item.getUrl();
    log.info("[GOAL-Detail] GET {}", url);

    if (url.contains("/video/") || url.contains("/videos/")) {
      log.info("Skip video url={}", url);
      return null;
    }

    Document doc = Jsoup.connect(url)
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        .referrer("https://www.google.com/")
        .timeout(12_000)
        .followRedirects(true)
        .get();

    // 메타
    String description = meta(doc, "meta[property=og:description]");
    String thumbnail = firstNonBlank(
        meta(doc, "meta[property=og:image]"),
        meta(doc, "meta[name=twitter:image]"),
        item.getImageUrl()
    );
    String sourceName = firstNonBlank(
        meta(doc, "meta[property=og:site_name]"),
        host(url)
    );
    String publishedRaw = firstNonBlank(
        meta(doc, "meta[property=article:published_time]"),
        meta(doc, "meta[name=article:published_time]"),
        meta(doc, "meta[name=date]"),
        meta(doc, "meta[itemprop=datePublished]"),
        toIsoOrNull(item.getPublishedAt()) // sitemap 값 폴백
    );

    // og:type 검사(있으면)
    String ogType = meta(doc, "meta[property=og:type]");
    if (ogType != null && !"article".equalsIgnoreCase(ogType.trim())) {
      log.info("Skip non-article by og:type={} url={}", ogType, url);
      return null;
    }

    LocalDateTime publishedAt = parseDateTimeSafe(publishedRaw);

    // 본문 추출(일반 → JSON-LD → AMP → description)
    String content = extractContentFromBody(doc);
    if (isBlank(content)) content = tryJsonLdArticleBody(doc);
    if (isBlank(content)) content = fetchAmpContent(url);
    if (isBlank(content)) content = description;

    if (isBlank(thumbnail)) {
      log.warn("Skip GOAL article (no thumbnail) {}", url);
      return null;
    }
    if (isBlank(content) || content.length() < minContentLength) {
      log.info("Skip short/empty content ({} chars): {}", (content==null?0:content.length()), url);
      return null;
    }

    String goalId = extractGoalIdFromUrl(url);
    if (isBlank(goalId)) {
      // fallback: url 해시라도 사용
      goalId = Integer.toHexString(url.getBytes(StandardCharsets.UTF_8).hashCode());
    }

    return Article.createArticle(
        goalId,
        url,
        content,
        thumbnail,
        sourceName,
        (publishedAt != null ? publishedAt : LocalDateTime.now(ZoneId.of("Asia/Seoul")))
    );
  }

  /** 가장 신뢰도 높은 본문 추출 */
  private String extractContentFromBody(Document doc) {
    // 1) data-testid 우선 + 해시형 클래스 폴백
    Element body = doc.selectFirst(
        "[data-testid=article-body], div[class*=article-body_body__], article .article-body, article"
    );
    String fromBody = collectParagraphsOnly(body);
    if (!isBlank(fromBody) && fromBody.length() >= 200) return fromBody;

    // 2) 리스트 기사(랭킹/평점/모음글) 폴백
    Element list = doc.selectFirst(
        "[data-component='article-body'], [data-testid=story-feed], ol, ul, .article-list, .list-article"
    );
    String fromList = collectListItems(list);
    if (!isBlank(fromList) && fromList.length() >= 160) return fromList;

    return null;
  }

  /** 일반 기사: p, li만 수집(헤딩 제외), 링크만 있는 문단 제외, 중복 줄 제거 */
  private String collectParagraphsOnly(Element root) {
    if (root == null) return null;
    root.select("script, style, noscript, .ad, .ad-slot, .advertisement, [aria-label=advertisement]").remove();

    var parts = new ArrayList<String>();
    root.select("p, li").forEach(el -> {
      String t = el.text();
      boolean onlyLink = !el.select("a").isEmpty() && t.equals(el.select("a").text());
      if (!isBlank(t) && !onlyLink) parts.add(t.trim());
    });

    if (parts.isEmpty()) return null;
    var dedup = new LinkedHashSet<>(parts);
    String joined = String.join("\n", dedup).trim();
    return isBlank(joined) ? null : joined;
  }

  /** 리스트 기사: 소제목+설명 묶음, 제목만 있으면 스킵 */
  private String collectListItems(Element root) {
    if (root == null) return null;
    root.select("script, style, noscript, .ad, .ad-slot, .advertisement, [aria-label=advertisement]").remove();

    var out = new StringBuilder();
    int idx = 1;

    var items = root.select("ol > li, ul > li, .article-list > li, .list-article > li, [data-component='list-item'], .card");
    if (items.isEmpty()) items = root.select("h2, h3"); // 헤딩 경계 폴백

    for (Element item : items) {
      String heading = firstNonBlank(
          textOrNull(item.selectFirst("h2")),
          textOrNull(item.selectFirst("h3")),
          textOrNull(item.selectFirst("strong"))
      );
      var descParts = new ArrayList<String>();
      item.select("p, li").forEach(el -> {
        String t = el.text();
        if (!isBlank(t)) descParts.add(t.trim());
      });

      // 헤딩만 있고 설명이 전혀 없으면 스킵
      if (isBlank(heading) && descParts.isEmpty()) continue;
      if (isBlank(heading) && !descParts.isEmpty()) {
        heading = descParts.remove(0); // 첫 문장을 제목으로
      }

      out.append(idx++).append(". ").append(heading == null ? "" : heading).append("\n");
      if (!descParts.isEmpty()) out.append(String.join("\n", descParts)).append("\n");
      out.append("\n");
    }
    String s = out.toString().trim();
    return isBlank(s) ? null : s;
  }

  private String tryJsonLdArticleBody(Document doc) {
    try {
      for (Element s : doc.select("script[type=application/ld+json]")) {
        String json = s.data();
        if (isBlank(json)) continue;
        JsonNode root = MAPPER.readTree(json);
        if (root.isArray()) {
          for (JsonNode n : root) {
            String body = n.path("articleBody").asText(null);
            if (!isBlank(body)) return body;
          }
        } else {
          String body = root.path("articleBody").asText(null);
          if (!isBlank(body)) return body;
        }
      }
    } catch (Exception ignore) {}
    return null;
  }

  private String fetchAmpContent(String url) {
    String ampUrl = toAmpUrl(url);
    try {
      Document amp = Jsoup.connect(ampUrl)
          .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
          .referrer("https://www.google.com/")
          .timeout(10000)
          .get();

      Element body = amp.selectFirst("article, .article-body, .article__body, [data-testid=article-body], .story-body");
      if (body != null) {
        body.select("script, style, noscript, .ad, .ad-slot, .advertisement, [aria-label=advertisement]").remove();
        var parts = new ArrayList<String>();
        body.select("p, li, h2, h3, h4").forEach(el -> {
          String t = el.text();
          if (!isBlank(t)) parts.add(t.trim());
        });
        String joined = String.join("\n", parts).trim();
        if (!isBlank(joined)) return joined;
      }
    } catch (Exception e) {
      log.debug("AMP fetch failed: {}", ampUrl, e);
    }
    return null;
  }

  private String toAmpUrl(String url) {
    try {
      URI uri = URI.create(url);
      String host = uri.getHost();
      String path = uri.getPath();
      String ampBase = "https://" + host + (path.endsWith("/") ? path + "amp" : path + "/amp");
      String q = uri.getQuery();
      return (q == null) ? ampBase : ampBase + "?" + q;
    } catch (Exception e) {
      return url.contains("?") ? url + "&outputType=amp" : url + "?outputType=amp";
    }
  }

  private String meta(Document doc, String selector) {
    Element e = doc.selectFirst(selector);
    if (e == null) return null;
    String c = e.attr("content");
    return isBlank(c) ? null : c;
  }

  private String firstNonBlank(String... arr) {
    if (arr == null) return null;
    for (String s : arr) if (!isBlank(s)) return s;
    return null;
  }

  private String textOrNull(Element e) {
    if (e == null) return null;
    String t = e.text();
    return isBlank(t) ? null : t.trim();
  }

  private String host(String url) {
    try { return URI.create(url).getHost(); } catch (Exception e) { return null; }
  }

  private String toIsoOrNull(OffsetDateTime odt) {
    return (odt == null) ? null : odt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }

  private LocalDateTime parseDateTimeSafe(String raw) {
    if (isBlank(raw)) return null;
    try {
      // ISO-8601 문자열을 UTC 기준으로 파싱
      return LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME)
          .atOffset(ZoneOffset.UTC)
          .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
          .toLocalDateTime();
    } catch (Exception ignore) {}
    try {
      String fixed = raw.replaceFirst("(\\+|\\-)(\\d{2}):(\\d{2})$", "$1$2$3"); // +09:00 → +0900
      return LocalDateTime.parse(fixed, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"))
          .atZone(ZoneOffset.UTC)
          .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
          .toLocalDateTime();
    } catch (Exception ignore) {}
    return null;
  }

  private static boolean isBlank(String s) { return s == null || s.isBlank(); }

  /** URL의 마지막 blt... 세그먼트를 goalId로 사용 */
  private String extractGoalIdFromUrl(String url) {
    try {
      URI uri = URI.create(url);
      String[] seg = uri.getPath().split("/");
      if (seg.length == 0) return null;
      String last = seg[seg.length - 1];
      if (last.startsWith("blt") && last.length() >= 10) {
        return last;
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }
}
