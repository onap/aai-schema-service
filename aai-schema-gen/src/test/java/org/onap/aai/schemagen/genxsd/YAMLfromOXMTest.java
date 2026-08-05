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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.common.collect.Multimap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.SneakyThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.EdgeRule;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.schemagen.SwaggerGenerationConfiguration;
import org.onap.aai.schemagen.testutils.TestUtilConfigTranslatorforBusiness;
import org.onap.aai.setup.SchemaConfigVersions;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.SchemaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@SpringJUnitConfig(
    classes = {SchemaConfigVersions.class, SchemaLocationsBean.class,
        TestUtilConfigTranslatorforBusiness.class, EdgeIngestor.class, NodeIngestor.class,
        SwaggerGenerationConfiguration.class

    })
@TestPropertySource(properties = {"schema.uri.base.path = /aai", "schema.xsd.maxoccurs = 5000"})
public class YAMLfromOXMTest {
    @Autowired
    EdgeIngestor edgeIngestor;

    @Autowired
    NodeIngestor nodeIngestor;
    private static final Logger logger = LoggerFactory.getLogger("YAMLfromOXMTest.class");
    private static final String OXMFILENAME = "src/test/resources/oxm/business_oxm_v11.xml";
    private static final String EDGEFILENAME =
        "src/test/resources/dbedgerules/DbEdgeBusinessRules_test.json";
    private static String testXML;
    protected static final String SERVICE_NAME = "JUNIT";

    @Autowired
    YAMLfromOXM yamlFromOxm;

    @Autowired
    SchemaConfigVersions schemaConfigVersions;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        System.setProperty("AJSC_HOME", ".");
        System.setProperty("BUNDLECONFIG_DIR", "src/test/resources/bundleconfig-local");
        System.setProperty("aai.service.name", SERVICE_NAME);
    }

    @BeforeEach
    public void setUp() throws Exception {
        XSDElementTest x = new XSDElementTest();
        x.setUp();
        writeSchema(x.testXML);
    }

    public void setupRelationship() throws Exception {
        XSDElementTest x = new XSDElementTest();
        x.setUpRelationship();
        writeSchema(x.testXML);
    }

    private static void writeSchema(String oxm) throws Exception {
        testXML = oxm;
        logger.debug(testXML);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(OXMFILENAME))) {
            bw.write(testXML);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(EDGEFILENAME))) {
            bw.write(EdgeDefs());
        }
    }

    @Test
    public void AtestIngestors() throws EdgeRuleNotFoundException {
        Multimap<String, EdgeRule> results =
            edgeIngestor.getAllRules(schemaConfigVersions.getDefaultVersion());
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream().filter((i) -> ((!i.isPrivateEdge()))).forEach((i) -> {
                EdgeDescription ed = new EdgeDescription(i);
                System.out.println(ed.getRuleKey());
            });
        }
        Document doc = nodeIngestor.getSchema(schemaConfigVersions.getDefaultVersion());
        assertNotNull(doc);
    }

    /**
     * The whole document for this six-type OXM, pinned byte for byte. The larger v13 fixture set is
     * pinned the same way by {@link SwaggerGenerationCharacterizationTest}, which also explains how
     * to regenerate a golden.
     */
    @Test
    public void theDocumentMatchesTheGolden() throws Exception {
        GoldenFile.assertMatches("aai_swagger_v11.business.golden.yaml", generate());
    }

    @Test
    public void theDocumentDescribesTheApiVersionItWasGeneratedFor() throws Exception {
        YamlDocument document = YamlDocument.parse(generate());

        assertEquals("2.0", document.map().get("swagger"));
        assertEquals("localhost", document.map().get("host"));
        assertEquals("/aai/v11", document.map().get("basePath"));
        assertEquals(List.of("https"), document.map().get("schemes"));

        Map<String, Object> info = document.map("info");
        assertEquals("v11", info.get("version"));
        assertEquals("Active and Available Inventory REST API", info.get("title"));
        assertEquals(
            Map.of("name", "Apache 2.0", "url", "http://www.apache.org/licenses/LICENSE-2.0.html"),
            info.get("license"));
        assertThat((String) info.get("description"),
            containsString("[Differences versus the previous schema version]"
                + "(apidocs/aai/aai_swagger_v11.diff)"));
    }

    @Test
    public void everyJavaTypeGetsADefinition() throws Exception {
        // sorted by name, so the PATCH flavours follow the definitions they are derived from
        assertEquals(
            List.of("business", "customer", "customers", "inventory", "nodes",
                "service-subscription", "service-subscriptions", "zzzz-patch-customer",
                "zzzz-patch-service-subscription"),
            YamlDocument.parse(generate()).keys("definitions"));
    }

    @Test
    public void everyAddressableObjectGetsAPath() throws Exception {
        assertEquals(List.of(
            "/business/customers/customer/{global-customer-id}/service-subscriptions/service-subscription/{service-type}",
            "/business/customers/customer/{global-customer-id}/service-subscriptions",
            "/business/customers/customer/{global-customer-id}", "/business/customers"),
            YamlDocument.parse(generate()).keys("paths"));
    }

    /**
     * The swagger definitions of an OXM that declares constraint facets carry the matching
     * validation keywords. All six are Swagger 2.0 keywords, so no vendor extension is involved.
     */
    @Test
    public void testProcessWithConstraintFacets() throws Exception {
        XSDElementTest x = new XSDElementTest();
        x.setUpWithFacets();
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        yamlFromOxm.setXmlVersion(x.testXML, v);

        Map<String, Object> properties =
            YamlDocument.parse(yamlFromOxm.process()).map("definitions", "customer", "properties");

        assertEquals(
            Map.of("type", "string", "minLength", 1, "maxLength", 36, "pattern", "^[A-Za-z0-9-]+$",
                "description", "Global customer id used across to uniquely identify customer."),
            properties.get("global-customer-id"));
        // allowedValues becomes a swagger enum
        assertEquals(
            Map.of("type", "string", "enum", List.of("CUST", "INFRA"), "description",
                "Subscriber type, a way to provide VID with only the INFRA customers."),
            properties.get("subscriber-type"));
        assertEquals(Map.of("type", "integer", "format", "int32", "minimum", 0, "maximum", 100,
            "description", "Rank of the customer."), properties.get("customer-rank"));
        // a property without facets is untouched
        assertEquals(
            Map.of("type", "string", "description",
                "Subscriber name, an alternate way to retrieve a customer."),
            properties.get("subscriber-name"));
    }

    @Test
    public void aRelationshipListRefersToTheRelationshipDefinition() throws Exception {
        setupRelationship();

        assertEquals(Map.of("$ref", "#/definitions/relationship"), YamlDocument.parse(generate())
            .map("definitions", "relationship-list", "properties").get("relationship"));
    }

    @Test
    public void testGetXMLRootElementName() throws Exception {
        generate();
        Element customer = yamlFromOxm.getJavaTypeElementSwagger("Customer");
        assertEquals("customer", yamlFromOxm.getXMLRootElementName(customer));
    }

    @Test
    public void testGetXmlRootElementName() throws Exception {
        generate();
        assertEquals("customer", yamlFromOxm.getXmlRootElementName("Customer"));
    }

    @Test
    public void testGetJavaTypeElementSwagger() throws Exception {
        generate();
        Element customer = yamlFromOxm.getJavaTypeElementSwagger("Customer");
        assertEquals("java-type", customer.getNodeName());
        assertEquals("Customer", customer.getAttribute("name"));
    }

    @Test
    public void testInvalidTopLevelTag_Actions_ShouldReturnFalse() {
        assertFalse(yamlFromOxm.validTag("Actions"));
    }

    @Test
    public void testValidTagNull() {
        assertFalse(yamlFromOxm.validTag(null));
    }

    @Test
    @SneakyThrows
    public void testSetVersion() {
        yamlFromOxm.setVersion(new SchemaVersion("v1"));
        assertEquals("v1", fieldOf(yamlFromOxm, "v").toString());
    }

    @Test
    @SneakyThrows
    public void testSetOxmVersion() {
        File oxmFile = new File("path/to/oxm/file");

        yamlFromOxm.setOxmVersion(oxmFile, new SchemaVersion("v1"));

        assertEquals("v1", fieldOf(yamlFromOxm, "v").toString());
        assertEquals(oxmFile, fieldOf(yamlFromOxm, "oxmFile"));
    }

    private String generate() throws Exception {
        yamlFromOxm.setXmlVersion(testXML, schemaConfigVersions.getAppRootVersion());
        return yamlFromOxm.process();
    }

    private static Object fieldOf(YAMLfromOXM generator, String name) throws Exception {
        Field field = OxmFileProcessor.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(generator);
    }

    public static String EdgeDefs() {
        StringBuilder sb = new StringBuilder(8092);
        sb.append("{\n" + "  \"rules\": [\n");
        sb.append("    {\n");
        sb.append("      \"from\": \"service-subscription\",\n");
        sb.append("""
                  "to": "customer",
                  "label": "org.onap.relationships.inventory.BelongsTo",
                  "direction": "OUT",
                  "multiplicity": "MANY2ONE",
                  "contains-other-v": "!${direction}",
                  "delete-other-v": "!${direction}",
                  "prevent-delete": "NONE",
                  "default": "true",
                  "description":""
            """);
        sb.append("    },\n");
        sb.append("""
                {
                  "from": "service-instance",
                  "to": "service-subscription",
                  "label": "org.onap.relationships.inventory.BelongsTo",
                  "direction": "OUT",
                  "multiplicity": "MANY2ONE",
                  "contains-other-v": "!${direction}",
                  "delete-other-v": "!${direction}",
                  "prevent-delete": "NONE",
                  "default": "true",
                  "description":""
                },
            """);
        sb.append("""
                {
                  "from": "service-subscription",
                  "to": "tenant",
                  "label": "org.onap.relationships.inventory.Uses",
                  "direction": "OUT",
                  "multiplicity": "MANY2MANY",
                  "contains-other-v": "NONE",
                  "delete-other-v": "NONE",
                  "prevent-delete": "NONE",
                  "default": "true",
                  "description":""
                }\
            """);
        sb.append("  ]\n" + "}\n");
        return sb.toString();
    }
}
