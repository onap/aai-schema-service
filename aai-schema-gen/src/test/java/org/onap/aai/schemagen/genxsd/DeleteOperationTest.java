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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DeleteOperationTest {

    private static final String VNF_PATH = "/network/generic-vnfs/generic-vnf/{vnf-id}";

    /** A fresh context per test; no shared state to clear. */
    private GenerationContext context;

    @BeforeEach
    public void newContext() {
        context = new GenerationContext();
    }

    public static Stream<Arguments> endpointsWithoutADelete() {
        return Stream.of(arguments("an untagged endpoint", "generic-vnf", "", VNF_PATH),
            arguments("a relationship child", "generic-vnf", "Network",
                "/network/relationship/xyz/{xyz-id}"),
            arguments("a relationship list", "service", "Network", "/network/relationship-list"),
            arguments("a search endpoint", "generic-vnf", "Network", "/search/vnf/{vnf-id}"),
            arguments("a container", "p-interfaces", "CloudInfrastructure",
                "/cloud-infrastructure/pservers/pserver/{hostname}/p-interfaces"));
    }

    @ParameterizedTest(name = "{0} has no delete")
    @MethodSource("endpointsWithoutADelete")
    public void buildsNothingFor(String endpoint, String xmlRootElementName, String tag,
        String path) {
        DeleteOperation delete =
            new DeleteOperation("TestOpId", xmlRootElementName, tag, path, List.of(vnfId()));
        assertNull(delete.build());
    }

    @Test
    public void buildsTheDeleteOfAnObject() {
        DeleteOperation delete = new DeleteOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()));

        Operation deleteOperation = delete.build();

        assertNotNull(deleteOperation);
        assertEquals(List.of("Network"), deleteOperation.getTags());
        assertEquals("delete an existing generic-vnf", deleteOperation.getSummary());
        assertEquals("delete an existing generic-vnf", deleteOperation.getDescription());
        assertEquals("deleteNetworkGenericVnfsGenericVnf", deleteOperation.getOperationId());
        assertEquals(OperationDefaults.JSON_AND_XML, deleteOperation.getConsumes());
        assertEquals(OperationDefaults.JSON_AND_XML, deleteOperation.getProduces());
        assertEquals(List.of("default"), List.copyOf(deleteOperation.getResponses().keySet()));
    }

    @Test
    public void deletingAnObjectRequiresTheRevisionTheCallerSaw() {
        DeleteOperation delete = new DeleteOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()));

        List<String> parameters = parameterNames(delete.build());

        assertEquals(List.of("vnf-id", "resource-version"), parameters);
    }

    @Test
    public void deletingARelationshipDoesNot() {
        // a relationship carries no revision of its own
        DeleteOperation delete = new DeleteOperation("ServiceXyzRelationship", "relationship",
            "Service", "/service/xyz/relationship", List.of(vnfId()));

        List<String> parameters = parameterNames(delete.build());

        assertEquals(List.of("vnf-id"), parameters);
    }

    @Test
    public void registerAddsObjectPath() {
        DeleteOperation delete = new DeleteOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of());
        delete.register(context);
        assertEquals("generic-vnf", context.getDeletePathObject(VNF_PATH));
    }

    @Test
    public void registerIgnoresRelationshipPath() {
        DeleteOperation delete = new DeleteOperation("TestPathEndsWithRelationship", "relationship",
            "Service", "/service/xyz/relationship", List.of());
        delete.register(context);
        assertTrue(context.getDeletePaths().isEmpty());
    }

    @Test
    public void buildDoesNotRegister() {
        // build() must be free of side effects; registration only happens via register()
        DeleteOperation delete = new DeleteOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of());
        delete.build();
        assertTrue(context.getDeletePaths().isEmpty());
    }

    private static PathParameter vnfId() {
        PathParameter vnfId = new PathParameter();
        vnfId.setName("vnf-id");
        vnfId.setDescription("Unique id of VNF. This is unique across the graph.");
        vnfId.setRequired(true);
        vnfId.setType("string");
        return vnfId;
    }

    private static List<String> parameterNames(Operation operation) {
        return operation.getParameters().stream().map(Parameter::getName).toList();
    }
}
