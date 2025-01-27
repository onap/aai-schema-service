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
package org.onap.aai.schemaservice.nodeschema.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class VersionValidatorTest {

    @Mock
    private SchemaErrorStrategy mockErrorStrategy;

    @Mock
    private VersionValidationModule mockVersionValidationModule;

    private VersionValidator versionValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        versionValidator = new VersionValidator(mockErrorStrategy, mockVersionValidationModule);
    }

    @Test
    void testValidate_NoError_ShouldReturnTrue() {
        when(mockVersionValidationModule.validate()).thenReturn("");
        when(mockErrorStrategy.isOK()).thenReturn(true);

        boolean result = versionValidator.validate();

        assertTrue(result);
        verify(mockErrorStrategy, never()).notifyOnError(anyString());
    }

    @Test
    void testValidate_WithError_ShouldReturnFalse() {
        String errorMessage = "Version validation failed";
        when(mockVersionValidationModule.validate()).thenReturn(errorMessage);
        when(mockErrorStrategy.isOK()).thenReturn(false);

        boolean result = versionValidator.validate();

        assertFalse(result);
        verify(mockErrorStrategy).notifyOnError(errorMessage);
    }

    @Test
    void testGetErrorMsg() {
        String errorMessage = "Error in version validation";
        when(mockErrorStrategy.getErrorMsg()).thenReturn(errorMessage);

        String errorMsg = versionValidator.getErrorMsg();

        assertEquals(errorMessage, errorMsg);
    }
}
