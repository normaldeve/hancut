package com.system.batch.killbatchsystem.article.infrastructure.batch.naver;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.JsonParseException;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class NaverSoccerJsonListReader implements ItemReader<String> {

  private final WebClient client;
  private final ObjectMapper mapper;

  private final String baseUrl;
  private final String date;
  private final int maxPages;
  private final int throttleMs;

  // ★ 기대 섹션(epl/primera/bundesliga/seriea/ligue1 등)
  private final String expectedSectionId;

  private final Queue<String> queue = new ArrayDeque<>();
  private boolean initialized = false;

  public NaverSoccerJsonListReader(String baseUrl, String date, int maxPages, int throttleMs, String league) {
    this.baseUrl = baseUrl;
    this.date = date;
    this.maxPages = maxPages;
    this.throttleMs = throttleMs;
    this.expectedSectionId = mapLeagueToSectionId(league);

    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
        .build();

    this.client = WebClient.builder()
        .exchangeStrategies(strategies)
        .defaultHeader(HttpHeaders.USER_AGENT,
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
        .defaultHeader(HttpHeaders.ACCEPT, "*/*")
        .build();

    this.mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private String mapLeagueToSectionId(String league) {
    if (league == null) return null; // 필터 없음(전체)
    String k = league.trim().toUpperCase();
    return switch (k) {
      case "EPL", "PREMIER", "PREMIERLEAGUE" -> "epl";
      case "LALIGA", "LA_LIGA", "PRIMERA", "PRIMERA DIVISION" -> "primera";
      case "BUNDES", "BUNDESLIGA" -> "bundesliga";
      case "SERIEA", "SERIE_A" -> "seriea";
      case "LIGUE1", "LIGUE_1" -> "ligue1";
      default -> "null";
    };
  }

  @Override
  public String read() throws Exception {
    if (!initialized) { init(); initialized = true; }
    return queue.poll();
  }

  private void init() {
    int total = 0;

    for (int p = 1; p <= maxPages; p++) {
      String url = baseUrl.replace("{date}", date).replace("{page}", String.valueOf(p));
      log.info("[List-JSON] GET {}", url);

      try {
        String body = client.get().uri(url)
            .accept(MediaType.ALL)
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(10))
            .block();

        if (body == null || body.isBlank()) { log.warn("Empty body: {}", url); continue; }

        log.info("RAW(len={}): {}", body.length(),
            body.substring(0, Math.min(400, body.length())).replaceAll("\\s+", " "));

        String trimmed = body.strip();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
          trimmed = trimmed.substring(1, trimmed.length() - 1)
              .replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
        }

        boolean usedRegex = false;
        try {
          // 1) Jackson
          JsonNode list = mapper.readTree(trimmed).path("newsListModel").path("newsList");
          if (list.isArray()) {
            int pageCount = 0;
            for (JsonNode n : list) {
              String sectionId = asTextOrNull(n, "sectionId");
              if (!acceptBySection(sectionId)) continue;
              String oid = asTextOrNull(n, "oid");
              String aid = asTextOrNull(n, "aid");
              if (oid == null || aid == null) continue;
              queue.offer("https://m.sports.naver.com/wfootball/article/" + oid + "/" + aid);
              total++; pageCount++;
            }
            log.info("Jackson extracted {} items (after filter)", pageCount);
          }
        } catch (JsonParseException jpe) {
          usedRegex = true;
          log.warn("Jackson parse failed, fallback to regex. cause={}", jpe.getMessage());
        } catch (Exception ex) {
          usedRegex = true;
          log.warn("Jackson parse failed, fallback to regex.", ex);
        }

        if (usedRegex) {
          // 2) 정규식: oid/aid/sectionId 모두 캡처
          Pattern ptn = Pattern.compile(
              "\"oid\"\\s*:\\s*\"(\\d+)\".*?\"aid\"\\s*:\\s*\"(\\d+)\".*?\"sectionId\"\\s*:\\s*\"([^\"]+)\"",
              Pattern.DOTALL);
          Matcher m = ptn.matcher(trimmed);
          int pageCount = 0;
          while (m.find()) {
            String oid = m.group(1);
            String aid = m.group(2);
            String sectionId = m.group(3);
            if (!acceptBySection(sectionId)) continue;
            queue.offer("https://m.sports.naver.com/wfootball/article/" + oid + "/" + aid);
            total++; pageCount++;
          }
          log.info("Regex extracted {} items (after filter)", pageCount);
        }

        Thread.sleep(throttleMs);
      } catch (Exception e) {
        log.warn("List fetch/parse failed: {}", url, e);
      }
    }

    log.info("Collected {} article URLs", total);
    if (total == 0) throw new IllegalStateException("No article URLs collected from JSON.");
  }

  private boolean acceptBySection(String sectionId) {
    return expectedSectionId == null || expectedSectionId.equalsIgnoreCase(sectionId);
  }

  private static String asTextOrNull(JsonNode node, String field) {
    if (node == null) return null;
    JsonNode v = node.get(field);
    return (v == null || v.isNull()) ? null : v.asText(null);
  }
}
