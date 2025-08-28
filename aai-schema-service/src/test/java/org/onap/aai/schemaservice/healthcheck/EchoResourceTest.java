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
package org.onap.aai.schemaservice.healthcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.exceptions.AAIException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class EchoResourceTest {

    @InjectMocks
    private EchoResource echoResource;

    @Mock
    private HttpHeaders headers;

    @Mock
    private HttpServletRequest request;

    @Mock
    private UriInfo uriInfo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEchoResult_Success() throws AAIException {
        when(headers.getRequestHeader("X-FromAppId")).thenReturn(List.of("App1"));
        when(headers.getRequestHeader("X-TransactionId")).thenReturn(List.of("Trans1"));

        Response response = echoResource.echoResult(headers, request, uriInfo);

        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity().toString().contains("App1"));
        assertTrue(response.getEntity().toString().contains("Trans1"));
    }

    @Test
    void testEchoResult_MissingHeaders() throws AAIException {
        when(headers.getRequestHeader("X-FromAppId")).thenReturn(null);
        when(headers.getRequestHeader("X-TransactionId")).thenReturn(null);

        Response response = echoResource.echoResult(headers, request, uriInfo);

        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Headers missing"));
    }

    @Test
    void testEchoResult_ValidHeadersNoQuery() throws AAIException {
        when(headers.getRequestHeader("X-FromAppId")).thenReturn(List.of("App1"));
        when(headers.getRequestHeader("X-TransactionId")).thenReturn(List.of("Trans1"));

        Response response = echoResource.echoResult(headers, request, uriInfo);

        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity().toString().contains("App1"));
        assertTrue(response.getEntity().toString().contains("Trans1"));
    }
}
