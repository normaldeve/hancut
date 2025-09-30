package com.system.batch.killbatchsystem.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * 전역 ObjectMapper를 설정해 폴리모픽 타입정보(@class) 없이 일관된 JSON 직렬화 정책을 적용하는 구성 클래스
 *
 * @author : junwo
 * @fileName : JacksonConfig
 * @since : 2025-09-30
 */
@Configuration
public class JacksonConfig {

  @Primary
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    ObjectMapper mapper = builder.createXmlMapper(false).build();

    try {
      mapper.deactivateDefaultTyping();
    } catch (NoSuchMethodError e) {
      mapper.setDefaultTyping(null);
    }

    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    return mapper;
  }
}
