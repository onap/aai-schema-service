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
package org.onap.aai.schemaservice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.aai.schemaservice.nodeschema.SchemaVersion;
import org.onap.aai.schemaservice.nodeschema.SchemaVersions;


import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AAIConfigTranslatorTest {

    private AAIConfigTranslator configTranslator;
    private SchemaLocationsBean mockSchemaLocationsBean;
    private SchemaVersion mockSchemaVersion;
    private SchemaVersions mockSchemaVersions;

    @BeforeEach
    void setUp() {
        mockSchemaLocationsBean = mock(SchemaLocationsBean.class);
        mockSchemaVersions = mock(SchemaVersions.class);
        mockSchemaVersion = mock(SchemaVersion.class);

        when(mockSchemaVersions.getVersions()).thenReturn(List.of(mockSchemaVersion));
        when(mockSchemaVersion.toString()).thenReturn("v1");

        configTranslator = new AAIConfigTranslator(mockSchemaLocationsBean,mockSchemaVersions);
    }


    @Test
    void testGetEdgeFiles_noFilesFound() {
        // Mock the schema locations
        String mockEdgeDirectory = "mock/edge/directory";
        when(mockSchemaLocationsBean.getEdgeDirectory()).thenReturn(mockEdgeDirectory);
        when(mockSchemaLocationsBean.getEdgesInclusionPattern()).thenReturn(List.of(".*\\.edge"));
        when(mockSchemaLocationsBean.getEdgesExclusionPattern()).thenReturn(Collections.emptyList());

        // Mock SchemaVersion
        when(mockSchemaVersion.toString()).thenReturn("v1");

        // Mock File behavior for the version directory
        File mockVersionDirectory = mock(File.class);

        // Simulate the behavior of an empty directory
        when(mockVersionDirectory.listFiles()).thenReturn(null); // Simulate empty directory
        when(mockVersionDirectory.getAbsolutePath()).thenReturn(mockEdgeDirectory + "/v1");

        // Simulate getParent() for files (if necessary)
        when(mockVersionDirectory.getParent()).thenReturn(mockEdgeDirectory + "/v1");

        // Expect RuntimeException to be thrown when there are no files
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            configTranslator.getEdgeFiles();
        });
    }

    @Test
    void testGetSchemaVersions() {
        // Act
        SchemaVersions result = configTranslator.getSchemaVersions();

        // Assert
        assertNotNull(result);
        assertEquals(mockSchemaVersions, result, "The returned SchemaVersions should match the mocked version.");
    }

    @Test
    void testGetNodeFiles_noFilesFound() {
        // Mock the schema locations
        String mockNodeDirectory = "mock/node/directory";
        when(mockSchemaLocationsBean.getNodeDirectory()).thenReturn(mockNodeDirectory);
        when(mockSchemaLocationsBean.getNodesInclusionPattern()).thenReturn(List.of(".*\\.json"));
        when(mockSchemaLocationsBean.getNodesExclusionPattern()).thenReturn(Collections.emptyList());
        when(mockSchemaVersion.toString()).thenReturn("v1");
        File mockVersionDirectory = mock(File.class);

        when(mockVersionDirectory.listFiles()).thenReturn(null);
        when(mockVersionDirectory.getAbsolutePath()).thenReturn(mockNodeDirectory + "/v1"); // Mock the expected path

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            configTranslator.getNodeFiles();
        });
    }

    private File mockFile(String fileName) {
        File file = mock(File.class);
        when(file.getName()).thenReturn(fileName);
        return file;
    }
}
