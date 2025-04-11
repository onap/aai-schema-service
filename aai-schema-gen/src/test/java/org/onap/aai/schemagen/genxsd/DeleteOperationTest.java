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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DeleteOperationTest {
    private String useOpId;
    private String xmlRootElementName;
    private String tag;
    private String path;
    private String pathParams;
    private String result;

    public static Collection<String[]> testConditions() {
        String inputs[][] = {
            // Case where tag is empty (tests StringUtils.isEmpty(tag))
            {"TestEmptyTag", "generic-vnf", "", "/network/generic-vnfs/generic-vnf/{vnf-id}",
                "        - name: vnf-id\n          in: path\n          description: Unique id of VNF. This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__\n",
                ""}, // Should return an empty string because tag is empty

            // Case where path contains "/relationship/" (tests path.contains("/relationship/"))
            {"TestPathContainsRelationship", "generic-vnf", "Network", "/network/relationship/xyz/{xyz-id}",
                "        - name: xyz-id\n          in: path\n          description: Unique id of XYZ. This is a test.\n          required: true\n          type: string\n          example: __XYZ-ID__\n",
                ""}, // Should return an empty string because path contains "/relationship/"

            // Case where path ends with "/relationship-list" (tests path.endsWith("/relationship-list"))
            {"TestPathEndsWithRelationshipList", "service", "Network", "/network/relationship-list",
                "        - name: xyz-id\n          in: path\n          description: Unique id of XYZ. This is a test.\n          required: true\n          type: string\n          example: __XYZ-ID__\n",
                ""}, // Should return an empty string because path ends with "/relationship-list"

            // Case when path ends with /relationship (tests path.endsWith("/relationship"))
            {"TestPathEndsWithRelationship", "relationship", "Service", "/service/xyz/relationship",
                "        - name: xyz-id\n          in: path\n          description: Unique id of XYZ.\n          required: true\n          type: string\n          example: __XYZ-ID__\n",
                "    delete:\n      tags:\n        - Service\n      summary: delete an existing relationship\n      description: delete an existing relationship\n      operationId: deleteTestPathEndsWithRelationship\n      consumes:\n        - application/json\n        - application/xml\n      produces:\n        - application/json\n        - application/xml\n      responses:\n        \"default\":\n          null      parameters:\n        - name: xyz-id\n          in: path\n          description: Unique id of XYZ.\n          required: true\n          type: string\n          example: __XYZ-ID__\n"},

            // Case where path starts with "/search" (tests path.startsWith("/search"))
            {"TestPathStartsWithSearch", "generic-vnf", "Network", "/search/vnf/{vnf-id}",
                "        - name: vnf-id\n          in: path\n          description: Unique id of VNF. This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__\n",
                ""}, // Should return an empty string because path starts with "/search"

            // Additional normal case to verify overall behavior
            {"TestValidPath", "generic-vnf", "Network", "/network/generic-vnfs/generic-vnf/{vnf-id}",
                "        - name: vnf-id\n          in: path\n          description: Unique id of VNF. This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__\n",
                "    delete:\n      tags:\n        - Network\n      summary: delete an existing generic-vnf\n      description: delete an existing generic-vnf\n      operationId: deleteTestValidPath\n      consumes:\n        - application/json\n        - application/xml\n      produces:\n        - application/json\n        - application/xml\n      responses:\n        \"default\":\n          null      parameters:\n        - name: vnf-id\n          in: path\n          description: Unique id of VNF. This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__\n        - name: resource-version\n          in: query\n          description: resource-version for concurrency\n          required: true\n          type: string\n"}
        };
        return Arrays.asList(inputs);
    }

    public void initDeleteOperationTest(String useOpId, String xmlRootElementName, String tag, String path,
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
        initDeleteOperationTest(useOpId, xmlRootElementName, tag, path, pathParams, result);
        DeleteOperation delete =
            new DeleteOperation(useOpId, xmlRootElementName, tag, path, pathParams);
        String modResult = delete.toString();
        assertThat(modResult, is(this.result));
    }

}
