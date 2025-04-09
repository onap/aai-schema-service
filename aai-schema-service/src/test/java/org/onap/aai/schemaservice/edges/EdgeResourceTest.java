/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom. All rights reserved.
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
package org.onap.aai.schemaservice.edges;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.schemaservice.nodeschema.SchemaVersion;
import org.onap.aai.schemaservice.nodeschema.SchemaVersions;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class EdgeResourceTest {

    @InjectMocks
    private EdgeResource edgeResource;

    @Mock
    private EdgeService edgeService;

    @Mock
    private SchemaVersions schemaVersions;

    @Mock
    private HttpHeaders headers;

    @Mock
    private UriInfo uriInfo;

    private Gson gson;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gson = new Gson();
        edgeResource = new EdgeResource(edgeService, schemaVersions, gson);
    }

    @Test
    void testRetrieveSchema_Success() throws AAIException {
        String version = "v1";

        EdgeRule edgeRule1 = new EdgeRule();
        List<EdgeRule> edgeRuleList = Arrays.asList(edgeRule1);
        EdgeRules edgeRules = new EdgeRules(edgeRuleList);

        List<SchemaVersion> schemaVersionList = Arrays.asList(new SchemaVersion(version));
        when(edgeService.findRules(version)).thenReturn(Optional.of(edgeRules));
        when(schemaVersions.getVersions()).thenReturn(schemaVersionList);

        Response response = edgeResource.retrieveSchema(version, headers, uriInfo);

        assertEquals(200, response.getStatus());
    }

    @Test
    void testRetrieveSchema_InvalidVersion_Empty() {
        String version = "";
        Response response = edgeResource.retrieveSchema(version, headers, uriInfo);
        String expectedResponseEntity = "{\"requestError\":{\"serviceException\":{"
            + "\"messageId\":\"SVC3000\","
            + "\"text\":\"Invalid input performing %1 on %2 (msg=%3) (ec=%4)\","
            + "\"variables\":[\"GET\",null,\"Invalid Accept header\",\"4.0.4014\"]"
            + "}}}";

        assertEquals(expectedResponseEntity,response.getEntity());
    }

    @Test
    void testRetrieveSchema_VersionNotFound() throws AAIException {
        String version = "v2";

        List<SchemaVersion> schemaVersionList = Arrays.asList(new SchemaVersion("v1"));
        when(schemaVersions.getVersions()).thenReturn(schemaVersionList);

        Response response = edgeResource.retrieveSchema(version, headers, uriInfo);

        assertEquals(400, response.getStatus());
        String expectedResponseEntity = "{"
            + "\"requestError\":{"
            + "\"serviceException\":{"
            + "\"messageId\":\"SVC3000\","
            + "\"text\":\"Invalid input performing %1 on %2 (msg=%3) (ec=%4)\","
            + "\"variables\":[\"GET\",null,\"Invalid Accept header\",\"4.0.4014\"]"
            + "}}}";
        assertEquals(expectedResponseEntity,response.getEntity());
    }

    @Test
    void testRetrieveSchema_EdgeRulesNotFound() throws AAIException {
        String version = "v1";

        when(edgeService.findRules(version)).thenReturn(Optional.empty());
        List<SchemaVersion> schemaVersionList = Arrays.asList(new SchemaVersion(version));
        when(schemaVersions.getVersions()).thenReturn(schemaVersionList);

        Response response = edgeResource.retrieveSchema(version, headers, uriInfo);

        assertEquals(404, response.getStatus());
        String expectedResponseEntity = "{\"requestError\":{\"serviceException\":{"
            + "\"messageId\":\"SVC3000\","
            + "\"text\":\"Invalid input performing %1 on %2 (msg=%3) (ec=%4)\","
            + "\"variables\":[\"GET\",null,\"Invalid Accept header\",\"4.0.4014\"]"
            + "}}}";

        assertEquals(expectedResponseEntity,response.getEntity());
    }

    @Test
    void testRetrieveSchema_ExceptionHandling() throws AAIException {
        String version = "v1";

        when(edgeService.findRules(version)).thenThrow(new RuntimeException("Unexpected error"));

        List<SchemaVersion> schemaVersionList = Arrays.asList(new SchemaVersion(version));
        when(schemaVersions.getVersions()).thenReturn(schemaVersionList);

        Response response = edgeResource.retrieveSchema(version, headers, uriInfo);

        assertEquals(500, response.getStatus());
        String expectedResponseEntity = "{\"requestError\":{\"serviceException\":{"
            + "\"messageId\":\"SVC3000\","
            + "\"text\":\"Invalid input performing %1 on %2 (msg=%3) (ec=%4)\","
            + "\"variables\":[\"GET\",null,\"Invalid Accept header\",\"4.0.4014\"]"
            + "}}}";
       assertEquals(expectedResponseEntity,response.getEntity());
    }
}
