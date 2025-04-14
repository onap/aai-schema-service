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

    // Test when CRUDpath ends with "/relationship"
    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenCrudPathEndsWithRelationship(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        String modifiedPath = path + "/relationship";
        NodeGetOperation get = new NodeGetOperation(useOpId, xmlRootElementName, tag, modifiedPath, pathParams);
        String modResult = get.toString();
        assertThat(modResult, is(""));
    }

    // Test when CRUDpath contains "/relationship/"
    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenCrudPathContainsRelationship(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        String modifiedPath = path + "/relationship/some-id";
        NodeGetOperation get = new NodeGetOperation(useOpId, xmlRootElementName, tag, modifiedPath, pathParams);
        String modResult = get.toString();
        assertThat(modResult, is(""));
    }

    // Test when CRUDpath ends with "/relationship-list"
    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenCrudPathEndsWithRelationshipList(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        String modifiedPath = path + "/relationship-list";
        NodeGetOperation get = new NodeGetOperation(useOpId, xmlRootElementName, tag, modifiedPath, pathParams);
        String modResult = get.toString();
        assertThat(modResult, is(""));
    }

    // Test when CRUDpath starts with "/search"
    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenCrudPathStartsWithSearch(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        String modifiedPath = "/search" + path;
        NodeGetOperation get = new NodeGetOperation(useOpId, xmlRootElementName, tag, modifiedPath, pathParams);
        String modResult = get.toString();
        assertThat(modResult, is(""));
    }

    // Test when CRUDpath starts with "/actions"
    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenCrudPathStartsWithActions(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        String modifiedPath = "/actions" + path;
        NodeGetOperation get = new NodeGetOperation(useOpId, xmlRootElementName, tag, modifiedPath, pathParams);
        String modResult = get.toString();
        assertThat(modResult, is(""));
    }

    // Test when CRUDpath starts with "/nodes"
    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenCrudPathStartsWithNodes(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        String modifiedPath = "/nodes" + path;
        NodeGetOperation get = new NodeGetOperation(useOpId, xmlRootElementName, tag, modifiedPath, pathParams);
        String modResult = get.toString();
        assertThat(modResult, is(""));
    }

    // Test when checklist contains xmlRootElementName
    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString_whenChecklistContainsXmlRootElementName(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String result) {
        NodeGetOperation.checklist.add(xmlRootElementName);
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
        Vector<String> queryProps = new Vector<String>();
        queryProps.add(prop);
        String props = null;
        for (String p : queryProps) {
            props += "qProp=" + p + "\n";
            // logger.debug("qProp="+p);
        }
        // logger.debug("Done="+this.xmlRootElementName);
        NodeGetOperation.addContainerProps(container, queryProps);
        assertThat(NodeGetOperation.containers.get(container).get(0), is(prop));
    }

}
