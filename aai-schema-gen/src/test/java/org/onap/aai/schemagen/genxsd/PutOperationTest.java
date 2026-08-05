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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.onap.aai.setup.SchemaVersion;

public class PutOperationTest {

    private static final SchemaVersion V = new SchemaVersion("v14");
    private static final String VNF_PATH = "/network/generic-vnfs/generic-vnf/{vnf-id}";
    private static final String RELATIONSHIP_PATH = VNF_PATH + "/relationship-list/relationship";

    /** A fresh context per test; no shared state to clear. */
    private GenerationContext context;

    @BeforeEach
    public void newContext() {
        context = new GenerationContext();
    }

    public static Stream<Arguments> endpointsWithoutAPut() {
        return Stream.of(
            arguments("an untagged endpoint", "generic-vnf", "", "/generic-vnf/{vnf-id}"),
            arguments("a relationship child", "relationship", "ExampleTag",
                "/example/relationship/related-resource"),
            arguments("a relationship list", "relationship", "ExampleTag",
                "/example/relationship-list"),
            arguments("a search endpoint", "search", "SearchTag", "/search/query"),
            arguments("a container", "p-interfaces", "CloudInfrastructure",
                "/cloud-infrastructure/pservers/pserver/{hostname}/p-interfaces"));
    }

    @ParameterizedTest(name = "{0} has no put")
    @MethodSource("endpointsWithoutAPut")
    public void buildsNothingFor(String endpoint, String xmlRootElementName, String tag,
        String path) {
        PutOperation put = new PutOperation("TestOpId", xmlRootElementName, tag, path,
            List.of(vnfId()), V, "/aai");
        assertNull(put.build());
    }

    @Test
    public void buildsThePutOfAnObject() {
        PutOperation put = new PutOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), V, "/aai");

        Operation putOperation = put.build();

        assertNotNull(putOperation);
        assertEquals(List.of("Network"), putOperation.getTags());
        assertEquals("create or update an existing generic-vnf", putOperation.getSummary());
        assertEquals("createOrUpdateNetworkGenericVnfsGenericVnf", putOperation.getOperationId());
        assertEquals(OperationDefaults.JSON_AND_XML, putOperation.getConsumes());
        assertEquals(OperationDefaults.JSON_AND_XML, putOperation.getProduces());
        assertEquals(List.of("default"), List.copyOf(putOperation.getResponses().keySet()));
    }

    @Test
    public void describesThePutAsAWholeObjectReplacement() {
        PutOperation put = new PutOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), V, "/aai");

        String description = put.build().getDescription();

        assertEquals(List.of("Create or update an existing generic-vnf.", "#",
            "Note! This PUT method has a corresponding PATCH method that can be used to update just a few of the fields of an existing object, rather than a full object replacement.  An example can be found in the [PATCH section] below"),
            description.lines().toList());
        // the trailing newline is what makes this render as a literal block, not as one line
        assertTrue(description.endsWith("\n"));
    }

    @Test
    public void takesTheObjectAsItsBody() {
        PutOperation put = new PutOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), V, "/aai");

        List<Parameter> parameters = put.build().getParameters();

        assertEquals(List.of("vnf-id", "body"),
            parameters.stream().map(Parameter::getName).toList());
        BodyParameter body = assertInstanceOf(BodyParameter.class, parameters.get(1));
        assertTrue(body.getRequired());
        assertEquals(
            "generic-vnf object that needs to be created or updated."
                + " [Valid relationship examples shown here]"
                + "(apidocs/aai/relations/v14/NetworkGenericVnfsGenericVnf.json)",
            body.getDescription());
        assertEquals("#/definitions/generic-vnf",
            assertInstanceOf(RefModel.class, body.getSchema()).get$ref());
    }

    @Test
    public void aRelationshipPutRefersToTheNodeDefinition() {
        PutOperation put =
            new PutOperation("NetworkGenericVnfsGenericVnfRelationshipListRelationship",
                "relationship", "Network", RELATIONSHIP_PATH, List.of(vnfId()), V, "/aai");

        Operation putOperation = put.build();

        assertNotNull(putOperation);
        assertEquals("see node definition for valid relationships", putOperation.getSummary());
        // the valid relationships are the description; there is nothing else to say here
        assertNull(putOperation.getDescription());
    }

    @Test
    public void aRelationshipBodyIsOneEntryOfTheDictionary() {
        PutOperation put =
            new PutOperation("NetworkGenericVnfsGenericVnfRelationshipListRelationship",
                "relationship", "Network", RELATIONSHIP_PATH, List.of(vnfId()), V, "/aai");

        BodyParameter body =
            assertInstanceOf(BodyParameter.class, put.build().getParameters().get(1));

        assertEquals("#/definitions/relationship-dict",
            assertInstanceOf(RefModel.class, body.getSchema()).get$ref());
        // the examples are per node type, so the operation id's relationship suffix is dropped
        assertTrue(
            body.getDescription()
                .endsWith("(apidocs/aai/relations/v14/NetworkGenericVnfsGenericVnf.json)"),
            body.getDescription());
    }

    @Test
    public void registerAddsRelationshipPath() {
        PutOperation put = new PutOperation("NetworkGenericVnfsGenericVnf", "relationship",
            "Network", RELATIONSHIP_PATH, List.of(), V, "/aai");
        put.register(context);
        assertEquals(RELATIONSHIP_PATH,
            context.getPutRelationPaths().get("NetworkGenericVnfsGenericVnf"));
    }

    @Test
    public void registerIgnoresNonRelationshipPath() {
        PutOperation put = new PutOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(), V, "/aai");
        put.register(context);
        assertTrue(context.getPutRelationPaths().isEmpty());
    }

    @Test
    public void buildDoesNotRegister() {
        // build() must be free of side effects; registration only happens via register()
        PutOperation put = new PutOperation("NetworkGenericVnfsGenericVnf", "relationship",
            "Network", RELATIONSHIP_PATH, List.of(), V, "/aai");
        put.build();
        assertTrue(context.getPutRelationPaths().isEmpty());
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
