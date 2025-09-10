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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.schemaservice.interceptors.AAIHeaderProperties;
import jakarta.ws.rs.container.ContainerRequestContext;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class HttpHeaderInterceptorTest {

    private HttpHeaderInterceptor httpHeaderInterceptor;

    @Mock
    private ContainerRequestContext mockRequestContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        httpHeaderInterceptor = new HttpHeaderInterceptor();
    }

    @Test
    void testFilter_WithPostMethodAndPatchOverride_ShouldSetMethodToPatch() throws IOException {
        // Setup: Mock the method to POST and the HTTP_METHOD_OVERRIDE header to PATCH
        when(mockRequestContext.getHeaderString(AAIHeaderProperties.HTTP_METHOD_OVERRIDE)).thenReturn("PATCH");
        when(mockRequestContext.getMethod()).thenReturn("POST");

        httpHeaderInterceptor.filter(mockRequestContext);

        verify(mockRequestContext).setMethod("PATCH");
    }

    @Test
    void testFilter_WithPostMethodAndNoOverride_ShouldNotChangeMethod() throws IOException {
        // Setup: Mock the method to POST and no HTTP_METHOD_OVERRIDE header (null or empty)
        when(mockRequestContext.getMethod()).thenReturn("POST");
        when(mockRequestContext.getHeaderString(AAIHeaderProperties.HTTP_METHOD_OVERRIDE)).thenReturn(null);

        httpHeaderInterceptor.filter(mockRequestContext);

        verify(mockRequestContext, never()).setMethod(anyString());
        assertEquals("POST", mockRequestContext.getMethod());
    }

    @Test
    void testFilter_WithPatchMethodAndNoOverride_ShouldNotChangeMethod() throws IOException {
        // Setup: Mock the method to PATCH and no override header
        when(mockRequestContext.getMethod()).thenReturn("PATCH");
        when(mockRequestContext.getHeaderString(AAIHeaderProperties.HTTP_METHOD_OVERRIDE)).thenReturn(null);

        httpHeaderInterceptor.filter(mockRequestContext);

        verify(mockRequestContext, never()).setMethod(anyString());
        assertEquals("PATCH", mockRequestContext.getMethod());

    }
}
