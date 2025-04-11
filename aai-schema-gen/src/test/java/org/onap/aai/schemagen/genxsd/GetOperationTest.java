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
import java.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetOperationTest {
    private static final Logger logger = LoggerFactory.getLogger(GetOperationTest.class);
    private String xmlRootElementName;
    private String result;

    public static Collection<String[]> testConditions() {
        String inputs[][] = {
            // Existing test cases
            {"NetworkGenericVnfsGenericVnf", "generic-vnf", "Network",
                "/network/generic-vnfs/generic-vnf/{vnf-id}",
                "        - name: vnf-id\n          in: path\n          description: Unique id of VNF.  This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__",
                "  /network/generic-vnfs/generic-vnf/{vnf-id}:\n    get:\n      tags:\n        - Network\n      summary: returns generic-vnf\n      description: returns generic-vnf\n      operationId: getNetworkGenericVnfsGenericVnf\n      produces:\n        - application/json\n        - application/xml\n      responses:\n        \"200\":\n          description: successful operation\n          schema:\n              $ref: \"#/definitions/generic-vnf\"\n        \"default\":\n          null      parameters:\n        - name: vnf-id\n          in: path\n          description: Unique id of VNF.  This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__"},
            {"GenericVnf", "generic-vnf", "", "/generic-vnf/{vnf-id}",
                "        - name: vnf-id\n          in: path\n          description: Unique id of VNF.  This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__",
                ""},

            // Add new test cases for path filtering conditions
            // Test case for path ending with "/relationship"
            {"TestOp1", "relationship", "TestTag", "/network/relationship", "", ""},

            // Test case for path containing "/relationship/"
            {"TestOp2", "relationship", "TestTag", "/network/relationship/123", "", ""},

            // Test case for path ending with "/relationship-list"
            {"TestOp3", "relationship-list", "TestTag", "/network/relationship-list", "", ""},

            // Test case for path starting with "/search"
            {"TestOp4", "search", "TestTag", "/search/records", "", ""}
        };
        return Arrays.asList(inputs);
    }

    public void initGetOperationTest(String useOpId, String xmlRootElementName, String tag, String path,
        String pathParams, String result) {
        this.xmlRootElementName = xmlRootElementName;
        this.result = result;
    }

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        String container = "p-interfaces";
        String queryProps[] = {
            "        - name: interface-name\n          in: query\n          description:\n          required: false\n          type: string",
            "        - name: prov-status\n          in: query\n          description:\n          required: false\n          type: string"};
        Vector<String> containerProps = new Vector<String>();
        for (String prop : queryProps) {
            containerProps.add(prop);
        }
        GetOperation.addContainerProps(container, containerProps);
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testAddContainerProps(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        initGetOperationTest(useOpId, xmlRootElementName, tag, path, pathParams, result);
        String container = this.xmlRootElementName;
        String prop = "        - name: " + container
            + "\n          in: query\n          description:\n          required: false\n          type: string";
        Vector<String> queryProps = new Vector<String>();
        queryProps.add(prop);
        for (String p : queryProps) {
            logger.debug("qProp=" + p);
        }
        logger.debug("Done=" + this.xmlRootElementName);
        GetOperation.addContainerProps(container, queryProps);
        assertThat(GetOperation.containers.get(container).get(0), is(prop));
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        initGetOperationTest(useOpId, xmlRootElementName, tag, path, pathParams, result);
        GetOperation get = new GetOperation(useOpId, xmlRootElementName, tag, path, pathParams);
        String modResult = get.toString();
        assertThat(modResult, is(this.result));
    }

}
