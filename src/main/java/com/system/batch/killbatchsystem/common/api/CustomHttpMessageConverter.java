package com.system.batch.killbatchsystem.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Please explain the class!!
 *
 * @author : junwo
 * @fileName : CustomHttpMessageConverter
 * @since : 2025-09-30
 */
public class CustomHttpMessageConverter extends MappingJackson2HttpMessageConverter {

  public CustomHttpMessageConverter(ObjectMapper objectMapper) {
    super(objectMapper);
    objectMapper.registerModule(new JavaTimeModule());
    setObjectMapper(objectMapper);
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return canWrite(mediaType);
  }
}
