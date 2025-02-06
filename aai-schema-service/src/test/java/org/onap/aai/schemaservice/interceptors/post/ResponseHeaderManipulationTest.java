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
package org.onap.aai.schemaservice.interceptors.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResponseHeaderManipulationTest {

    @InjectMocks
    private ResponseHeaderManipulation responseHeaderManipulation;

    @Mock
    private ContainerRequestContext mockRequestContext;

    @Mock
    private ContainerResponseContext mockResponseContext;

    @Mock
    private MultivaluedMap<String, Object> mockResponseHeaders;

    @BeforeEach
    public void setUp() {
        when(mockResponseContext.getHeaders()).thenReturn(mockResponseHeaders);
    }

    @Test
    public void testFilterWithContentType() throws IOException {
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(MediaType.APPLICATION_JSON);
        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        assertEquals("application/json",mockResponseContext.getHeaderString("Content-Type"));
    }

    @Test
    public void testFilterWithNoContentType() throws IOException {
        // Arrange: Simulate Accept header with "*/*" and no Content-Type set
        when(mockRequestContext.getHeaderString("Accept")).thenReturn("*/*");

        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        assertEquals("*/*",mockRequestContext.getHeaderString("Accept"));
    }

    @Test
    public void testFilterWithNullAcceptHeader() throws IOException {
        // Arrange: Simulate null Accept header
        when(mockRequestContext.getHeaderString("Accept")).thenReturn(null);
        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        assertNull(mockRequestContext.getHeaderString("Accept"));
    }

    @Test
    public void testFilterWithEmptyAcceptHeader() throws IOException {
        // Arrange: Simulate empty Accept header and no Content-Type set
        when(mockRequestContext.getHeaderString("Accept")).thenReturn("");
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(null);

        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);
        assertEquals("", mockRequestContext.getHeaderString("Accept"));
    }

}
