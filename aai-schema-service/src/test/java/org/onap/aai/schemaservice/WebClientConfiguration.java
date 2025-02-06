package org.onap.aai.schemaservice;

import java.time.Duration;
import java.util.Collections;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@TestConfiguration
public class WebClientConfiguration {

  @Lazy
  @Bean
  WebTestClient webTestClient(@LocalServerPort int port) {
    return WebTestClient.bindToServer()
      .baseUrl("http://localhost:" + port + "/aai/schema-service")
      .responseTimeout(Duration.ofSeconds(300))
      .defaultHeaders(headers -> {
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-FromAppId", "test");
        headers.set("X-TransactionId", "someTransaction");
        headers.setBasicAuth("AAI", "AAI");
      })
      .build();
  }
}
