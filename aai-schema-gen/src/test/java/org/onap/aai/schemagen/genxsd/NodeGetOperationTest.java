/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright © 2025-2026 Deutsche Telekom.
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

import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class NodeGetOperationTest {

    private static final String VNF_PATH = "/network/generic-vnfs/generic-vnf/{vnf-id}";
    private static final String CONTAINER_PATH =
        "/cloud-infrastructure/pservers/pserver/{hostname}/p-interfaces";
    private static final String CONTAINER = "p-interfaces";

    /**
     * A fresh context per test. Previously the container map and checklist were static, so tests
     * leaked state into each other; an explicit context removes that ordering coupling.
     */
    private NodeGenerationContext context;

    @BeforeEach
    public void setUp() {
        context = new NodeGenerationContext();
        context.addContainerProps(CONTAINER,
            List.of(indexedProperty("interface-name"), indexedProperty("prov-status")));
    }

    @Test
    public void buildsTheGetOfANodeType() {
        NodeGetOperation get = new NodeGetOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), context);

        Operation getOperation = get.build();

        assertNotNull(getOperation);
        // every node endpoint is tagged Operations, whatever the CRUD path's own tag was
        assertEquals(List.of("Operations"), getOperation.getTags());
        assertEquals("returns generic-vnf", getOperation.getSummary());
        assertEquals("returns generic-vnf", getOperation.getDescription());
        assertEquals("getNetworkGenericVnfsGenericVnf", getOperation.getOperationId());
        assertEquals(OperationDefaults.JSON_AND_XML, getOperation.getProduces());
        assertEquals(List.of("200", "default"), List.copyOf(getOperation.getResponses().keySet()));
        assertEquals("#/definitions/generic-vnf", assertInstanceOf(RefModel.class,
            getOperation.getResponses().get("200").getResponseSchema()).get$ref());
    }

    @Test
    public void aNodeIsReachedByItsTypeRatherThanItsPlaceInTheTree() {
        NodeGetOperation get = new NodeGetOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), context);

        assertEquals("/nodes/generic-vnfs/generic-vnf/{vnf-id}", get.getPath());
    }

    @Test
    public void anEndpointAddressingNoSingleObjectIsQueriedByProperty() {
        NodeGetOperation get = new NodeGetOperation("CloudInfrastructurePserversPserverPInterfaces",
            CONTAINER, "CloudInfrastructure", CONTAINER_PATH, List.of(hostname()), context);

        assertEquals("/nodes/p-interfaces?parameter=value[&parameter2=value2]", get.getPath());
    }

    @Test
    public void aContainerIsFilterableByItsIndexedProperties() {
        NodeGetOperation get = new NodeGetOperation("CloudInfrastructurePserversPserverPInterfaces",
            CONTAINER, "CloudInfrastructure", CONTAINER_PATH, List.of(hostname()), context);

        List<String> parameters =
            get.build().getParameters().stream().map(Parameter::getName).toList();

        assertEquals(List.of("hostname", "interface-name", "prov-status"), parameters);
    }

    @ParameterizedTest(name = "no node get for a crud path {0}")
    @ValueSource(
        strings = {VNF_PATH + "/relationship", VNF_PATH + "/relationship/some-id",
            VNF_PATH + "/relationship-list", "/search" + VNF_PATH, "/actions" + VNF_PATH,
            "/nodes" + VNF_PATH})
    public void buildsNothingForCrudPath(String crudPath) {
        NodeGetOperation get = new NodeGetOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", crudPath, List.of(vnfId()), context);
        assertNull(get.build());
    }

    @Test
    public void buildsNothingForAnUntaggedEndpoint() {
        NodeGetOperation get = new NodeGetOperation("GenericVnf", "generic-vnf", "", VNF_PATH,
            List.of(vnfId()), context);
        assertNull(get.build());
    }

    @Test
    public void aNodeTypeYieldsAtMostOneEndpoint() {
        NodeGetOperation first = new NodeGetOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), context);
        assertNotNull(first.build());
        first.register();

        NodeGetOperation second = new NodeGetOperation("BusinessCustomersGenericVnf", "generic-vnf",
            "Business", "/business/customers/customer/{global-customer-id}/generic-vnf/{vnf-id}",
            List.of(vnfId()), context);

        assertNull(second.build());
    }

    @Test
    public void buildDoesNotRegister() {
        // build() must be free of side effects; registration only happens via register()
        NodeGetOperation get = new NodeGetOperation("NetworkGenericVnfsGenericVnf", "generic-vnf",
            "Network", VNF_PATH, List.of(vnfId()), context);
        get.build();
        assertNotNull(get.build());
    }

    private static PathParameter vnfId() {
        return pathParameter("vnf-id", "Unique id of VNF. This is unique across the graph.");
    }

    private static PathParameter hostname() {
        return pathParameter("hostname", "Value from executing hostname on the compute node.");
    }

    private static PathParameter pathParameter(String name, String description) {
        PathParameter parameter = new PathParameter();
        parameter.setName(name);
        parameter.setDescription(description);
        parameter.setRequired(true);
        parameter.setType("string");
        return parameter;
    }

    private static QueryParameter indexedProperty(String name) {
        QueryParameter property = new QueryParameter();
        property.setName(name);
        property.setRequired(false);
        property.setType("string");
        return property;
    }
}
