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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.onap.aai.schemaservice.service.AuthorizationService;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OneWaySslAuthorizationTest {

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private ContainerRequestContext containerRequestContext;

    @InjectMocks
    private OneWaySslAuthorization oneWaySslAuthorization;

    @BeforeEach
    public void setUp() {
        lenient().when(authorizationService.checkIfUserAuthorized(anyString())).thenReturn(true);
    }

    @Test
    public void testFilterWithValidBasicAuth() throws Exception {
        String basicAuth = "Basic validToken";
        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        when(containerRequestContext.getHeaderString("Authorization")).thenReturn(basicAuth);
        when(containerRequestContext.getAcceptableMediaTypes()).thenReturn(acceptHeaderValues);

        UriInfo uriInfoMock = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost/some/other/path"));
        when(uriInfoMock.getPath()).thenReturn("/some/other/path");

        oneWaySslAuthorization.filter(containerRequestContext);

        verify(containerRequestContext, times(0)).abortWith(any());
    }

    @Test
    public void testFilterWithInvalidBasicAuth() throws Exception {
        String basicAuth = "Basic invalidToken";
        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        UriInfo uriInfoMock = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost/some/other/path"));
        when(uriInfoMock.getPath()).thenReturn("/some/other/path");

        when(containerRequestContext.getHeaderString("Authorization")).thenReturn(basicAuth);
        when(containerRequestContext.getAcceptableMediaTypes()).thenReturn(acceptHeaderValues);

        when(authorizationService.checkIfUserAuthorized("invalidToken")).thenReturn(false);

        oneWaySslAuthorization.filter(containerRequestContext);

        verify(containerRequestContext, times(1)).abortWith(any(Response.class));
    }

    @Test
    public void testFilterWithNoAuthorizationHeader() throws Exception {
        String basicAuth = null;
        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        when(containerRequestContext.getHeaderString("Authorization")).thenReturn(basicAuth);
        when(containerRequestContext.getAcceptableMediaTypes()).thenReturn(acceptHeaderValues);

        UriInfo uriInfoMock = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost/some/other/path"));
        when(uriInfoMock.getPath()).thenReturn("/some/other/path");

        oneWaySslAuthorization.filter(containerRequestContext);

        verify(containerRequestContext, times(1)).abortWith(any(Response.class));
    }

    @Test
    public void testFilterWithInvalidAuthorizationHeaderFormat() throws Exception {
        String basicAuth = "Bearer invalidToken"; // Header doesn't start with "Basic "
        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        when(containerRequestContext.getHeaderString("Authorization")).thenReturn(basicAuth);
        when(containerRequestContext.getAcceptableMediaTypes()).thenReturn(acceptHeaderValues);

        UriInfo uriInfoMock = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost/some/other/path"));
        when(uriInfoMock.getPath()).thenReturn("/some/other/path");

        oneWaySslAuthorization.filter(containerRequestContext);

        verify(containerRequestContext, times(1)).abortWith(any(Response.class));
    }

    @Test
    public void testFilterForEchoPath() throws Exception {
        String path = "/util/echo";

        UriInfo uriInfoMock = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost" + path));
        when(uriInfoMock.getPath()).thenReturn(path);

        oneWaySslAuthorization.filter(containerRequestContext);

        verify(containerRequestContext, times(0)).abortWith(any());
    }

    @Test
    public void testErrorResponse() throws Exception {
        Method errorResponseMethod = OneWaySslAuthorization.class.getDeclaredMethod("errorResponse", String.class, List.class);
        errorResponseMethod.setAccessible(true);

        String errorCode = "AAI_3300";
        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        Object result = errorResponseMethod.invoke(oneWaySslAuthorization, errorCode, acceptHeaderValues);

        assertTrue(result instanceof Optional);
        Optional<Response> responseOptional = (Optional<Response>) result;
        assertTrue(responseOptional.isPresent());
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), responseOptional.get().getStatus());
    }
}

