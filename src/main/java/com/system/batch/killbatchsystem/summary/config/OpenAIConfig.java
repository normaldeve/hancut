package com.system.batch.killbatchsystem.summary.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenAIConfig {

  @Bean
  public WebClient openAiWebClient(
      @Value("${openai.api-key}") String apiKey) {

    // 다형성 타입정보(@class) 안 붙는 매퍼
    ObjectMapper cleanMapper = JsonMapper.builder()
        .findAndAddModules()
        .build();

    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(c -> {
          c.defaultCodecs().jackson2JsonEncoder(
              new Jackson2JsonEncoder(cleanMapper, MediaType.APPLICATION_JSON));
          c.defaultCodecs().jackson2JsonDecoder(
              new Jackson2JsonDecoder(cleanMapper, MediaType.APPLICATION_JSON));
        })
        .build();

    return WebClient.builder()
        .baseUrl("https://api.openai.com/v1")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .exchangeStrategies(strategies)
        .build();
  }
}
