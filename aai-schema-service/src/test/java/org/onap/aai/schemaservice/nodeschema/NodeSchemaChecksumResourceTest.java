/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2024 Deutsche Telekom. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
*/

package org.onap.aai.schemaservice.nodeschema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.schemaservice.WebClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(WebClientConfiguration.class)
public class NodeSchemaChecksumResourceTest {

  @Autowired
  WebTestClient client;

  @Autowired
  SchemaVersions schemaVersions;

  @BeforeAll
  public static void setupConfig() throws AAIException {
    System.setProperty("AJSC_HOME", "./");
    System.setProperty("BUNDLECONFIG_DIR", "src/main/resources/");
    System.out.println("Current directory: " + System.getProperty("user.dir"));
  }

  @Test
  public void thatChecksumsCanBeRetrieved() {
    ChecksumResponse response = client.get()
        .uri("/v1/nodes/checksums")
        .exchange()
        .expectStatus().isOk()
        .returnResult(ChecksumResponse.class)
        .getResponseBody()
        .blockFirst();
    assertEquals(schemaVersions.getVersions().size(), response.getChecksumMap().size());
  }

}
