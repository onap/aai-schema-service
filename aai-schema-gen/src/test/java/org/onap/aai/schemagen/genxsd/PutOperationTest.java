/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.schemagen.genxsd;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.onap.aai.setup.SchemaVersion;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PutOperationTest {
    private String useOpId;
    private String xmlRootElementName;
    private String tag;
    private String path;
    private String pathParams;
    private String result;
    private static SchemaVersion v = new SchemaVersion("v14");

    public static Collection<String[]> testConditions() {
        String inputs[][] = {
            // Normal case: creates or updates a generic-vnf
            {"NetworkGenericVnfsGenericVnf", "generic-vnf", "Network",
                "/network/generic-vnfs/generic-vnf/{vnf-id}",
                "        - name: vnf-id\n          in: path\n          description: Unique id of VNF.  This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__",
                "    put:\n      tags:\n        - Network\n      summary: create or update an existing generic-vnf\n      description: |\n        Create or update an existing generic-vnf.\n        #\n        Note! This PUT method has a corresponding PATCH method that can be used to update just a few of the fields of an existing object, rather than a full object replacement.  An example can be found in the [PATCH section] below\n      operationId: createOrUpdateNetworkGenericVnfsGenericVnf\n      consumes:\n        - application/json\n        - application/xml\n      produces:\n        - application/json\n        - application/xml\n      responses:\n        \"default\":\n          null      parameters:\n        - name: vnf-id\n          in: path\n          description: Unique id of VNF.  This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__        - name: body\n          in: body\n          description: generic-vnf object that needs to be created or updated. [Valid relationship examples shown here](apidocs/aai/relations/"
                    + v.toString()
                    + "/NetworkGenericVnfsGenericVnf.json)\n          required: true\n          schema:\n            $ref: \"#/definitions/generic-vnf\"\n"},

            // Case where path contains "/relationship/": this should return empty
            {"RelationshipListExample", "relationship", "ExampleTag",
                "/example/relationship/related-resource",
                "        - name: related-resource\n          in: path\n          description: Related resource.\n          required: true\n          type: string\n          example: __RESOURCE-ID__",
                ""},

            // Case where path ends with "/relationship-list": this should return empty
            {"RelationshipListExample", "relationship", "ExampleTag",
                "/example/relationship-list",
                "        - name: related-resource\n          in: path\n          description: Related resource.\n          required: true\n          type: string\n          example: __RESOURCE-ID__",
                ""},

            // Case where path neither ends with "/relationship" nor "}" - should return empty
            // ExamplePathWithoutRelationship - Expecting full operation details for this path
            {"ExamplePathWithoutRelationship", "example-resource", "ExampleTag", "/example-path/{resource-id}",
                "        - name: resource-id\n          in: path\n          description: Resource ID.\n          required: true\n          type: string\n          example: __RESOURCE-ID__",
                "    put:\n      tags:\n        - ExampleTag\n      summary: create or update an existing example-resource\n      description: |\n        Create or update an existing example-resource.\n        #\n        Note! This PUT method has a corresponding PATCH method that can be used to update just a few of the fields of an existing object, rather than a full object replacement. An example can be found in the [PATCH section] below\n      operationId: createOrUpdateExamplePathWithoutRelationship\n      consumes:\n        - application/json\n        - application/xml\n      produces:\n        - application/json\n        - application/xml\n      responses:\n        \"default\":\n          null\n      parameters:\n        - name: resource-id\n          in: path\n          description: Resource ID.\n          required: true\n          type: string\n          example: __RESOURCE-ID__\n        - name: body\n          in: body\n          description: example-resource object that needs to be created or updated. [Valid relationship examples shown here](apidocs/aai/relations/v14/ExamplePathWithoutRelationship.json)\n          required: true\n          schema:\n            $ref: \"#/definitions/example-resource\"\n"
                  },

            // Case where path starts with "/search": this should return empty
            {"SearchExample", "search", "SearchTag",
                "/search/query",
                "        - name: query\n          in: path\n          description: Search query.\n          required: true\n          type: string\n          example: __QUERY__",
                ""},

            // Additional normal case for coverage
            {"GenericVnf", "generic-vnf", "", "/generic-vnf/{vnf-id}",
                "        - name: vnf-id\n          in: path\n          description: Unique id of VNF.  This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__",
                ""},
            // Test case for path ending with '/relationship'
            {"RelationshipTest", "relationship", "", "/path/to/relationship",
                "", ""},

            // Test case for path starting with '/search'
            {"SearchTest", "search", "", "/search/path/to/resource",
                "", ""},

            // Test case for path containing "/relationship/"
            {"TestOp2", "relationship", "TestTag", "/network/relationship/123", "", ""},

            // Test case for path ending with "/relationship-list"
            {"TestOp3", "relationship-list", "TestTag", "/network/relationship-list", "", ""},

            // Test case for path starting with "/search"
            {"TestOp4", "search", "TestTag", "/search/records", "", ""}
        };
        return Arrays.asList(inputs);
    }

    public void initPutOperationTest(String useOpId, String xmlRootElementName, String tag, String path,
        String pathParams, String result) {
        this.useOpId = useOpId;
        this.xmlRootElementName = xmlRootElementName;
        this.tag = tag;
        this.path = path;
        this.pathParams = pathParams;
        this.result = result;
    }

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {

    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        initPutOperationTest(useOpId, xmlRootElementName, tag, path, pathParams, result);
        PutOperation put =
            new PutOperation(useOpId, xmlRootElementName, tag, path, pathParams, v, "/aai");
        String modResult = put.toString();

        // Trim leading/trailing spaces and normalize internal whitespace (i.e., remove multiple spaces)
        String normalizedExpected = result.trim().replaceAll("\\s+", " ");
        String normalizedActual = modResult.trim().replaceAll("\\s+", " ");

        assertThat(normalizedActual, is(normalizedExpected));
    }

    // Test case for path starting with "/search"
    @Test
    public void testToStringForSearchPath() {
        PutOperation put = new PutOperation("useOpId", "xmlRootElementName", "tag", "/search/query", "pathParams", v, "/aai");
        String result = put.toString();
        assertThat(result, is("")); // Should return empty string for path starting with "/search"
    }

}
