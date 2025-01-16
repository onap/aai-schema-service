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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvalidResponseStatusTest {

    @InjectMocks
    private InvalidResponseStatus invalidResponseStatus;

    @Mock
    private ContainerRequestContext mockRequestContext;

    @Mock
    private ContainerResponseContext mockResponseContext;


    @Test
    public void testFilter_ResponseStatus405_ShouldChangeTo400() throws IOException {
        when(mockResponseContext.getStatus()).thenReturn(405);

        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        verify(mockResponseContext).setStatus(400);

        verify(mockResponseContext).setEntity(anyString());
    }

    @Test
    public void testFilter_ResponseStatus405_ShouldHandleContentType() throws IOException {
        String contentType = "application/json";
        when(mockResponseContext.getStatus()).thenReturn(405);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(contentType);

        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        verify(mockResponseContext).setStatus(400);

        verify(mockResponseContext).setEntity(anyString());
        assertEquals("application/json",mockResponseContext.getHeaderString("Content-Type"));
    }

    @Test
    public void testFilter_ContentTypeSetToNull_ShouldSetXmlContentType() throws IOException {
        // Setup: Simulate 405 status and no Content-Type header
        when(mockResponseContext.getStatus()).thenReturn(405);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(null);

        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        verify(mockResponseContext).setEntity(anyString());
        Assertions.assertNull(mockResponseContext.getHeaderString("Content-Type"));
    }

    @Test
    public void testFilter_ResponseStatus406_ContentTypeXml_ShouldSetXmlMessage() throws IOException {
        when(mockResponseContext.getStatus()).thenReturn(406);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn("application/xml");

        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        verify(mockResponseContext).setStatus(406);

        verify(mockResponseContext).setEntity(anyString());
        assertEquals("application/xml",mockResponseContext.getHeaderString("Content-Type"));
    }

    @Test
    public void testFilter_ResponseStatus406_ContentTypeJson_ShouldSetJsonMessage() throws IOException {
        // Setup: Simulate a 406 status and Content-Type header as "application/json"
        when(mockResponseContext.getStatus()).thenReturn(406);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn("application/json");

        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        verify(mockResponseContext).setStatus(406);

        verify(mockResponseContext).setEntity(anyString());
        assertEquals("application/json",mockResponseContext.getHeaderString("Content-Type"));
    }

    @Test
    public void testFilter_ResponseStatus406_ContentTypeUnsupported_ShouldReturnErrorMessage() throws IOException {
        when(mockResponseContext.getStatus()).thenReturn(406);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn("application/unsupported");

        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        verify(mockResponseContext).setStatus(406);

        verify(mockResponseContext).setEntity(anyString());
        assertEquals("application/unsupported",mockResponseContext.getHeaderString("Content-Type"));

    }
}
