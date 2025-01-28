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
package org.onap.aai.schemaservice.nodeschema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.aai.schemaservice.nodeschema.validation.AAISchemaValidationException;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SchemaVersionsTest {

    @InjectMocks
    private SchemaVersions schemaVersions;

    private List<String> mockApiVersions;
    private String mockDefaultVersion = "v1";
    private String mockEdgeLabelVersion = "v2";
    private String mockDepthVersion = "v3";
    private String mockAppRootVersion = "v4";
    private String mockRelatedLinkVersion = "v5";
    private String mockNamespaceChangeVersion = "v6";

    @BeforeEach
    void setUp() {
        mockApiVersions = Arrays.asList("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8");

        ReflectionTestUtils.setField(schemaVersions, "apiVersions", mockApiVersions);
        ReflectionTestUtils.setField(schemaVersions, "defaultApiVersion", mockDefaultVersion);
        ReflectionTestUtils.setField(schemaVersions, "edgeLabelStartVersion", mockEdgeLabelVersion);
        ReflectionTestUtils.setField(schemaVersions, "depthStartVersion", mockDepthVersion);
        ReflectionTestUtils.setField(schemaVersions, "appRootStartVersion", mockAppRootVersion);
        ReflectionTestUtils.setField(schemaVersions, "relatedLinkStartVersion", mockRelatedLinkVersion);
        ReflectionTestUtils.setField(schemaVersions, "namespaceChangeStartVersion", mockNamespaceChangeVersion);
    }

    @Test
    void testInitializeShouldNotThrowExceptionWhenValidVersions() {
        assertDoesNotThrow(() -> schemaVersions.initialize());
    }

    @Test
    void testInitializeShouldThrowExceptionWhenRelatedLinkVersionIsInvalid() {
        mockApiVersions = Arrays.asList("v1", "v2", "v3", "v4", "v6");
        ReflectionTestUtils.setField(schemaVersions, "apiVersions", mockApiVersions);
        assertThrows(AAISchemaValidationException.class, () -> schemaVersions.initialize(),
            "Invalid, related link version is not in the api versions list");
    }

    @Test
    void testInitializeShouldThrowExceptionWhenNamespaceChangeVersionIsInvalid() {
        mockApiVersions = Arrays.asList("v1", "v2", "v3", "v4", "v5");
        ReflectionTestUtils.setField(schemaVersions, "apiVersions", mockApiVersions);
        assertThrows(AAISchemaValidationException.class, () -> schemaVersions.initialize(),
            "Invalid, namespace change start version is not in the api versions list");
    }

    @Test
    void testInitializeShouldThrowExceptionWhenDefaultVersionIsInvalid() {
        mockApiVersions = Arrays.asList("v2", "v3", "v4", "v5", "v6");
        ReflectionTestUtils.setField(schemaVersions, "apiVersions", mockApiVersions);
        assertThrows(AAISchemaValidationException.class, () -> schemaVersions.initialize(),
            "Invalid, default version is not in the api versions list");
    }

    @Test
    void testInitializeShouldThrowExceptionWhenDepthVersionIsInvalid() {
        mockApiVersions = Arrays.asList("v1", "v2", "v4", "v5", "v6");
        ReflectionTestUtils.setField(schemaVersions, "apiVersions", mockApiVersions);
        assertThrows(AAISchemaValidationException.class, () -> schemaVersions.initialize(),
            "Invalid, depth version is not in the api versions list");
    }

    @Test
    void testGetVersionsReturnsCorrectVersions() {
        schemaVersions.initialize();
        List<SchemaVersion> versions = schemaVersions.getVersions();
        assertNotNull(versions);
        assertEquals(8, versions.size());
        assertEquals("v1", versions.get(0).toString());
        assertEquals("v2", versions.get(1).toString());
        assertEquals("v3", versions.get(2).toString());
        assertEquals("v4", versions.get(3).toString());
        assertEquals("v5", versions.get(4).toString());
        assertEquals("v6", versions.get(5).toString());
    }

    @Test
    void testGetEdgeLabelVersion() {
        schemaVersions.initialize();
        assertEquals(mockEdgeLabelVersion, schemaVersions.getEdgeLabelVersion().toString());
    }

    @Test
    void testGetDefaultVersion() {
        schemaVersions.initialize();
        assertEquals(mockDefaultVersion, schemaVersions.getDefaultVersion().toString());
    }

    @Test
    void testGetDepthVersion() {
        schemaVersions.initialize();
        assertEquals(mockDepthVersion, schemaVersions.getDepthVersion().toString());
    }

    @Test
    void testGetAppRootVersion() {
        schemaVersions.initialize();
        assertEquals(mockAppRootVersion, schemaVersions.getAppRootVersion().toString());
    }

    @Test
    void testGetRelatedLinkVersion() {
        schemaVersions.initialize();
        assertEquals(mockRelatedLinkVersion, schemaVersions.getRelatedLinkVersion().toString());
    }

    @Test
    void testGetNamespaceChangeVersion() {
        schemaVersions.initialize();
        assertEquals(mockNamespaceChangeVersion, schemaVersions.getNamespaceChangeVersion().toString());
    }

    @Test
    void testSetNamespaceChangeVersion() {
        SchemaVersion newNamespaceChangeVersion = new SchemaVersion("v7");
        schemaVersions.setNamespaceChangeVersion(newNamespaceChangeVersion);
        assertEquals("v7", schemaVersions.getNamespaceChangeVersion().toString());
    }

    @Test
    void testInitializeShouldThrowExceptionWhenEdgeLabelVersionIsInvalid() {
        mockApiVersions = Arrays.asList("v1", "v3", "v4", "v5", "v6");
        ReflectionTestUtils.setField(schemaVersions, "apiVersions", mockApiVersions);
        assertThrows(AAISchemaValidationException.class, () -> schemaVersions.initialize(),
            "Invalid, edge label version is not in the api versions list");
    }

    @Test
    void testInitializeShouldThrowExceptionWhenAppRootVersionIsInvalid() {
        mockApiVersions = Arrays.asList("v1", "v2", "v3", "v5", "v6");
        ReflectionTestUtils.setField(schemaVersions, "apiVersions", mockApiVersions);
        assertThrows(AAISchemaValidationException.class, () -> schemaVersions.initialize(),
            "Invalid, app root version is not in the api versions list");
    }
}
