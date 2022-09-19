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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.schemaservice.config.PropertyPasswordConfiguration;
import org.onap.aai.util.AAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = SchemaServiceApp.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(initializers = PropertyPasswordConfiguration.class)
@Import(SchemaServiceTestConfiguration.class)

@RunWith(SpringRunner.class)
public class SchemaServiceTest {

    private HttpHeaders headers;

    private HttpEntity httpEntity;

    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    private String authorization;

    @LocalServerPort
    protected int randomPort;

    @BeforeClass
    public static void setupConfig() throws AAIException {
        System.setProperty("AJSC_HOME", "./");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources/");
        System.out.println("Current directory: " + System.getProperty("user.dir"));
    }

    @Before
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
        httpEntity = new HttpEntity(headers);
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
        httpEntity = new HttpEntity(headers);

        ResponseEntity responseEntity;

        responseEntity = restTemplate.exchange(baseUrl + "/aai/schema-service/v1/nodes?version=v20",
            HttpMethod.GET, httpEntity, String.class);
        assertThat(responseEntity.getStatusCodeValue(), is(200));

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        httpEntity = new HttpEntity(headers);

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
        httpEntity = new HttpEntity(headers);

        ResponseEntity responseEntity;

        responseEntity =
            restTemplate.exchange(baseUrl + "/aai/schema-service/v1/nodes?version=blah",
                HttpMethod.GET, httpEntity, String.class);
        System.out.println("  " + responseEntity.getBody());
        assertThat(responseEntity.getStatusCodeValue(), is(400));

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        httpEntity = new HttpEntity(headers);

        responseEntity =
            restTemplate.exchange(baseUrl + "/aai/schema-service/v1/edgerules?version=blah",
                HttpMethod.GET, httpEntity, String.class);

        assertThat(responseEntity.getStatusCodeValue(), is(400));
    }

    @Test
    public void testVersions() {

        ResponseEntity responseEntity;

        responseEntity = restTemplate.exchange(baseUrl + "/aai/schema-service/v1/versions",
            HttpMethod.GET, httpEntity, String.class);
        assertThat(responseEntity.getStatusCodeValue(), is(200));

    }

    @Test
    public void testGetStoredQueriesSuccess() {

        ResponseEntity responseEntity;

        responseEntity = restTemplate.exchange(baseUrl + "/aai/schema-service/v1/stored-queries",
            HttpMethod.GET, httpEntity, String.class);
        assertThat(responseEntity.getStatusCodeValue(), is(200));

    }
}
