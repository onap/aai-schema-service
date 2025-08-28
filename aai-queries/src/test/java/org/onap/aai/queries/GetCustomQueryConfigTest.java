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
package org.onap.aai.queries;

import static org.junit.jupiter.api.Assertions.*;
import com.google.common.collect.Lists;
import java.lang.reflect.Method;
import java.util.ArrayList;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class GetCustomQueryConfigTest {

    private String configJson;

    private GetCustomQueryConfig getCustomQueryConfig;


    @BeforeEach
    public void setUp() throws Exception {
        System.setProperty("AJSC_HOME", ".");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources");

        configJson = """
            {
            	"stored-queries": [{
            		"queryName1": {
            			"query": {
            				"required-properties": ["prop1", "prop2"],
            				"optional-properties": ["prop3", "prop4"]
            			},
            			"stored-query": "out('blah').has('something','foo')"
            		}
            	}, {
            		"queryName2": {
            			"query": {
            				"optional-properties": ["prop5"]
            			},
            			"stored-query": "out('bar').has('stuff','baz')"
            		}
            	}, {
            		"queryName3": {
            			"stored-query": "out('bar1').has('stuff','baz1')"
            		}
            	}]
            }\
            """;

        getCustomQueryConfig = new GetCustomQueryConfig(configJson);

    }

    @Test
    public void testGetStoredQueryNameWithOptAndReqProps() {
        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(configJson);
        CustomQueryConfig cqc = getCustomQueryConfig.getStoredQuery("queryName1");

        assertEquals(Lists.newArrayList("prop3", "prop4"), cqc.getQueryOptionalProperties());
        assertEquals(Lists.newArrayList("prop1", "prop2"), cqc.getQueryRequiredProperties());
        assertEquals("out('blah').has('something','foo')", cqc.getQuery());
    }

    @Test
    public void testGetStoredQueryNameWithOptProps() {
        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(configJson);
        CustomQueryConfig cqc = getCustomQueryConfig.getStoredQuery("queryName2");

        assertEquals(Lists.newArrayList("prop5"), cqc.getQueryOptionalProperties());
        assertEquals(new ArrayList<String>(), cqc.getQueryRequiredProperties());
        assertEquals("out('bar').has('stuff','baz')", cqc.getQuery());
    }

    @Test
    public void testGetStoredQueryNameWithNoProps() {
        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(configJson);
        CustomQueryConfig cqc = getCustomQueryConfig.getStoredQuery("queryName3");

        assertEquals(new ArrayList<String>(), cqc.getQueryOptionalProperties());
        assertEquals(new ArrayList<String>(), cqc.getQueryRequiredProperties());
        assertEquals("out('bar1').has('stuff','baz1')", cqc.getQuery());
    }

    @Test
    public void testGetPropertyString() throws Exception {
        // Accessing the private method using reflection
        Method method = GetCustomQueryConfig.class.getDeclaredMethod("getPropertyString", JsonObject.class, String.class);
        method.setAccessible(true); // Make the private method accessible

        // Prepare test data for the JSON
        JsonObject configObject = new JsonObject();
        JsonElement queryName1 = new JsonPrimitive("out('blah').has('something','foo')");
        configObject.add("queryName1", queryName1);

        // Test case 1: The config is present and valid
        String result = (String) method.invoke(getCustomQueryConfig, configObject, "queryName1");
        assertEquals("out('blah').has('something','foo')", result);

        // Test case 2: The config is not present in the JSON
        result = (String) method.invoke(getCustomQueryConfig, configObject, "nonExistingConfig");
        assertNull(result);

        // Test case 3: The config is null
        configObject.add("queryName2", JsonNull.INSTANCE);
        result = (String) method.invoke(getCustomQueryConfig, configObject, "queryName2");
        assertNull(result);
    }

    @Test
    public void testQueryWithNullProperties() {
        String queryWithNullPropsJson = """
            {
            "stored-queries": [{
            	"queryName1": {
            		"query": {
            			"required-properties": null,
            			"optional-properties": null
            		},
            		"stored-query": "out('blah').has('something','foo')"
            	}
            }]
            }\
            """;

        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(queryWithNullPropsJson);
        CustomQueryConfig cqc = getCustomQueryConfig.getStoredQuery("queryName1");

        // Assert that null values for required/optional properties are converted to empty lists
        assertEquals(new ArrayList<String>(), cqc.getQueryRequiredProperties());
        assertEquals(new ArrayList<String>(), cqc.getQueryOptionalProperties());
    }

    @Test
    public void testQueryWithMissingProperties() {
        String queryWithMissingPropsJson = """
            {
            "stored-queries": [{
            	"queryName1": {
            		"query": {
            			"required-properties": []
            		},
            		"stored-query": "out('blah').has('something','foo')"
            	}
            }]
            }\
            """;

        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(queryWithMissingPropsJson);
        CustomQueryConfig cqc = getCustomQueryConfig.getStoredQuery("queryName1");

        // Since "optional-properties" is missing, it should default to an empty list
        assertEquals(new ArrayList<String>(), cqc.getQueryOptionalProperties());
        assertEquals(new ArrayList<String>(), cqc.getQueryRequiredProperties());
    }

    @Test
    public void testQueryWithNullRequiredProperties() {
        String queryWithNullPropsJson = """
            {
            "stored-queries": [{
            	"queryName1": {
            		"query": {
            			"required-properties": null,
            			"optional-properties": ["prop3"]
            		},
            		"stored-query": "out('blah').has('something','foo')"
            	}
            }]
            }\
            """;

        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(queryWithNullPropsJson);
        CustomQueryConfig cqc = getCustomQueryConfig.getStoredQuery("queryName1");

        // Assert that null values for required-properties convert to an empty list
        assertEquals(new ArrayList<String>(), cqc.getQueryRequiredProperties());
        assertEquals(Lists.newArrayList("prop3"), cqc.getQueryOptionalProperties());
    }

    @Test
    public void testQueryWithNullOptionalProperties() {
        String queryWithNullPropsJson = """
            {
            "stored-queries": [{
            	"queryName1": {
            		"query": {
            			"required-properties": ["prop1"],
            			"optional-properties": null  // Null for optional properties
            		},
            		"stored-query": "out('blah').has('something','foo')"
            	}
            }]
            }\
            """;

        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(queryWithNullPropsJson);
        CustomQueryConfig cqc = getCustomQueryConfig.getStoredQuery("queryName1");

        // Assert that null values for optional-properties convert to an empty list
        assertEquals(Lists.newArrayList("prop1"), cqc.getQueryRequiredProperties());
        assertEquals(new ArrayList<String>(), cqc.getQueryOptionalProperties());
    }
}
