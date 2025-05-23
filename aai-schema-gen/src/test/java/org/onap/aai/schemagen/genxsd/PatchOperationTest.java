/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.onap.aai.setup.SchemaVersion;

public class PatchOperationTest {
    private static SchemaVersion v = new SchemaVersion("v16");

    public static Collection<String[]> testConditions() {
        String inputs[][] = {{"NetworkGenericVnfsGenericVnf", "generic-vnf", "Network",
            "/network/generic-vnfs/generic-vnf/{vnf-id}",
            "        - name: vnf-id\n          in: path\n          description: Unique id of VNF.  This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__",
            "    patch:\n      tags:\n        - Network\n      summary: update an existing generic-vnf\n      description: |\n        Update an existing generic-vnf\n        #\n        Note:  Endpoints that are not devoted to object relationships support both PUT and PATCH operations.\n        The PUT operation will entirely replace an existing object.\n        The PATCH operation sends a \"description of changes\" for an existing object.  The entire set of changes must be applied.  An error result means no change occurs.\n        #\n        Other differences between PUT and PATCH are:\n        #\n        - For PATCH, you can send any of the values shown in sample REQUEST body.  There are no required values.\n        - For PATCH, resource-id which is a required REQUEST body element for PUT, must not be sent.\n        - PATCH cannot be used to update relationship elements; there are dedicated PUT operations for this.\n      operationId: UpdateNetworkGenericVnfsGenericVnf\n      consumes:\n        - application/json\n      produces:\n        - application/json\n      responses:\n        \"default\":\n          null      parameters:\n        - name: vnf-id\n          in: path\n          description: Unique id of VNF.  This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__        - name: body\n          in: body\n          description: generic-vnf object that needs to be updated.[See Examples](apidocs/aai/relations/v16/NetworkGenericVnfsGenericVnf.json)\n          required: true\n          schema:\n            $ref: \"#/definitions/generic-vnf\"\n"},
            // if ( StringUtils.isEmpty(tag) )
            {"GenericVnf", "generic-vnf", "", "/generic-vnf/{vnf-id}",
                "        - name: vnf-id\n          in: path\n          description: Unique id of VNF.  This is unique across the graph.\n          required: true\n          type: string\n          example: __VNF-ID__",
                ""},
            // Test: if ( !path.endsWith("/relationship") && !path.endsWith("}") )
            {"CloudInfrastructurePserversPserverPInterfaces", "p-interfaces", "CloudInfrastructure",
                "/cloud-infrastructure/pservers/pserver/{hostname}/p-interfaces",
                "        - name: hostname\n          in: path\n          description: Value from executing hostname on the compute node.\n          required: true\n          type: string\n          example: __HOSTNAME__",
                ""},
            // {"","ctag-pool","","","",""},
            // {"","pserver","","","",""},
            // {"","oam-network","","","",""},
            // {"","dvs-switch","","","",""},
            // {"","availability-zone","","","",""}
        };
        return Arrays.asList(inputs);
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString(String useOpId, String xmlRootElementName, String tag, String path, String pathParams, String expectedResult) {
        PatchOperation patch =
            new PatchOperation(useOpId, xmlRootElementName, tag, path, pathParams, v, "/aai");
        String modResult = patch.toString();
        assertThat(modResult, is(expectedResult));
    }

}
