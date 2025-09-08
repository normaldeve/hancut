package com.system.batch.killbatchsystem.summary.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.batch.killbatchsystem.summary.domain.SummaryContent;
import com.system.batch.killbatchsystem.summary.application.AISummarize;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAISummarize implements AISummarize {

  private final WebClient webClient;

  @Value("${openai.model}")
  private String model;

  @Override
  public SummaryContent summarize(String content) {
    log.info("AI summary: {}", content);
    Map<String, Object> body = Map.of(
        "model", model,
        "response_format", Map.of("type", "json_object"),
        "temperature", 0.3,
        "messages", List.of(
            Map.of("role","system","content",
                "너는 한국어 스포츠 뉴스 편집자다. 다음 스키마의 JSON만 반환하라.\n" +
                    "{\"title\": string, \"league\": string, \"team\": string, \"summary\": string[], \"keywords\": string[]}\n" +
                    "규칙: \n" +
                    "- title: 25자 이내, 클릭베이트/과장 금지, 기사 사실 기반\n" +
                    "- league: 해당 기사가 어디 축구 리그와 관련된 기사인지 판단하고 리그 이름을 작성할 것. 또한 리그 이름은 띄어쓰기 없이 붙여서 작성할 것\n" +
                    "- team: 해당 기사가 어느 축구 팀과 관련된 기사인지 판단하고 팀 이름을 작성할 것\n" +
                    "- summary: 5 ~ 6개의 핵심 bullet, 각 30자 이내, 문장 말투\n" +
                    "- keywords: 3개, 각 10자 이내의 명사구, (선수 이름, 팀 이름, 리그 이름)에 해당하는 것만 포함시킬 것, 해시/이모지 금지(표시는 프론트에서 처리)\n" +
                    "- 한국어로 작성"),
            Map.of("role","user","content",
                "기사 본문:\n" + safeTrim(content, 8000))
        )
    );
    Map resp = webClient.post()
        .uri("/chat/completions")
        .bodyValue(body)
        .retrieve()
        .bodyToMono(Map.class)
        .block(Duration.ofSeconds(30));

    String contentJson = ((Map)((Map)((List)resp.get("choices")).get(0))
        .get("message")).get("content").toString();

    try {
      ObjectMapper om = new ObjectMapper();
      JsonNode n = om.readTree(contentJson);

      String title = n.path("title").asText(null);

      String league = n.path("league").asText(null);

      String team = n.path("team").asText(null);

      List<String> summary = new ArrayList<>();
      if (n.has("summary") && n.get("summary").isArray()) {
        for (JsonNode s : n.get("summary")) summary.add(s.asText());
      }

      List<String> keywords = new ArrayList<>();
      if (n.has("keywords") && n.get("keywords").isArray()) {
        for (JsonNode k : n.get("keywords")) keywords.add(k.asText());
      }

      log.info("AI summary: title: {}, league: {}", title, league);
      return new SummaryContent(title, league, team, summary, keywords);
    } catch (Exception e) {
      throw new IllegalStateException("요약 JSON 파싱 실패: " + e.getMessage(), e);
    }
  }

  private static String safeTrim(String s, int maxLen) {
    if (s == null) return "";
    return s.length() <= maxLen ? s : s.substring(0, maxLen);
  }
}
