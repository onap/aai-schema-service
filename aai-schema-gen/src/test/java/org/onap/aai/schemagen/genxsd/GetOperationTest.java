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
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GetOperationTest {

    private static final String VNF_PATH = "/network/generic-vnfs/generic-vnf/{vnf-id}";
    private static final String CONTAINER = "p-interfaces";

    /**
     * A fresh context per test. Previously the container map was static, so whichever test ran
     * first
     * decided what the others observed; an explicit context removes that ordering coupling.
     */
    private GenerationContext context;

    @BeforeEach
    public void setUp() {
        context = new GenerationContext();
        context.addContainerProps(CONTAINER,
            List.of(indexedProperty("interface-name"), indexedProperty("prov-status")));
    }

    public static Stream<Arguments> endpointsWithoutAGet() {
        return Stream.of(
            arguments("an untagged endpoint", "generic-vnf", "", "/generic-vnf/{vnf-id}"),
            arguments("a relationship", "relationship", "TestTag", "/network/relationship"),
            arguments("a relationship child", "relationship", "TestTag",
                "/network/relationship/123"),
            arguments("a relationship list", "relationship-list", "TestTag",
                "/network/relationship-list"),
            arguments("a search endpoint", "search", "TestTag", "/search/records"));
    }

    @ParameterizedTest(name = "{0} has no get")
    @MethodSource("endpointsWithoutAGet")
    public void buildsNothingFor(String endpoint, String xmlRootElementName, String tag,
        String path) {
        GetOperation get =
            new GetOperation("TestOpId", xmlRootElementName, tag, path, List.of(vnfId()), context);
        assertNull(get.build());
    }

    @Test
    public void buildsTheGetOfAnObject() {
        GetOperation get = new GetOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), context);

        Operation getOperation = get.build();

        assertNotNull(getOperation);
        assertEquals(List.of("Network"), getOperation.getTags());
        assertEquals("returns generic-vnf", getOperation.getSummary());
        assertEquals("returns generic-vnf", getOperation.getDescription());
        assertEquals("getNetworkGenericVnfsGenericVnf", getOperation.getOperationId());
        assertEquals(OperationDefaults.JSON_AND_XML, getOperation.getProduces());
        // a GET consumes nothing: it has no request body
        assertNull(getOperation.getConsumes());
        assertEquals(List.of("200", "default"), List.copyOf(getOperation.getResponses().keySet()));
    }

    @Test
    public void answersWithTheObjectThePathAddresses() {
        GetOperation get = new GetOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), context);

        RefModel success = assertInstanceOf(RefModel.class,
            get.build().getResponses().get("200").getResponseSchema());

        assertEquals("#/definitions/generic-vnf", success.get$ref());
    }

    @Test
    public void aContainerIsFilterableByItsIndexedProperties() {
        GetOperation get = new GetOperation("CloudInfrastructurePserversPserverPInterfaces",
            CONTAINER, "CloudInfrastructure",
            "/cloud-infrastructure/pservers/pserver/{hostname}/p-interfaces", List.of(vnfId()),
            context);

        List<String> parameters =
            get.build().getParameters().stream().map(Parameter::getName).toList();

        assertEquals(List.of("vnf-id", "interface-name", "prov-status"), parameters);
    }

    @Test
    public void anObjectWithNoIndexedPropertiesHasNoQueryParameters() {
        GetOperation get = new GetOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), context);

        assertEquals(1, get.build().getParameters().size());
    }

    @Test
    public void laterIndexedPropertiesDoNotReachAnAlreadyBuiltOperation() {
        // the generator keeps adding to the container's list as it walks it; an operation is a
        // snapshot of what was known when it was constructed
        List<Parameter> growing = new ArrayList<>(List.of(indexedProperty("hostname")));
        context.addContainerProps("pservers", growing);
        GetOperation get = new GetOperation("CloudInfrastructurePservers", "pservers",
            "CloudInfrastructure", "/cloud-infrastructure/pservers", List.of(), context);

        growing.add(indexedProperty("in-maint"));

        assertEquals(List.of("hostname"),
            get.build().getParameters().stream().map(Parameter::getName).toList());
    }

    private static PathParameter vnfId() {
        PathParameter vnfId = new PathParameter();
        vnfId.setName("vnf-id");
        vnfId.setDescription("Unique id of VNF. This is unique across the graph.");
        vnfId.setRequired(true);
        vnfId.setType("string");
        return vnfId;
    }

    private static QueryParameter indexedProperty(String name) {
        QueryParameter property = new QueryParameter();
        property.setName(name);
        property.setRequired(false);
        property.setType("string");
        return property;
    }
}
