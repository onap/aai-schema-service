/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright © 2025 Deutsche Telekom.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.schemagen.genxsd;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NodeGetOperationTest {
    private String xmlRootElementName;
    private String result;

    public static Collection<String[]> testConditions() {
        String inputs[][] = {
            {"NetworkGenericVnfsGenericVnf", "generic-vnf", "Network", "/network/generic-vnfs/generic-vnf/{vnf-id}",
                "        - name: vnf-id\n          in: path\n          description: Unique id of VNF.  This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__",
                "  /nodes/generic-vnfs/generic-vnf/{vnf-id}:\n    get:\n      tags:\n        - Operations\n      summary: returns generic-vnf\n      description: returns generic-vnf\n      operationId: getNetworkGenericVnfsGenericVnf\n      produces:\n        - application/json\n        - application/xml\n      responses:\n        \"200\":\n          description: successful operation\n          schema:\n              $ref: \"#/definitions/generic-vnf\"\n        \"default\":\n          null\n      parameters:\n        - name: vnf-id\n          in: path\n          description: Unique id of VNF.  This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__"},
            {"GenericVnf", "generic-vnf", "", "/Network/generic-vnf/{vnf-id}",
                "        - name: vnf-id\n          in: path\n          description: Unique id of VNF.  This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__",
                ""},
            {"CloudInfrastructurePserversPserverPInterfaces", "p-interfaces", "CloudInfrastructure",
                "/cloud-infrastructure/pservers/pserver/{hostname}/p-interfaces",
                "        - name: hostname\n          in: path\n          description: Value from executing hostname on the compute node.\n          required: true\n          type: string\n          example: __HOSTNAME__",
                "  /nodes/p-interfaces?parameter=value[&parameter2=value2]:\n    get:\n      tags:\n        - Operations\n      summary: returns p-interfaces\n      description: returns p-interfaces\n      operationId: getCloudInfrastructurePserversPserverPInterfaces\n      produces:\n        - application/json\n        - application/xml\n      responses:\n        \"200\":\n          description: successful operation\n          schema:\n              $ref: \"#/definitions/p-interfaces\"\n        \"default\":\n          null\n      parameters:\n        - name: hostname\n          in: path\n          description: Value from executing hostname on the compute node.\n          required: true\n          type: string\n          example: __HOSTNAME__\n        - name: interface-name\n          in: query\n          description:\n          required: false\n          type: string        - name: prov-status\n          in: query\n          description:\n          required: false\n          type: string"}
        };
        return Arrays.asList(inputs);
    }

    public void initNodeGetOperationTest(String useOpId, String xmlRootElementName, String tag, String path,
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
        NodeGetOperation.addContainerProps(container, containerProps);
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenCrudPathEndsWithRelationship(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        String modifiedPath = path + "/relationship";
        assertModResult(useOpId, xmlRootElementName, tag, modifiedPath, pathParams);
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenCrudPathContainsRelationship(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        String modifiedPath = path + "/relationship/some-id";
        assertModResult(useOpId, xmlRootElementName, tag, modifiedPath, pathParams);
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenCrudPathEndsWithRelationshipList(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        String modifiedPath = path + "/relationship-list";
        assertModResult(useOpId, xmlRootElementName, tag, modifiedPath, pathParams);
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenCrudPathStartsWithSearch(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        String modifiedPath = "/search" + path;
        assertModResult(useOpId, xmlRootElementName, tag, modifiedPath, pathParams);
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenCrudPathStartsWithActions(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        String modifiedPath = "/actions" + path;
        assertModResult(useOpId, xmlRootElementName, tag, modifiedPath, pathParams);
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenCrudPathStartsWithNodes(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        String modifiedPath = "/nodes" + path;
        assertModResult(useOpId, xmlRootElementName, tag, modifiedPath, pathParams);
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenChecklistContainsXmlRootElementName(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        NodeGetOperation.checklist.add(xmlRootElementName);
        assertModResult(useOpId, xmlRootElementName, tag, path, pathParams);
    }

    private void assertModResult(String useOpId, String xmlRootElementName, String tag, String path, String pathParams) {
    NodeGetOperation get = new NodeGetOperation(useOpId, xmlRootElementName, tag, path, pathParams);
    String modResult = get.toString();
    assertThat(modResult, is(""));
}

    // Test case for adding container properties
    @MethodSource("testConditions")
    @ParameterizedTest
    public void testAddContainerProps(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        initNodeGetOperationTest(useOpId, xmlRootElementName, tag, path, pathParams, result);
        String container = this.xmlRootElementName;
        String prop = "        - name: " + container
            + "\n          in: query\n          description:\n          required: false\n          type: string";
        
        Vector<String> queryProps = new Vector<>();
        queryProps.add(prop);

        NodeGetOperation.addContainerProps(container, queryProps);
        assertThat(NodeGetOperation.containers.get(container).get(0), is(prop));
    }

}
