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
package org.onap.aai.schemaservice.nodeschema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NodeSchemaResourceTest {

    @Mock
    private NodeSchemaService nodeSchemaService;

    @Mock
    private SchemaVersions schemaVersions;

    @Mock
    private HttpHeaders headers;

    @Mock
    private UriInfo uriInfo;

    @InjectMocks
    private NodeSchemaResource nodeSchemaResource;

    @Test
    public void testRetrieveSchema_Success() {
        String version = "v1";
        String schema = "<schema>...</schema>";

        when(schemaVersions.getVersions()).thenReturn(
            List.of(new SchemaVersion("v1"), new SchemaVersion("v2"), new SchemaVersion("v3"))
        );
        when(nodeSchemaService.fetch(version)).thenReturn(Optional.of(schema));

        Response response = nodeSchemaResource.retrieveSchema(version, headers, uriInfo);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(schema, response.getEntity());
    }

    @Test
    public void testRetrieveSchema_VersionEmpty_ThrowsAAIException() {
        String version = "";
        when(nodeSchemaService.fetch(version)).thenReturn(Optional.empty());

        Response response = nodeSchemaResource.retrieveSchema(version, headers, uriInfo);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),response.getStatus());
        String expectedResponseEntity = "{\"requestError\":{\"serviceException\":{\"messageId\":\"SVC3000\",\"text\":\"Invalid input performing %1 on %2 (msg=%3) (ec=%4)\",\"variables\":[\"GET\",null,\"Invalid Accept header\",\"4.0.4014\"]}}}";
        assertEquals(expectedResponseEntity,response.getEntity());
    }

    @Test
    public void testRetrieveSchema_VersionNotFound_ThrowsAAIException() {
        String version = "v2";
        String schema = "<schema>...</schema>";

        when(nodeSchemaService.fetch(version)).thenReturn(Optional.of(schema));
        when(schemaVersions.getVersions()).thenReturn(
            List.of(new SchemaVersion("v1"), new SchemaVersion("v3"))
        );

        Response response = nodeSchemaResource.retrieveSchema(version, headers, uriInfo);

        String expectedResponseEntity = "{\"requestError\":{\"serviceException\":{\"messageId\":\"SVC3000\",\"text\":\"Invalid input performing %1 on %2 (msg=%3) (ec=%4)\",\"variables\":[\"GET\",null,\"Invalid Accept header\",\"4.0.4014\"]}}}";
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(expectedResponseEntity,response.getEntity());
    }

    @Test
    public void testRetrieveSchema_SchemaNotFound_ThrowsAAIException() {
        String version = "v1";

        when(schemaVersions.getVersions()).thenReturn(
            List.of(new SchemaVersion("v1"), new SchemaVersion("v2"), new SchemaVersion("v3"))
        );
        when(nodeSchemaService.fetch(version)).thenReturn(Optional.empty());

        Response response = nodeSchemaResource.retrieveSchema(version, headers, uriInfo);
        String expectedEntityResponse = "{\"requestError\":{\"serviceException\":{\"messageId\":\"SVC3000\",\"text\":\"Invalid input performing %1 on %2 (msg=%3) (ec=%4)\",\"variables\":[\"GET\",null,\"Invalid Accept header\",\"4.0.4014\"]}}}";
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(expectedEntityResponse,response.getEntity());
    }
}
