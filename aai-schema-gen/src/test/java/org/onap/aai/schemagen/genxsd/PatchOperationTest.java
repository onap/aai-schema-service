/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright © 2026 Deutsche Telekom.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.onap.aai.setup.SchemaVersion;

public class PatchOperationTest {

    private static final SchemaVersion V = new SchemaVersion("v16");
    private static final String VNF_PATH = "/network/generic-vnfs/generic-vnf/{vnf-id}";

    public static Stream<Arguments> endpointsWithoutAPatch() {
        return Stream.of(
            arguments("an untagged endpoint", "generic-vnf", "", "/generic-vnf/{vnf-id}"),
            arguments("a container", "p-interfaces", "CloudInfrastructure",
                "/cloud-infrastructure/pservers/pserver/{hostname}/p-interfaces"),
            // relationships are replaced whole, so unlike the PUT there is no PATCH on one
            arguments("a relationship", "relationship", "Network",
                VNF_PATH + "/relationship-list/relationship"),
            arguments("a relationship child", "relationship", "Network",
                "/network/relationship/xyz"),
            arguments("a relationship list", "relationship", "Network",
                "/network/relationship-list"),
            arguments("a search endpoint", "search", "SearchTag", "/search/query/{query-id}"));
    }

    @ParameterizedTest(name = "{0} has no patch")
    @MethodSource("endpointsWithoutAPatch")
    public void buildsNothingFor(String endpoint, String xmlRootElementName, String tag,
        String path) {
        PatchOperation patch = new PatchOperation("TestOpId", xmlRootElementName, tag, path,
            List.of(vnfId()), V, "/aai");
        assertNull(patch.build());
    }

    @Test
    public void buildsThePatchOfAnObject() {
        PatchOperation patch = new PatchOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), V, "/aai");

        Operation patchOperation = patch.build();

        assertNotNull(patchOperation);
        assertEquals(List.of("Network"), patchOperation.getTags());
        assertEquals("update an existing generic-vnf", patchOperation.getSummary());
        assertEquals("UpdateNetworkGenericVnfsGenericVnf", patchOperation.getOperationId());
        // a patch document has no XML form here
        assertEquals(OperationDefaults.JSON_ONLY, patchOperation.getConsumes());
        assertEquals(OperationDefaults.JSON_ONLY, patchOperation.getProduces());
        assertEquals(List.of("default"), List.copyOf(patchOperation.getResponses().keySet()));
    }

    @Test
    public void describesHowAPatchDiffersFromAPut() {
        PatchOperation patch = new PatchOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), V, "/aai");

        String description = patch.build().getDescription();

        assertEquals(List.of("Update an existing generic-vnf", "#",
            "Note:  Endpoints that are not devoted to object relationships support both PUT and PATCH operations.",
            "The PUT operation will entirely replace an existing object.",
            "The PATCH operation sends a \"description of changes\" for an existing object.  The entire set of changes must be applied.  An error result means no change occurs.",
            "#", "Other differences between PUT and PATCH are:", "#",
            "- For PATCH, you can send any of the values shown in sample REQUEST body.  There are no required values.",
            "- For PATCH, resource-id which is a required REQUEST body element for PUT, must not be sent.",
            "- PATCH cannot be used to update relationship elements; there are dedicated PUT operations for this."),
            description.lines().toList());
        // the trailing newline is what makes this render as a literal block, not as one line
        assertTrue(description.endsWith("\n"));
    }

    @Test
    public void takesAPartialObjectAsItsBody() {
        PatchOperation patch = new PatchOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), V, "/aai");

        List<Parameter> parameters = patch.build().getParameters();

        assertEquals(List.of("vnf-id", "body"),
            parameters.stream().map(Parameter::getName).toList());
        BodyParameter body = assertInstanceOf(BodyParameter.class, parameters.get(1));
        assertTrue(body.getRequired());
        assertEquals(
            "generic-vnf object that needs to be updated."
                + "[See Examples](apidocs/aai/relations/v16/NetworkGenericVnfsGenericVnf.json)",
            body.getDescription());
    }

    @Test
    public void theBodyRefersToThePatchDefinitionOfTheObject() {
        // a partial object has a definition of its own, which the generator names with a prefix
        PatchOperation patch = new PatchOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), V, "/aai");
        patch.setPrefixForPatchRef("zzzz-patch-");

        BodyParameter body =
            assertInstanceOf(BodyParameter.class, patch.build().getParameters().get(1));

        assertEquals("#/definitions/zzzz-patch-generic-vnf",
            assertInstanceOf(RefModel.class, body.getSchema()).get$ref());
    }

    private static PathParameter vnfId() {
        PathParameter vnfId = new PathParameter();
        vnfId.setName("vnf-id");
        vnfId.setDescription("Unique id of VNF. This is unique across the graph.");
        vnfId.setRequired(true);
        vnfId.setType("string");
        return vnfId;
    }
}
