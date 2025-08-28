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
package org.onap.aai.schemaservice.interceptors.pre;

import org.glassfish.jersey.server.ContainerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.schemaservice.interceptors.AAIHeaderProperties;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestTransactionLoggingTest {

    @InjectMocks
    RequestTransactionLogging requestTransactionLogging;

    @Mock
    ContainerRequestContext containerRequestContext;

    @Mock
    UriInfo uriInfoMock;

    @Mock
    private MultivaluedMap<String, String> mockHeaders;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFilterMethod() throws UnsupportedEncodingException {

        String contentType = "application/json";
        String expectedTxId = "12345";
        String request ="request_id";
        String aaiRequestTs="aaiRequestId";
        String payload = "{\"key\":\"value\"}";

        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        when(containerRequestContext.getHeaderString("Content-Type")).thenReturn(contentType);
        when(containerRequestContext.getAcceptableMediaTypes()).thenReturn(acceptHeaderValues);
        when(containerRequestContext.getProperty(AAIHeaderProperties.AAI_TX_ID)).thenReturn(expectedTxId);
        when(containerRequestContext.getProperty(AAIHeaderProperties.AAI_REQUEST)).thenReturn(request);
        when(containerRequestContext.getProperty(AAIHeaderProperties.AAI_REQUEST_TS)).thenReturn(aaiRequestTs);

        when(containerRequestContext.getHeaders()).thenReturn(mockHeaders);

        when(mockHeaders.containsKey("Content-Type")).thenReturn(false);
        when(mockHeaders.containsKey("Accept")).thenReturn(true);

        // Mocking UriInfo methods
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost/some/other/path"));
        when(uriInfoMock.getPath()).thenReturn("/some/other/path");

        InputStream inputStream = new ByteArrayInputStream(payload.getBytes("UTF-8"));
        when(containerRequestContext.getEntityStream()).thenReturn(inputStream);

        requestTransactionLogging.filter(containerRequestContext);

        // Assertions for headers
        assertNotNull(mockHeaders, "Headers should not be null.");
        assertNull(mockHeaders.getFirst("Content-Type"), "Content-Type should initially be null.");
    }

    @Test
    public void testFilterMethodWithIOException() throws IOException {

        String contentType = "application/json";
        String expectedTxId = "12345";
        String request = "request_id";
        String aaiRequestTs = "aaiRequestId";
        String payload = "{\"key\":\"value\"}";

        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        // Mocking containerRequestContext methods
        when(containerRequestContext.getHeaderString("Content-Type")).thenReturn(contentType);
        when(containerRequestContext.getAcceptableMediaTypes()).thenReturn(acceptHeaderValues);
        when(containerRequestContext.getProperty(AAIHeaderProperties.AAI_TX_ID)).thenReturn(expectedTxId);
        when(containerRequestContext.getProperty(AAIHeaderProperties.AAI_REQUEST)).thenReturn(request);
        when(containerRequestContext.getProperty(AAIHeaderProperties.AAI_REQUEST_TS)).thenReturn(aaiRequestTs);

        // Mocking the getHeaders() method to return a non-null headers object
        when(containerRequestContext.getHeaders()).thenReturn(mockHeaders);
        when(mockHeaders.getFirst("Content-Type")).thenReturn(null);  // This is to simulate the header being absent
        when(mockHeaders.getFirst("Accept")).thenReturn("application/xml");

        doNothing().when(mockHeaders).putSingle(anyString(), anyString());
        when(mockHeaders.containsKey("Content-Type")).thenReturn(false);
        when(mockHeaders.containsKey("Accept")).thenReturn(true);

        // Mocking UriInfo methods
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost/some/other/path"));
        when(uriInfoMock.getPath()).thenReturn("/some/other/path");

        // Mocking getEntityStream to throw an IOException
        InputStream inputStream = mock(InputStream.class);
        when(inputStream.available()).thenThrow(new IOException("Test IOException"));
        when(containerRequestContext.getEntityStream()).thenReturn(inputStream);

        assertThrows(ContainerException.class, () -> {
            requestTransactionLogging.filter(containerRequestContext);
        });
    }
}
