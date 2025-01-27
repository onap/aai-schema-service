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
import org.onap.aai.schemaservice.config.ConfigTranslator;
import org.onap.aai.schemaservice.nodeschema.SchemaVersion;
import java.util.*;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class NodeValidatorTest {

    @Mock
    private ConfigTranslator mockTranslator;

    @Mock
    private SchemaErrorStrategy mockErrorStrategy;

    @Mock
    private DuplicateNodeDefinitionValidationModule mockDupChecker;

    private NodeValidator nodeValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        nodeValidator = new NodeValidator(mockTranslator, mockErrorStrategy, mockDupChecker);
    }

    @Test
    void testValidate_NoDuplicates_ShouldReturnTrue() {
        SchemaVersion schemaVersion = mock(SchemaVersion.class);
        List<String> nodeFiles = Arrays.asList("file1", "file2", "file3");

        Map<SchemaVersion, List<String>> mockNodeFiles = new HashMap<>();
        mockNodeFiles.put(schemaVersion, nodeFiles);

        when(mockTranslator.getNodeFiles()).thenReturn(mockNodeFiles);
        when(mockDupChecker.findDuplicates(nodeFiles, schemaVersion)).thenReturn("");

        boolean result = nodeValidator.validate();
        assertFalse(result);

        verify(mockErrorStrategy, never()).notifyOnError(anyString());
    }

    @Test
    void testValidate_WithDuplicates_ShouldReturnFalse() {
        SchemaVersion schemaVersion = mock(SchemaVersion.class);
        List<String> nodeFiles = Arrays.asList("file1", "file2", "file3");

        Map<SchemaVersion, List<String>> mockNodeFiles = new HashMap<>();
        mockNodeFiles.put(schemaVersion, nodeFiles);

        when(mockTranslator.getNodeFiles()).thenReturn(mockNodeFiles);
        when(mockDupChecker.findDuplicates(nodeFiles, schemaVersion)).thenReturn("Duplicate found!");
        boolean result = nodeValidator.validate();

        assertFalse(result);
        verify(mockErrorStrategy).notifyOnError("Duplicate found!");
    }

    @Test
    void testGetErrorMsg_WithoutError_ShouldReturnEmptyString() {
        when(mockErrorStrategy.getErrorMsg()).thenReturn("");

        String errorMsg = nodeValidator.getErrorMsg();

        assertEquals("", errorMsg);
    }

    @Test
    void testGetErrorMsg_WithError_ShouldReturnErrorMessage() {
        when(mockErrorStrategy.getErrorMsg()).thenReturn("Some error occurred");

        String errorMsg = nodeValidator.getErrorMsg();

        assertEquals("Some error occurred", errorMsg);
    }
}
