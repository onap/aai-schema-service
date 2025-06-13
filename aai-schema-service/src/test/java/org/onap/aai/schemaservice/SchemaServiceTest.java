/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
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
import static org.hamcrest.Matchers.is;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.util.AAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = SchemaServiceApp.class)
@Import(SchemaServiceTestConfiguration.class)
public class SchemaServiceTest {

    private HttpHeaders headers;

    private HttpEntity<String> httpEntity;

    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    private String authorization;

    @LocalServerPort
    protected int randomPort;

    @BeforeAll
    public static void setupConfig() throws AAIException {
        System.setProperty("AJSC_HOME", "./");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources/");
        System.out.println("Current directory: " + System.getProperty("user.dir"));
    }

    @BeforeEach
    public void setup() throws AAIException, UnsupportedEncodingException {

        AAIConfig.init();
        headers = new HttpHeaders();

        authorization = Base64.getEncoder().encodeToString("AAI:AAI".getBytes("UTF-8"));

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");
        headers.add("Authorization", "Basic " + authorization);
        httpEntity = new HttpEntity<String>(headers);
        baseUrl = "http://localhost:" + randomPort;
    }

    @Test
    public void testGetSchemaAndEdgeRules() {

        headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");
        headers.add("Authorization", "Basic " + authorization);
        httpEntity = new HttpEntity<String>(headers);

        ResponseEntity<String> responseEntity;

        responseEntity = restTemplate.exchange(baseUrl + "/aai/schema-service/v1/nodes?version=v20",
            HttpMethod.GET, httpEntity, String.class);
        assertThat(responseEntity.getStatusCodeValue(), is(200));

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        httpEntity = new HttpEntity<String>(headers);

        responseEntity =
            restTemplate.exchange(baseUrl + "/aai/schema-service/v1/edgerules?version=v20",
                HttpMethod.GET, httpEntity, String.class);

        assertThat(responseEntity.getStatusCodeValue(), is(200));
    }

    @Test
    public void testInvalidSchemaAndEdges() {

        headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");
        headers.add("Authorization", "Basic " + authorization);
        httpEntity = new HttpEntity<String>(headers);

        ResponseEntity<String> responseEntity;

        responseEntity =
            restTemplate.exchange(baseUrl + "/aai/schema-service/v1/nodes?version=blah",
                HttpMethod.GET, httpEntity, String.class);
        System.out.println("  " + responseEntity.getBody());
        assertThat(responseEntity.getStatusCodeValue(), is(400));

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        httpEntity = new HttpEntity<String>(headers);

        responseEntity =
            restTemplate.exchange(baseUrl + "/aai/schema-service/v1/edgerules?version=blah",
                HttpMethod.GET, httpEntity, String.class);

        assertThat(responseEntity.getStatusCodeValue(), is(400));
    }

    @Test
    public void testVersions() {

        ResponseEntity<String> responseEntity;

        responseEntity = restTemplate.exchange(baseUrl + "/aai/schema-service/v1/versions",
            HttpMethod.GET, httpEntity, String.class);
        assertThat(responseEntity.getStatusCodeValue(), is(200));

    }

    @Test
    public void testGetStoredQueriesSuccess() {

        ResponseEntity<String> responseEntity;

        responseEntity = restTemplate.exchange(baseUrl + "/aai/schema-service/v1/stored-queries",
            HttpMethod.GET, httpEntity, String.class);
        assertThat(responseEntity.getStatusCodeValue(), is(200));

    }

    @Test
    public void testSetDefaultProps() {
        // Simulate directory containing app name
        System.setProperty("user.dir", "/path/to/aai-schema-service");
        System.clearProperty("BUNDLECONFIG_DIR");

        // Call setDefaultProps
        SchemaServiceApp.setDefaultProps();

        // Verify the BUNDLECONFIG_DIR property is set correctly
        assertEquals("src/main/resources", System.getProperty("BUNDLECONFIG_DIR"));

        // Simulate directory not containing app name
        System.setProperty("user.dir", "/path/to/other");
        System.clearProperty("BUNDLECONFIG_DIR");
        SchemaServiceApp.setDefaultProps();

        // Verify the default value when not containing the app name
        assertEquals("aai-schema-service/src/main/resources", System.getProperty("BUNDLECONFIG_DIR"));
    }

    @Test
    public void testSetDefaultPropsWhenNotSet() {
        // Simulate directory not containing app name
        System.setProperty("user.dir", "/path/to/other");
        System.clearProperty("BUNDLECONFIG_DIR");

        // Call setDefaultProps
        SchemaServiceApp.setDefaultProps();

        // Verify the default value when the property was not previously set
        assertEquals("aai-schema-service/src/main/resources", System.getProperty("BUNDLECONFIG_DIR"));
    }

    // Test for setDefaultProps with null file.separator
    @Test
    public void testSetDefaultPropsWithNullFileSeparator() {
        // Clear the file.separator property
        System.clearProperty("file.separator");

        // Call setDefaultProps to set the default value
        SchemaServiceApp.setDefaultProps();

        // Verify that the file.separator system property is set to "/"
        assertEquals("/", System.getProperty("file.separator"));
    }

    @Test
    public void testAJSCHomePropertyWhenNotSet() {
        // Clear the AJSC_HOME property to simulate it being unset
        System.clearProperty("AJSC_HOME");

        // Call setDefaultProps to ensure AJSC_HOME gets set
        SchemaServiceApp.setDefaultProps();

        // Verify that the AJSC_HOME property is set to "."
        assertEquals(".", System.getProperty("AJSC_HOME"));
    }

}
