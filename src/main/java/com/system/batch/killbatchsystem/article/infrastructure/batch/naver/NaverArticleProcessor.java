package com.system.batch.killbatchsystem.article.infrastructure.batch.naver;

import com.system.batch.killbatchsystem.article.domain.Article;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component(value = "naverArticleProcessor")
@StepScope
public class NaverArticleProcessor implements ItemProcessor<String, Article> {

  @Value("${crawler.content.max-chars:700}")
  private int maxChars;

  @Override
  public Article process(String url) throws Exception {
    log.info("[Detail] GET {}", url);

    Document doc = Jsoup.connect(url)
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
        .header("Accept-Language", "ko,ko-KR;q=0.9,en-US;q=0.8,en;q=0.7")
        .referrer("https://m.sports.naver.com/")
        .timeout(10000)
        .get();

    String content = textOrNull(doc.selectFirst("article, #newsct_article, #newsEndContents, .news_end"));
    if (content == null) content = textOrNull(doc.body());

    // 금지 문구 필터
    if (containsProhibitedNotice(doc, content)) {
      log.warn("Skip (copyright/prohibited notice detected) url={}", url);
      return null;
    }

    // ===== 네이버 기사 ID =====
    String naverId = extractId(url);

    // ===== 발행 시각 =====
    String dateText = null; String hit = null;
    String[] metaSelectors = {
        "meta[property=article:published_time]",
        "meta[name=ptime]"
    };
    for (String sel : metaSelectors) {
      Element e = doc.selectFirst(sel);
      if (e != null) {
        dateText = e.attr("content");
        if (notBlank(dateText)) { hit = sel; break; }
      }
    }
    if (isBlank(dateText)) {
      String[] viewSelectors = {
          "div.article_head_info em.date",
          "em.date",
          "span._ARTICLE_DATE_TIME[data-date-time]",
          "span._ARTICLE_DATE_TIME",
          ".media_end_head_info_datestamp_time[data-date-time]",
          ".media_end_head_info_datestamp_time",
          "#newsct_article_date[data-date-time]",
          "#newsct_article_date"
      };
      for (String sel : viewSelectors) {
        Element e = doc.selectFirst(sel);
        if (e != null) {
          dateText = e.hasAttr("data-date-time") ? e.attr("data-date-time") : e.text();
          if (notBlank(dateText)) { hit = sel; break; }
        }
      }
    }
    if (isBlank(dateText)) {
      Element head = doc.selectFirst("div.article_head_info, #newsct, #ct");
      if (head != null) {
        String headText = head.text();
        Pattern p = Pattern.compile("(\\d{4})\\.(\\d{2})\\.(\\d{2})\\.?\\s*(오전|오후)?\\s*(\\d{1,2}):(\\d{2})(?::(\\d{2}))?");
        Matcher m = p.matcher(headText);
        if (m.find()) { dateText = m.group(0); hit = "regex(head)"; }
      }
    }
    LocalDateTime publishedAt = parseKST(dateText);
    if (publishedAt == null) {
      log.warn("Failed to parse published date [{}] (hit:{}) from {}", dateText, hit, url);
      publishedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    // ===== 썸네일 / 출처 =====
    String thumbnailUrl = extractThumbnail(doc, url);
    if (thumbnailUrl == null) {
      log.warn("Skip article {} because no thumbnail found", naverId);
      return null;
    }
    String sourceName   = resolveSourceName(doc, url);

    return Article.createArticle(naverId, url, content, thumbnailUrl, sourceName, publishedAt);
  }

  /** og:image → twitter:image → 본문 첫 이미지 순으로 추출, 상대경로는 절대경로로 보정 */
  private String extractThumbnail(Document doc, String pageUrl) {
    // 1) 메타 태그 우선
    String[] metas = {
        "meta[property=og:image]",
        "meta[name=og:image]",
        "meta[name=twitter:image]",
        "meta[property=twitter:image]"
    };
    for (String sel : metas) {
      Element e = doc.selectFirst(sel);
      if (e != null && notBlank(e.attr("content"))) {
        return absolutize(pageUrl, e.attr("content"));
      }
    }
    // 2) 본문 내 이미지 폴백
    Element img = doc.selectFirst("article img, #newsct_article img, #newsEndContents img, .news_end img, img");
    if (img != null) {
      String src = notBlank(img.attr("data-src")) ? img.attr("data-src") : img.attr("src");
      if (notBlank(src)) return absolutize(pageUrl, src);
    }
    return null;
  }

  /** 도메인 호스트 */
  private String resolveSourceName(Document doc, String pageUrl) {
    Element e = doc.selectFirst("meta[property=og:site_name], meta[name=og:site_name]");
    if (e != null && notBlank(e.attr("content"))) return e.attr("content");
    try { return URI.create(pageUrl).getHost(); } catch (Exception ignore) { return null; }
  }

  /** 상대 URL을 원문 기준 절대 URL로 변환 */
  private String absolutize(String baseUrl, String maybeRelative) {
    try {
      return Jsoup.parse("<a href=\"" + maybeRelative + "\"></a>", baseUrl)
          .selectFirst("a").absUrl("href");
    } catch (Exception e) { return maybeRelative; }
  }

  private String extractId(String articleUrl) {
    try {
      URI uri = URI.create(articleUrl);
      String[] seg = uri.getPath().split("/");
      String oid = seg[seg.length - 2];
      String aid = seg[seg.length - 1];
      return oid + "_" + aid;
    } catch (Exception e) {
      return Integer.toHexString(articleUrl.getBytes(StandardCharsets.UTF_8).hashCode());
    }
  }

  private static boolean isBlank(String s) { return s == null || s.isBlank(); }
  private static boolean notBlank(String s) { return !isBlank(s); }
  private String textOrNull(Element e) { return e == null ? null : e.text(); }

  /** 네이버에서 흔한 포맷들을 모두 시도 + 정규식 폴백(AM/PM 변환 포함) */
  private LocalDateTime parseKST(String text) {
    if (isBlank(text)) return null;

    String normalized = text.trim()
        .replace("오전", "AM")
        .replace("오후", "PM");

    List<DateTimeFormatter> fmts = List.of(
        DateTimeFormatter.ISO_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy.MM.dd. a h:mm:ss", Locale.KOREA),
        DateTimeFormatter.ofPattern("yyyy.MM.dd. a h:mm", Locale.KOREA),
        DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm:ss", Locale.KOREA),
        DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm", Locale.KOREA),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.KOREA),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.KOREA)
    );

    for (DateTimeFormatter f : fmts) {
      try { return LocalDateTime.parse(normalized, f); } catch (Exception ignore) {}
    }

    Pattern p = Pattern.compile("(\\d{4})\\.(\\d{2})\\.(\\d{2})\\.?\\s*(AM|PM)?\\s*(\\d{1,2}):(\\d{2})(?::(\\d{2}))?",
        Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(normalized);
    if (m.find()) {
      int y = Integer.parseInt(m.group(1));
      int mo = Integer.parseInt(m.group(2));
      int d = Integer.parseInt(m.group(3));
      String ap = m.group(4);
      int h = Integer.parseInt(m.group(5));
      int mi = Integer.parseInt(m.group(6));
      int s = m.group(7) != null ? Integer.parseInt(m.group(7)) : 0;

      if (ap != null) {
        boolean pm = ap.equalsIgnoreCase("PM");
        if (pm && h < 12) h += 12;
        if (!pm && h == 12) h = 0;
      }
      return LocalDateTime.of(LocalDate.of(y, mo, d), LocalTime.of(h, mi, s));
    }
    return null;
  }

  // 금지 문구 탐지: 본문 + 흔한 위치(푸터/저작권 표기 + 메타태그)
  private boolean containsProhibitedNotice(Document doc, String content) {
    String norm = normalize(content);

    // 자주 보이는 한국어/영문 패턴들
    String[] phrases = {
        "무단전재및재배포금지",
        "무단전재및재배포는금지",
        "무단전재금지",
        "무단복제및배포금지",
        "저작권자",
        "저작권보호",
        "copyright",
        "allrightsreserved",
        "unauthorizedreproductionprohibited",
        "redistributionprohibited"
    };

    for (String p : phrases) {
      if (norm.contains(p)) return true;
    }

    // 페이지 내 흔한 표기 위치도 같이 체크
    String[] sel = {
        ".copyright", ".footer", "#footer", ".media_end_copyright",
        "meta[name=copyright]", "meta[property=article:copyright]"
    };
    for (String s : sel) {
      var e = doc.selectFirst(s);
      if (e != null) {
        String t = (e.hasAttr("content") ? e.attr("content") : e.text());
        if (t != null && containsAny(normalize(t), phrases)) return true;
      }
    }
    return false;
  }

  private boolean containsAny(String text, String[] needles) {
    for (String n : needles) if (text.contains(n)) return true;
    return false;
  }

  private String normalize(String s) {
    if (s == null) return "";
    // 공백/구두점 제거 + 소문자화로 느슨하게 매칭
    return s.toLowerCase(Locale.ROOT).replaceAll("\\s+", "")
        .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}가-힣]", "");
  }
}
