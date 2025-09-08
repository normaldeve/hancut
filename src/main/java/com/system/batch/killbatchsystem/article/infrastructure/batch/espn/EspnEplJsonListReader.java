package com.system.batch.killbatchsystem.article.infrastructure.batch.espn;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayDeque;
import java.util.Queue;

@Slf4j
public class EspnEplJsonListReader implements ItemReader<String> {

  private final WebClient client;
  private final ObjectMapper mapper = new ObjectMapper();

  private final String apiUrl;
  private final int limit;
  private final int maxItems;
  private final int throttleMs;

  private final Queue<String> queue = new ArrayDeque<>();
  private boolean initialized = false;

  public EspnEplJsonListReader(
      @Value("${espn.json.epl-url}") String apiUrl,
      @Value("${espn.limit:20}") int limit,
      @Value("${espn.max-items:20}") int maxItems,
      @Value("${espn.throttleMs:400}") Integer throttleMs
  ) {
    this.apiUrl = apiUrl;
    this.limit = limit;
    this.maxItems = maxItems;
    this.throttleMs = (throttleMs == null ? 400 : throttleMs);

    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
        .build();

    this.client = WebClient.builder()
        .exchangeStrategies(strategies)
        .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0")
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  @Override
  public String read() throws Exception {
    if (!initialized) { init(); initialized = true; }
    String next = queue.poll();
    if (next != null && throttleMs > 0) Thread.sleep(throttleMs);
    return next;
  }

  private void init() {
    String nextUrl = appendOrReplaceLimit(apiUrl, limit);
    int collected = 0;

    while (nextUrl != null && collected < maxItems) {
      log.info("[ESPN-List] GET {}", nextUrl);
      String body = client.get().uri(nextUrl)
          .retrieve().bodyToMono(String.class)
          .block();

      if (body == null || body.isBlank()) break;

      try {
        JsonNode root = mapper.readTree(body);
        // articles 혹은 headlines
        JsonNode arr = root.has("articles") ? root.get("articles") : root.get("headlines");
        int pageCount = 0;
        if (arr != null && arr.isArray()) {
          for (JsonNode n : arr) {
            String href = n.path("links").path("web").path("href").asText(null);
            if (href != null && !href.isBlank()) {
              queue.offer(href);
              collected++; pageCount++;
              if (collected >= maxItems) break;
            }
          }
        }
        log.info("collected={} (+{} this page)", collected, pageCount);

        // 다음 페이지 링크(있으면) 추출
        String maybeNext = null;
        JsonNode links = root.path("links");
        if (!links.isMissingNode()) {
          maybeNext = links.path("next").path("href").asText(null);
        }
        // 일부 응답은 page.next 같은 다른 경로를 사용할 수 있으므로 보조 체크
        if (maybeNext == null) {
          JsonNode page = root.path("page");
          if (!page.isMissingNode()) {
            maybeNext = page.path("next").asText(null);
          }
        }

        nextUrl = (maybeNext == null || maybeNext.isBlank()) ? null : ensureLimit(maybeNext, limit);
      } catch (Exception e) {
        log.warn("Failed to parse ESPN news page", e);
        break;
      }
    }

    if (queue.isEmpty()) {
      throw new IllegalStateException("No ESPN article URLs collected.");
    }
  }

  private String appendOrReplaceLimit(String url, int limit) {
    return ensureLimit(url, limit);
  }
  private String ensureLimit(String url, int limit) {
    // limit 파라미터가 있으면 바꾸고, 없으면 추가
    if (url.contains("limit=")) {
      return url.replaceAll("([?&]limit=)\\d+", "$1" + limit);
    }
    return url + (url.contains("?") ? "&" : "?") + "limit=" + limit;
  }
}