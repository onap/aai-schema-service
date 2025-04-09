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

import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.onap.aai.schemaservice.config.ConfigTranslator;
import org.w3c.dom.*;
import java.lang.reflect.Method;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NodeIngestorTest {

    @Mock
    private ConfigTranslator configTranslator;

    @Mock
    private Map<SchemaVersion, DynamicJAXBContext> versionContextMap;

    @Mock
    private Map<SchemaVersion, Set<String>> typesPerVersion;

    @InjectMocks
    private NodeIngestor nodeIngestor;

    @Mock
    DynamicJAXBContext context1;

    @Mock
    DynamicJAXBContext context2;


    private static final SchemaVersion version1 = new SchemaVersion("v1");
    private static final SchemaVersion version2 = new SchemaVersion("v2");


    @BeforeEach
    public void setUp() {
        when(versionContextMap.get(version1)).thenReturn(context1);
        when(versionContextMap.get(version2)).thenReturn(context2);

        Set<String> nodeTypesV1 = new HashSet<>(Arrays.asList("node-type-1", "node-type-2"));
        Set<String> nodeTypesV2 = new HashSet<>(Collections.singletonList("node-type-3"));

        when(typesPerVersion.get(version1)).thenReturn(nodeTypesV1);
        when(typesPerVersion.get(version2)).thenReturn(nodeTypesV2);

        when(configTranslator.getNodeFiles()).thenReturn(mockFileMap());
    }

    @Test
    public void testGetVersionFromClassNameWithVersion() {
        String className = "com.example.SomeClass.v1.SomeOtherClass";
        SchemaVersion result = nodeIngestor.getVersionFromClassName(className);
        assertNotNull(result, "SchemaVersion should not be null");
        assertEquals("v1", result.toString(), "The version extracted from class name should be v1");
    }

    @Test
    public void testCreateNode() throws Exception {
        Document mockDoc1 = mock(Document.class);
        Node mockNode1 = mock(Node.class);
        Node mockNode2 = mock(Node.class);

        NodeList mockNodeList = mock(NodeList.class);
        when(mockNodeList.getLength()).thenReturn(2);
        when(mockNodeList.item(0)).thenReturn(mockNode1);
        when(mockNodeList.item(1)).thenReturn(mockNode2);
        when(mockDoc1.getElementsByTagName("java-type")).thenReturn(mockNodeList);

        Node javaTypesContainer = mock(Node.class);
        Document combinedDoc = mock(Document.class);
        Map<String, Collection<Node>> mockNodeMap = new HashMap<>();
        mockNodeMap.put("node-type-1", List.of(mockNode1, mockNode2));

        Node mockImportedNode = mock(Node.class);
        when(combinedDoc.importNode(any(Node.class), eq(true))).thenReturn(mockImportedNode);

        Method createNodeMethod = NodeIngestor.class.getDeclaredMethod("createNode", Document.class, Node.class, Map.class);
        createNodeMethod.setAccessible(true);

        createNodeMethod.invoke(nodeIngestor, combinedDoc, javaTypesContainer, mockNodeMap);

        verify(javaTypesContainer, times(1)).appendChild(any(Node.class));
        verify(combinedDoc, times(2)).importNode(any(Node.class), eq(true));
    }

    private Map<SchemaVersion, List<String>> mockFileMap() {
        return Map.of(
            version1, List.of("file1.xml", "file2.xml"),
            version2, List.of("file3.xml", "file4.xml")
        );
    }
}
