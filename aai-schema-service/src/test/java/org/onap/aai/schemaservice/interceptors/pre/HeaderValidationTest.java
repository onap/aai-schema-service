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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.onap.aai.restcore.MediaType;
import org.onap.aai.schemaservice.interceptors.AAIHeaderProperties;
import org.onap.logging.filter.base.Constants;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HeaderValidationTest {

    @InjectMocks
    private HeaderValidation headerValidation;

    @Mock
    private ContainerRequestContext requestContext;

    private MultivaluedMap<String, String> headers;

    @BeforeEach
    public void setUp() {
        headers = new MultivaluedHashMap<>();
        when(requestContext.getHeaders()).thenReturn(headers);
        UriInfo mockUriInfo = mock(UriInfo.class);
        when(mockUriInfo.getPath()).thenReturn("default/path");
        when(requestContext.getUriInfo()).thenReturn(mockUriInfo);
    }

    @Test
    public void testGetPartnerName_withEmptyPartnerName() {
        when(requestContext.getHeaderString("X-ONAP-PartnerName")).thenReturn("");
        when(requestContext.getHeaderString("X-FromAppId")).thenReturn("testAppId");

        String partnerName = headerValidation.getPartnerName(requestContext);

        assertEquals("testAppId", partnerName);
    }

    @Test
    public void testGetPartnerName_withNullPartnerNameAndFromAppId() {
        // Mock behavior of getHeaderString to return null for both PARTNER_NAME and FROM_APP_ID
        when(requestContext.getHeaderString("X-ONAP-PartnerName")).thenReturn(null);
        when(requestContext.getHeaderString("X-FromAppId")).thenReturn("testAppId");

        String partnerName = headerValidation.getPartnerName(requestContext);

        assertEquals("testAppId", partnerName);
    }

    @Test
    public void testGetPartnerName_withMissingPartnerNameAndFromAppId() {
        // Mock behavior of getHeaderString to return null for both PARTNER_NAME and FROM_APP_ID
        when(requestContext.getHeaderString("X-ONAP-PartnerName")).thenReturn(null);

        String partnerName = headerValidation.getPartnerName(requestContext);

        assertNull(partnerName);
    }

    @Test
    public void testGetRequestId_withValidRequestId() {
        // Mock behavior of getHeaderString to return a valid request ID
        when(requestContext.getHeaderString("X-ONAP-RequestID")).thenReturn("testRequestId");

        String requestId = headerValidation.getRequestId(requestContext);

        assertEquals("testRequestId", requestId);
    }

    @Test
    public void testFilter_withMissingPartnerName() throws IOException {
        // Mock behavior for missing PartnerName header
        when(requestContext.getHeaderString("X-ONAP-PartnerName")).thenReturn("");
        when(requestContext.getHeaderString("X-FromAppId")).thenReturn("testAppId");
        when(requestContext.getHeaderString("X-ONAP-RequestId")).thenReturn("testRequestId");

        headerValidation.filter(requestContext);

        // Verify that the method calls abortWith due to the missing partner name
        verify(requestContext).abortWith(argThat(response -> response.getStatus() == 400));
    }

    @Test
    void testGetRequestId_ClearsExistingHeaders() {
        // Arrange
        String expectedRequestId = "test-request-id";
        headers.put(ONAPLogConstants.Headers.REQUEST_ID, new ArrayList<>());
        headers.put(Constants.HttpHeaders.TRANSACTION_ID, new ArrayList<>());
        headers.put(Constants.HttpHeaders.HEADER_REQUEST_ID, new ArrayList<>());
        headers.put(Constants.HttpHeaders.ECOMP_REQUEST_ID, new ArrayList<>());

        when(requestContext.getHeaderString(ONAPLogConstants.Headers.REQUEST_ID))
            .thenReturn(expectedRequestId);

        String actualRequestId = headerValidation.getRequestId(requestContext);

        assertEquals(expectedRequestId, actualRequestId);
        verify(requestContext, atLeastOnce()).getHeaders();
        assertTrue(headers.get(ONAPLogConstants.Headers.REQUEST_ID).isEmpty());
        assertTrue(headers.get(Constants.HttpHeaders.TRANSACTION_ID).contains(expectedRequestId));
        assertTrue(headers.get(Constants.HttpHeaders.HEADER_REQUEST_ID).isEmpty());
        assertTrue(headers.get(Constants.HttpHeaders.ECOMP_REQUEST_ID).isEmpty());
    }

    @Test
    void testGetRequestId_WhenEcompRequestIdExists() {
        String expectedRequestId = "ecomp-123";
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.REQUEST_ID))
            .thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.HEADER_REQUEST_ID))
            .thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.TRANSACTION_ID))
            .thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.ECOMP_REQUEST_ID))
            .thenReturn(expectedRequestId);

        String result = headerValidation.getRequestId(requestContext);

        assertEquals(expectedRequestId, result);
    }

    @Test
    void whenPartnerNameHasValidComponents_shouldReturnFirstComponent() {
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn("TEST.COMPONENT");

        String result = headerValidation.getPartnerName(requestContext);

        assertEquals("TEST.COMPONENT", result);
    }

    @Test
    void whenPartnerNameStartsWithAAI_shouldUseFromAppId() {
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn("AAI.COMPONENT");

        String result = headerValidation.getPartnerName(requestContext);

        assertEquals("AAI.COMPONENT", result);
    }

    @Test
    void shouldClearAndUpdateHeaders() {
        List<String> oldValues = new ArrayList<>();
        oldValues.add("OLD-VALUE");
        headers.put(ONAPLogConstants.Headers.PARTNER_NAME, oldValues);
        headers.put(AAIHeaderProperties.FROM_APP_ID, oldValues);

        when(requestContext.getHeaderString(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn("NEW-SOT");

        String result = headerValidation.getPartnerName(requestContext);

        assertEquals("NEW-SOT", result);
        assertEquals("NEW-SOT", headers.getFirst(AAIHeaderProperties.FROM_APP_ID));
    }


    @Test
    public void testGetRequestId_withValidHeaderRequestId() {
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.REQUEST_ID)).thenReturn("");
        when(requestContext.getHeaderString(Constants.HttpHeaders.HEADER_REQUEST_ID)).thenReturn("validHeaderRequestId");

        String result = headerValidation.getRequestId(requestContext);

        assertEquals("validHeaderRequestId",result);
    }

    @Test
    public void testGetRequestIdNull_withValidTransactionId() {
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.REQUEST_ID)).thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.HEADER_REQUEST_ID)).thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.TRANSACTION_ID)).thenReturn("validTransactionId");

        String result = headerValidation.getRequestId(requestContext);

        assertEquals("validTransactionId",result);
    }

    @Test
    public void testGetRequestIdEmpty_withValidTransactionId() {
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.REQUEST_ID)).thenReturn("");
        when(requestContext.getHeaderString(Constants.HttpHeaders.HEADER_REQUEST_ID)).thenReturn("");
        when(requestContext.getHeaderString(Constants.HttpHeaders.TRANSACTION_ID)).thenReturn("validTransactionId");

        String result = headerValidation.getRequestId(requestContext);

        assertEquals("validTransactionId",result);
    }

    @Test
    public void testGetRequestId_withValidEcompRequestId() {

        when(requestContext.getHeaderString(ONAPLogConstants.Headers.REQUEST_ID)).thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.HEADER_REQUEST_ID)).thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.TRANSACTION_ID)).thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.ECOMP_REQUEST_ID)).thenReturn("validEcompRequestId");

        String result = headerValidation.getRequestId(requestContext);

        assertEquals("validEcompRequestId",result);
    }

    @Test
    public void testGetRequestId_withValidEcompRequestIdAfterEmptyTransactionId() {
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.REQUEST_ID)).thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.HEADER_REQUEST_ID)).thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.TRANSACTION_ID)).thenReturn("");
        when(requestContext.getHeaderString(Constants.HttpHeaders.ECOMP_REQUEST_ID)).thenReturn("validEcompRequestId");

        String result = headerValidation.getRequestId(requestContext);

        assertEquals("validEcompRequestId",result);
    }

    @Test
    public void testGetRequestId_withMultipleHeadersValid() {
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.REQUEST_ID)).thenReturn("validRequestId");
        when(requestContext.getHeaderString(Constants.HttpHeaders.HEADER_REQUEST_ID)).thenReturn("anotherRequestId");

        String result = headerValidation.getRequestId(requestContext);
        assertEquals("validRequestId", result, "Expected validRequestId to be returned as it's the first non-null value");

    }
    @Test
    public void testGetPartnerName_withNullPartnerNameAndNullFromAppId() {
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn(null);
        when(requestContext.getHeaderString(AAIHeaderProperties.FROM_APP_ID)).thenReturn(null);

        String partnerName = headerValidation.getPartnerName(requestContext);
        assertNull(partnerName, "Expected null partner name when both PARTNER_NAME and FROM_APP_ID are null");
    }

    @Test
    public void testGetPartnerName_withNonEmptyPartnerName() {
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn("partnerName");
        when(requestContext.getHeaderString(AAIHeaderProperties.FROM_APP_ID)).thenReturn("testAppId");
        String partnerName = headerValidation.getPartnerName(requestContext);

        assertEquals("partnerName", partnerName, "Expected partner name to be used directly when PARTNER_NAME is not empty");
    }

    @Test
    public void testGetPartnerName_withNullPartnerNameAndNonEmptyFromAppId() {
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn(null);
        when(requestContext.getHeaderString(AAIHeaderProperties.FROM_APP_ID)).thenReturn("testAppId");

        String partnerName = headerValidation.getPartnerName(requestContext);
        assertEquals("testAppId", partnerName, "Expected partner name to fall back to FROM_APP_ID when PARTNER_NAME is null");
    }

    @Test
    public void testGetPartnerName_withEmptyFromAppId() {
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn("validPartnerName");
        when(requestContext.getHeaderString(AAIHeaderProperties.FROM_APP_ID)).thenReturn("");
        String partnerName = headerValidation.getPartnerName(requestContext);

        assertEquals("validPartnerName", partnerName, "Expected valid PARTNER_NAME to be used even when FROM_APP_ID is empty");
    }

    @Test
    public void testGetPartnerName_withValidPartnerNameWithDot() {
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn("TEST.COMPONENT");
        when(requestContext.getHeaderString(AAIHeaderProperties.FROM_APP_ID)).thenReturn("testAppId");

        String partnerName = headerValidation.getPartnerName(requestContext);

        assertEquals("TEST.COMPONENT", partnerName, "Expected partner name to return 'TEST.COMPONENT' when PARTNER_NAME is valid");
    }
    @Test
    public void testValidateHeaderValuePresence_usingReflection() throws Exception {

        Method method = HeaderValidation.class.getDeclaredMethod("validateHeaderValuePresence", String.class, String.class, List.class);
        method.setAccessible(true);
        String value = "testValue";
        String errorCode = "AAI_4009";
        List<MediaType> acceptHeaderValues = List.of(MediaType.APPLICATION_JSON_TYPE);
        Optional<Response> result = (Optional<Response>) method.invoke(headerValidation, value, errorCode, acceptHeaderValues);
        assertNotNull(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"swagger-ui.html", "openapi.json"})
    public void testFilter_skipsSafeEndpoints(String safePath) throws IOException {
        when(requestContext.getUriInfo().getPath()).thenReturn(safePath);
        headerValidation.filter(requestContext);
        verify(requestContext, never()).abortWith(any(Response.class));
    }


}
