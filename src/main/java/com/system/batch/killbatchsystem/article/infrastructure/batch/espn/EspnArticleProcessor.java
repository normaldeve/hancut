package com.system.batch.killbatchsystem.article.infrastructure.batch.espn;

import com.system.batch.killbatchsystem.article.domain.Article;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Slf4j
@Component("espnArticleProcessor")
@StepScope
public class EspnArticleProcessor implements ItemProcessor<String, Article> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public Article process(String url) throws Exception {
    log.info("[ESPN-Detail] GET {}", url);

    if (url.contains("/video/")) {
      log.info("Skip video url={}", url);
      return null;
    }

    // 1) 원문 페이지 수집 (헤더/타임아웃 보강)
    Document doc = Jsoup.connect(url)
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        .header("Accept-Language", "en-US,en;q=0.8,ko-KR;q=0.7,ko;q=0.5")
        .referrer("https://www.google.com/")
        .timeout(12_000)
        .followRedirects(true)
        .get();

    // 2) 메타/서브 메타
    String title = meta(doc, "meta[property=og:title]");
    String description = meta(doc, "meta[property=og:description]");
    String thumbnail = firstNonBlank(
        meta(doc, "meta[property=og:image]"),
        meta(doc, "meta[name=twitter:image]")
    );
    String sourceName = firstNonBlank(
        meta(doc, "meta[property=og:site_name]"),
        host(url)
    );
    String publishedRaw = firstNonBlank(
        meta(doc, "meta[property=article:published_time]"),
        meta(doc, "meta[name=article:published_time]")
    );
    LocalDateTime publishedAt = parseDateTimeSafe(publishedRaw);

    // 3) 본문 추출 (우선: article-body → 보강 셀렉터)
    String content = extractContentFromBody(doc);
    // 4) 폴백 1: JSON-LD articleBody
    if (isBlank(content)) content = tryJsonLdArticleBody(doc);
    // 5) 폴백 2: AMP 버전에서 본문 추출
    if (isBlank(content)) content = fetchAmpContent(url);
    // 6) 폴백 3: 그래도 없으면 메타 설명으로라도 채움
    if (isBlank(content)) content = description;

    // 필수 체크
    if (isBlank(thumbnail)) {
      log.warn("Skip ESPN article (no thumbnail) {}", url);
      return null;
    }
    if (isBlank(content)) {
      log.warn("Skip ESPN article (content empty) {}", url);
      return null;
    }

    String espnId = extractEspnIdFromUrl(url);
    if (isBlank(espnId)) {
      // fallback: url 해시라도 써야 함
      espnId = Integer.toHexString(url.getBytes(StandardCharsets.UTF_8).hashCode());
    }

    return Article.createArticle(espnId, url, content, thumbnail, sourceName,
        (publishedAt != null ? publishedAt : LocalDateTime.now(ZoneId.of("Asia/Seoul"))));
  }

  private String extractContentFromBody(Document doc) {
    // 가장 확실한 컨테이너: div.article-body
    Element body = doc.selectFirst("div.article-body");
    if (body == null) {
      body = doc.selectFirst("[data-testid=story-feed], .article-body-content, .story-body, article");
    }
    if (body != null) {
      // 광고/스크립트 제거
      body.select("script, style, noscript, .ad-slot, .inline-ads, [aria-label=advertisement]").remove();
      var parts = new ArrayList<String>();
      body.select("p, li, h2, h3, h4").forEach(el -> {
        String t = el.text();
        if (!isBlank(t)) parts.add(t.trim());
      });
      String joined = String.join("\n", parts).trim();
      if (!isBlank(joined)) return joined;
    }
    return null;
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
          .timeout(10_000)
          .get();

      // AMP에서도 유사한 컨테이너/문단 수집
      Element body = amp.selectFirst("article, [data-testid=story-feed], .article-body, .article-body-content, .story-body");
      if (body != null) {
        body.select("script, style, noscript, .ad-slot, .inline-ads, [aria-label=advertisement]").remove();
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
      String host = uri.getHost();           // www.espn.com
      String path = uri.getPath();           // /soccer/story/_/id/...
      String ampBase = "https://" + host + "/amp" + (path.startsWith("/") ? path : ("/" + path));
      String q = uri.getQuery();
      return (q == null) ? ampBase : ampBase + "?" + q;
    } catch (Exception e) {
      return url.contains("?") ? url + "&platform=amp" : url + "?platform=amp";
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

  private String host(String url) {
    try { return URI.create(url).getHost(); } catch (Exception e) { return null; }
  }

  private LocalDateTime parseDateTimeSafe(String raw) {
    if (isBlank(raw)) return null;
    try {
      return LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME); // ISO-8601
    } catch (Exception ignore) {}
    try {
      String fixed = raw.replaceFirst("(\\+|\\-)(\\d{2}):(\\d{2})$", "$1$2$3"); // +09:00 → +0900
      return LocalDateTime.parse(fixed, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
    } catch (Exception ignore) {}
    return null;
  }

  private static boolean isBlank(String s) { return s == null || s.isBlank(); }

  private String extractEspnIdFromUrl(String url) {
    try {
      URI uri = URI.create(url);
      String[] seg = uri.getPath().split("/");
      // ESPN 구조 예시: /football/story/_/id/45971354/slug
      for (int i = 0; i < seg.length; i++) {
        if ("id".equals(seg[i]) && i + 1 < seg.length) {
          return seg[i + 1]; // 바로 다음이 ID
        }
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }
}
