package org.onap.aai.schemaservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Configuration
public class MappingConfig {

  @Bean
  public Gson gson() {
    return new GsonBuilder().create();
  }
}
