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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.schemaservice.config.ConfigTranslator;
import org.onap.aai.schemaservice.nodeschema.SchemaVersion;
import org.onap.aai.schemaservice.nodeschema.SchemaVersions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultVersionValidationModuleTest {

    @Mock
    private ConfigTranslator configTranslator;

    @Mock
    private SchemaVersions schemaVersions;

    @InjectMocks
    private DefaultVersionValidationModule validationModule;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidate_AllSchemasPresent() {
        SchemaVersion version1 = new SchemaVersion("v1");
        SchemaVersion version2 = new SchemaVersion("v2");

        Map<SchemaVersion, List<String>> nodeFiles = new HashMap<>();
        nodeFiles.put(version1, Arrays.asList("nodeFile1"));
        nodeFiles.put(version2, Arrays.asList("nodeFile2"));

        Map<SchemaVersion, List<String>> edgeFiles = new HashMap<>();
        edgeFiles.put(version1, Arrays.asList("edgeFile1"));
        edgeFiles.put(version2, Arrays.asList("edgeFile2"));

        when(configTranslator.getNodeFiles()).thenReturn(nodeFiles);
        when(configTranslator.getEdgeFiles()).thenReturn(edgeFiles);
        when(configTranslator.getSchemaVersions()).thenReturn(schemaVersions);
        when(schemaVersions.getVersions()).thenReturn(Arrays.asList(version1, version2));

        String result = validationModule.validate();

        assertEquals("", result, "There should be no missing schemas.");
    }

    @Test
    void testValidate_NodeSchemaMissing() {
        SchemaVersion version1 = new SchemaVersion("v1");
        SchemaVersion version2 = new SchemaVersion("v2");

        Map<SchemaVersion, List<String>> nodeFiles = new HashMap<>();
        nodeFiles.put(version1, Arrays.asList("nodeFile1"));
        nodeFiles.put(version2, null);

        Map<SchemaVersion, List<String>> edgeFiles = new HashMap<>();
        edgeFiles.put(version1, Arrays.asList("edgeFile1"));
        edgeFiles.put(version2, Arrays.asList("edgeFile2"));

        when(configTranslator.getNodeFiles()).thenReturn(nodeFiles);
        when(configTranslator.getEdgeFiles()).thenReturn(edgeFiles);
        when(configTranslator.getSchemaVersions()).thenReturn(schemaVersions);
        when(schemaVersions.getVersions()).thenReturn(Arrays.asList(version1, version2));

        String result = validationModule.validate();

        assertEquals("Missing schema for the following versions: v2 has no OXM configured. ", result);
    }

    @Test
    void testValidate_EdgeSchemaMissing() {
        SchemaVersion version1 = new SchemaVersion("v1");
        SchemaVersion version2 = new SchemaVersion("v2");

        Map<SchemaVersion, List<String>> nodeFiles = new HashMap<>();
        nodeFiles.put(version1, Arrays.asList("nodeFile1"));
        nodeFiles.put(version2, Arrays.asList("nodeFile2"));

        Map<SchemaVersion, List<String>> edgeFiles = new HashMap<>();
        edgeFiles.put(version1, Arrays.asList("edgeFile1"));
        edgeFiles.put(version2, null);

        when(configTranslator.getNodeFiles()).thenReturn(nodeFiles);
        when(configTranslator.getEdgeFiles()).thenReturn(edgeFiles);
        when(configTranslator.getSchemaVersions()).thenReturn(schemaVersions);
        when(schemaVersions.getVersions()).thenReturn(Arrays.asList(version1, version2));

        String result = validationModule.validate();

        assertEquals("Missing schema for the following versions: v2 has no edge rules configured. ", result);
    }

    @Test
    void testValidate_BothNodeAndEdgeSchemasMissing() {
        SchemaVersion version1 = new SchemaVersion("v1");
        SchemaVersion version2 = new SchemaVersion("v2");

        Map<SchemaVersion, List<String>> nodeFiles = new HashMap<>();
        nodeFiles.put(version1, Arrays.asList("nodeFile1"));
        nodeFiles.put(version2, null);

        Map<SchemaVersion, List<String>> edgeFiles = new HashMap<>();
        edgeFiles.put(version1, Arrays.asList("edgeFile1"));
        edgeFiles.put(version2, null);

        when(configTranslator.getNodeFiles()).thenReturn(nodeFiles);
        when(configTranslator.getEdgeFiles()).thenReturn(edgeFiles);
        when(configTranslator.getSchemaVersions()).thenReturn(schemaVersions);
        when(schemaVersions.getVersions()).thenReturn(Arrays.asList(version1, version2));

        String result = validationModule.validate();

        assertEquals("Missing schema for the following versions: v2 has no OXM configured. v2 has no edge rules configured. ", result);
    }
}
