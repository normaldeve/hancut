package com.system.batch.killbatchsystem.article.infrastructure.batch.goal;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.batch.item.ItemReader;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Queue;

@Slf4j
public class GoalNewsSitemapReader implements ItemReader<GoalSitemapItem> {

  private final String sitemapUrl;
  private final int maxItems;
  private final int throttleMs;
  private final int recentHours;

  private final Queue<GoalSitemapItem> queue = new ArrayDeque<>();
  private boolean initialized = false;

  public GoalNewsSitemapReader(String sitemapUrl, int maxItems, int recentHours, Integer throttleMs) {
    this.sitemapUrl = sitemapUrl;
    this.maxItems = Math.max(1, maxItems);
    this.recentHours = Math.max(0, recentHours);
    this.throttleMs = (throttleMs == null ? 400 : throttleMs);
  }

  @Override
  public GoalSitemapItem read() throws Exception {
    if (!initialized) { init(); initialized = true; }
    GoalSitemapItem next = queue.poll();
    if (next != null && throttleMs > 0) Thread.sleep(throttleMs);
    return next;
  }

  private void init() {
    try {
      log.info("[GOAL-NEWS] GET {}", sitemapUrl);
      Document xml = Jsoup.connect(sitemapUrl)
          .userAgent("Mozilla/5.0")
          .ignoreContentType(true) // XML
          .timeout(12000)
          .get();

      int collected = 0;
      Instant now = Instant.now();

      for (Element url : xml.select("urlset > url")) {
        String loc = text(url, "loc");
        if (loc == null || loc.isBlank()) continue;

        String title = text(url, "news\\:news > news\\:title");
        String pub = text(url, "news\\:news > news\\:publication_date");
        String img = text(url, "image\\:image > image\\:loc");

        // 최근 X시간 제한
        if (recentHours > 0 && pub != null && !pub.isBlank()) {
          try {
            OffsetDateTime odt = OffsetDateTime.parse(pub, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            if (Duration.between(odt.toInstant(), now).toHours() > recentHours) {
              continue;
            }
          } catch (Exception ignore) {}
        }

        String u = loc.toLowerCase();
        if (u.contains("/video/") || u.contains("/videos/") || u.contains("/live/") || u.contains("/match/")) {
          continue;
        }

        OffsetDateTime odt = null;
        try { odt = OffsetDateTime.parse(pub, DateTimeFormatter.ISO_OFFSET_DATE_TIME); } catch (Exception ignore) {}

        queue.offer(new GoalSitemapItem(loc, title, img, odt));
        collected++;
        if (collected >= maxItems) break;
      }

      log.info("GOAL-NEWS collected={}, queueSize={}", collected, queue.size());
      if (queue.isEmpty()) {
        throw new IllegalStateException("No Goal.com news URLs collected from sitemap.");
      }

    } catch (Exception e) {
      log.warn("Failed to parse Goal Google News sitemap", e);
      throw new IllegalStateException("Goal Google News sitemap fetch failed: " + sitemapUrl, e);
    }
  }

  private String text(Element root, String css) {
    Element e = root.selectFirst(css);
    return (e == null) ? null : e.text();
  }
}
