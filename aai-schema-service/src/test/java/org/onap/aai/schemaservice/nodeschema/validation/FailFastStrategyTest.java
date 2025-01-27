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
import static org.junit.jupiter.api.Assertions.*;

class FailFastStrategyTest {

    private FailFastStrategy failFastStrategy;

    @BeforeEach
    void setUp() {
        failFastStrategy = new FailFastStrategy();
    }

    @Test
    void testIsOK_WhenNoErrorOccurred() {
        assertTrue(failFastStrategy.isOK());
    }

    @Test
    void testGetErrorMsg_WhenNoErrorOccurred() {
        assertEquals("No errors found.", failFastStrategy.getErrorMsg());
    }

    @Test
    void testNotifyOnError_ShouldSetErrorState() {
        String errorMessage = "Validation failed!";
        AAISchemaValidationException exception = assertThrows(AAISchemaValidationException.class, () -> {
            failFastStrategy.notifyOnError(errorMessage);
        });

        assertEquals(errorMessage, exception.getMessage());
        assertFalse(failFastStrategy.isOK());
        assertEquals(errorMessage, failFastStrategy.getErrorMsg());
    }
}
