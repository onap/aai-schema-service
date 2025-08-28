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
package org.onap.aai.schemagen.swagger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.aai.setup.SchemaConfigVersions;
import org.onap.aai.setup.SchemaVersion;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class GenerateSwaggerTest {

    private GenerateSwagger generateSwagger;
    private SchemaConfigVersions mockSchemaConfigVersions;

    @BeforeEach
    void setUp() {
        // Initialize the GenerateSwagger instance before each test
        generateSwagger = new GenerateSwagger();
        mockSchemaConfigVersions = mock(SchemaConfigVersions.class);
        GenerateSwagger.schemaConfigVersions = mockSchemaConfigVersions;
    }

    @Test
    void testGetSchemaConfigVersions() {
        // Act
        SchemaConfigVersions result = generateSwagger.getSchemaConfigVersions();

        // Assert
        assertNotNull(result);
        assertEquals(mockSchemaConfigVersions, result);
    }

    @Test
    void testConvertToApiWithValidData() {
        // Arrange
        Map<String, Object> pathMap = new HashMap<>();
        Map<String, Object> httpVerbMap = new HashMap<>();
        Map<String, Object> getMap = new HashMap<>();
        getMap.put("tags", Arrays.asList("tag1"));
        getMap.put("summary", "Summary of GET");
        getMap.put("operationId", "getOp");

        httpVerbMap.put("get", getMap);
        pathMap.put("/path", httpVerbMap);

        // Act
        List<Api> apis = GenerateSwagger.convertToApi(pathMap);

        // Assert
        assertNotNull(apis);
        assertEquals(1, apis.size());
        Api api = apis.get(0);
        assertEquals("/path", api.getPath());
        assertEquals(1, api.getHttpMethods().size());
        Api.HttpVerb httpVerb = api.getHttpMethods().get(0);
        assertEquals("get", httpVerb.getType());
        assertEquals("Summary of GET", httpVerb.getSummary());
        assertEquals("getOp", httpVerb.getOperationId());
    }

    @Test
    void testConvertToApiWithNullPathMap() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            GenerateSwagger.convertToApi(null);
        });
    }

    @Test
    void testConvertToDefinitionWithValidData() {
        // Arrange
        Map<String, Object> definitionMap = new HashMap<>();
        Map<String, Object> propertiesMap = new HashMap<>();
        Map<String, Object> property = new HashMap<>();
        property.put("description", "A sample description");
        property.put("type", "string");

        propertiesMap.put("property1", property);
        definitionMap.put("Definition1", Map.of("properties", propertiesMap, "required", Arrays.asList("property1")));

        // Act
        List<Definition> definitions = GenerateSwagger.convertToDefinition(definitionMap);

        // Assert
        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        Definition definition = definitions.get(0);
        assertEquals("Definition1", definition.getDefinitionName());
        assertEquals(1, definition.getPropertyList().size());
        Definition.Property propertyDef = definition.getPropertyList().get(0);
        assertEquals("property1", propertyDef.getPropertyName());
        assertEquals("string", propertyDef.getPropertyType());
        assertTrue(propertyDef.isRequired());
    }

    @Test
    void testConvertToDefinitionWithNullDefinitionMap() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            GenerateSwagger.convertToDefinition(null);
        });
    }

    @Test
    void testFormatDescriptionWithValidDescription() {
        // Arrange
        String description = "###### Header\n- Item 1\n- Item 2\n";

        // Act
        String formattedDescription = GenerateSwagger.formatDescription(description);

        // Assert
        assertTrue(formattedDescription.contains("<h6 id=\"header\">Header</h6>"));
        assertTrue(formattedDescription.contains("<li>Item 1</li>"));
        assertTrue(formattedDescription.contains("<li>Item 2</li>"));
    }

    @Test
    void testFormatDescriptionWithEmptyDescription() {
        // Arrange
        String description = "";
        String formattedDescription = GenerateSwagger.formatDescription(description);
        assertEquals("<p></p>", formattedDescription);
    }

    @Test
    void testFormatDescriptionWithComplexInput() {
        // Arrange
        String description = "###### Header\nThis is a description with a [link](http://example.com).\n";
        String formattedDescription = GenerateSwagger.formatDescription(description);
        assertTrue(formattedDescription.contains("<h6 id=\"header\">Header</h6>"));
        assertFalse(formattedDescription.contains("<p>This is a description with a <a href=\"http://example.com\">link</a>.</p>"));
    }

    @Test
    void testConvertToApiConsumes() {
        // Arrange
        List<String> consumesList = Arrays.asList("application/json");
        Map<String, Object> httpVerbMap = new HashMap<>();
        Map<String, Object> getMap = new HashMap<>();
        getMap.put("consumes", consumesList);
        httpVerbMap.put("get", getMap);

        Map<String, Object> pathMap = new HashMap<>();
        pathMap.put("/path", httpVerbMap);

        // Act
        List<Api> apis = GenerateSwagger.convertToApi(pathMap);

        // Assert
        assertNotNull(apis);
        assertEquals(1, apis.size());
        Api api = apis.get(0);
        Api.HttpVerb httpVerb = api.getHttpMethods().get(0);

        assertEquals(consumesList, httpVerb.getConsumes());
        assertTrue(httpVerb.isConsumerEnabled());
    }

    @Test
    void testConvertToApiProduces() {
        // Arrange
        List<String> producesList = Arrays.asList("application/json");
        Map<String, Object> httpVerbMap = new HashMap<>();
        Map<String, Object> getMap = new HashMap<>();
        getMap.put("produces", producesList);
        httpVerbMap.put("get", getMap);

        Map<String, Object> pathMap = new HashMap<>();
        pathMap.put("/path", httpVerbMap);

        // Act
        List<Api> apis = GenerateSwagger.convertToApi(pathMap);

        // Assert
        assertNotNull(apis);
        assertEquals(1, apis.size());
        Api api = apis.get(0);
        Api.HttpVerb httpVerb = api.getHttpMethods().get(0);

        assertEquals(producesList, httpVerb.getProduces());
    }

    @Test
    void testConvertToApiParametersWithBody() {
        // Arrange
        Map<String, Object> bodyParam = new LinkedHashMap<>();
        bodyParam.put("name", "body");
        bodyParam.put("description", "Request body [somefile.json]");

        Map<String, Object> schemaMap = new LinkedHashMap<>();
        schemaMap.put("$ref", "patchDefinitions/mySchema");

        bodyParam.put("schema", schemaMap);

        List<Map<String, Object>> parameters = Arrays.asList(bodyParam);
        Map<String, Object> httpVerbMap = new LinkedHashMap<>();
        Map<String, Object> getMap = new LinkedHashMap<>();
        getMap.put("parameters", parameters);
        httpVerbMap.put("get", getMap);

        Map<String, Object> pathMap = new LinkedHashMap<>();
        pathMap.put("/path", httpVerbMap);

        // Act
        List<Api> apis = GenerateSwagger.convertToApi(pathMap);

        // Assert
        assertNotNull(apis);
        assertEquals(1, apis.size());
        Api api = apis.get(0);
        Api.HttpVerb httpVerb = api.getHttpMethods().get(0);

        // Assert that the body parameters are handled correctly
        assertTrue(httpVerb.isBodyParametersEnabled());
        assertEquals(bodyParam, httpVerb.getBodyParameters());
        assertEquals("definitions/mySchema", httpVerb.getSchemaLink());
        assertEquals("/mySchema", httpVerb.getSchemaType());
    }

    @Test
    void testConvertToApiParametersWithoutBody() {
        // Arrange
        Map<String, Object> param1 = new HashMap<>();
        param1.put("name", "param1");
        param1.put("description", "First parameter");

        List<Map<String, Object>> parameters = Arrays.asList(param1);
        Map<String, Object> httpVerbMap = new HashMap<>();
        Map<String, Object> getMap = new HashMap<>();
        getMap.put("parameters", parameters);
        httpVerbMap.put("get", getMap);

        Map<String, Object> pathMap = new HashMap<>();
        pathMap.put("/path", httpVerbMap);

        // Act
        List<Api> apis = GenerateSwagger.convertToApi(pathMap);

        // Assert
        assertNotNull(apis);
        assertEquals(1, apis.size());
        Api api = apis.get(0);
        Api.HttpVerb httpVerb = api.getHttpMethods().get(0);

        // Assert parameters are handled correctly
        assertTrue(httpVerb.isParametersEnabled());
        assertEquals(1, httpVerb.getParameters().size());
    }

    @Test
    void testConvertToApiResponses() {
        // Arrange
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> schemaMap = new HashMap<>();
        schemaMap.put("$ref", "getDefinitions/myResponseSchema");

        responseMap.put("200", Map.of("description", "Success", "schema", schemaMap));

        Map<String, Object> httpVerbMap = new HashMap<>();
        Map<String, Object> getMap = new HashMap<>();
        getMap.put("responses", responseMap);
        httpVerbMap.put("get", getMap);

        Map<String, Object> pathMap = new HashMap<>();
        pathMap.put("/path", httpVerbMap);

        // Act
        List<Api> apis = GenerateSwagger.convertToApi(pathMap);

        // Assert
        assertNotNull(apis);
        assertEquals(1, apis.size());
        Api api = apis.get(0);
        Api.HttpVerb httpVerb = api.getHttpMethods().get(0);

        assertTrue(httpVerb.isHasReturnSchema());
        assertEquals("definitions/myResponseSchema", httpVerb.getReturnSchemaLink());
    }

    @Test
    void testConvertToApiNoResponses() {
        // Arrange
        Map<String, Object> httpVerbMap = new LinkedHashMap<>();
        Map<String, Object> getMap = new LinkedHashMap<>();
        getMap.put("responses", new LinkedHashMap<>());  // Empty responses map

        httpVerbMap.put("get", getMap);
        Map<String, Object> pathMap = new LinkedHashMap<>();
        pathMap.put("/path", httpVerbMap);

        // Act
        List<Api> apis = GenerateSwagger.convertToApi(pathMap);

        // Assert
        assertNotNull(apis);
        Api api = apis.get(0);
        Api.HttpVerb httpVerb = api.getHttpMethods().get(0);

        // Assert that the responses list is empty
        assertTrue(httpVerb.getResponses().isEmpty());  // Check if the list is empty
    }
}
