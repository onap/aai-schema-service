/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2026 Deutsche Telekom. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.schemaservice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

/**
 * Runs against the packaged application.properties, which the test-scoped
 * application.properties would otherwise shadow on the classpath.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = SchemaServiceApp.class)
@TestPropertySource(
    locations = "file:target/classes/application.properties",
    properties = {"server.local.startpath=src/main/resources/", "management.server.port=0"})
@Import(SchemaServiceTestConfiguration.class)
class ManagementEndpointsTest {

    @Autowired
    private RestTemplate restTemplate;

    @LocalServerPort
    private int serverPort;

    @LocalManagementPort
    private int managementPort;

    @BeforeAll
    static void setupConfig() {
        System.setProperty("AJSC_HOME", "./");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources/");
    }

    @Test
    void healthIsUp() throws Exception {
        ResponseEntity<String> response = get("/actuator/health");

        assertThat(response.getStatusCode().value(), is(200));
        assertThat(new ObjectMapper().readTree(response.getBody()).path("status").asText(),
            is("UP"));
    }

    @Test
    void prometheusServesMetrics() {
        ResponseEntity<String> response = get("/actuator/prometheus");

        assertThat(response.getStatusCode().value(), is(200));
        assertThat(response.getBody(), containsString("jvm_memory_used_bytes"));
    }

    @Test
    void infoShowsBuildArtifact() throws Exception {
        ResponseEntity<String> response = get("/actuator/info");

        assertThat(response.getStatusCode().value(), is(200));
        assertThat(
            new ObjectMapper().readTree(response.getBody()).path("build").path("artifact").asText(),
            is("aai-schema-service"));
    }

    @Test
    void otherEndpointsAreNotExposed() {
        assertThat(get("/actuator/env").getStatusCode().value(), is(404));
    }

    @Test
    void actuatorIsNotServedOnTheApplicationPort() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + serverPort + "/aai/schema-service/actuator/health", String.class);

        assertThat(response.getStatusCode().value(), is(not(200)));
    }

    private ResponseEntity<String> get(String path) {
        return restTemplate.getForEntity("http://localhost:" + managementPort + path, String.class);
    }
}
