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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.schemagen.SwaggerGenerationConfiguration;
import org.onap.aai.schemagen.testutils.TestUtilConfigTranslatorforEdges;
import org.onap.aai.setup.SchemaConfigVersions;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.w3c.dom.*;

@SpringJUnitConfig(
    classes = {SchemaConfigVersions.class, SchemaLocationsBean.class,
        TestUtilConfigTranslatorforEdges.class, EdgeIngestor.class, NodeIngestor.class,
        SwaggerGenerationConfiguration.class

    })
@TestPropertySource(properties = {"schema.uri.base.path = /aai", "schema.xsd.maxoccurs = 5000"})
public class NodesYAMLfromOXMTest {
    // public class NodesYAMLfromOXMTest extends AAISetup {
    private static final Logger logger = LoggerFactory.getLogger("NodesYAMLfromOXMTest.class");
    private static final String OXMFILENAME = "src/test/resources/oxm/business_v11.xml";
    private static final String EDGEFILENAME =
        "src/test/resources/dbedgerules/EdgeDescriptionRules_test.json";
    public static AnnotationConfigApplicationContext ctx = null;
    private static String testXML;

    @Autowired
    NodesYAMLfromOXM nodesYamlFromOxm;
    @Autowired
    SchemaConfigVersions schemaConfigVersions;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {

        XSDElementTest x = new XSDElementTest();
        x.setUp();
        testXML = x.testXML;
        logger.debug(testXML);
        BufferedWriter bw = new BufferedWriter(new FileWriter(OXMFILENAME));
        bw.write(testXML);
        bw.close();
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(EDGEFILENAME));
        bw1.write(YAMLfromOXMTest.EdgeDefs());
        bw1.close();

    }

    @BeforeEach
    public void setUp() throws Exception {

        // no shared node-GET state to clear: NodesYAMLfromOXM.process() now starts each version
        // with a fresh NodeGenerationContext
        XSDElementTest x = new XSDElementTest();
        x.setUp();
        testXML = x.testXML;

        logger.debug(testXML);
    }

    /**
     * The whole nodes document for this six-type OXM, pinned byte for byte. See {@link GoldenFile}
     * for how to regenerate a golden after an intentional output change.
     */
    @Test
    public void theDocumentMatchesTheGolden() throws Exception {
        GoldenFile.assertMatches("aai_swagger_v11.business.nodes.golden.yaml", generate());
    }

    @Test
    public void theDocumentDescribesTheApiVersionItWasGeneratedFor() throws Exception {
        YamlDocument document = YamlDocument.parse(generate());

        assertEquals("2.0", document.map().get("swagger"));
        assertEquals("localhost", document.map().get("host"));
        assertEquals("/aai/v11", document.map().get("basePath"));
        assertEquals(List.of("https"), document.map().get("schemes"));
        assertEquals("v11", document.map("info").get("version"));
    }

    /**
     * A node is reached by its type rather than by its place in the inventory tree, which is the
     * one
     * thing that distinguishes this document from the CRUD one.
     */
    @Test
    public void everyNodeTypeIsReachableUnderNodes() throws Exception {
        assertEquals(
            List.of("/nodes/customers/customer/{global-customer-id}",
                "/nodes/customers?parameter=value[&parameter2=value2]",
                "/nodes/service-subscriptions/service-subscription/{service-type}",
                "/nodes/service-subscriptions?parameter=value[&parameter2=value2]"),
            YamlDocument.parse(generate()).keys("paths"));
    }

    /** A node endpoint only ever answers GET; the CRUD document owns the writes. */
    @Test
    public void aNodeEndpointIsReadOnly() throws Exception {
        YamlDocument document = YamlDocument.parse(generate());

        document.keys("paths").forEach(path -> assertEquals(List.of("get"),
            document.keys("paths", path), "expected only a GET on " + path));
    }

    /**
     * The definitions are the CRUD document's, minus the PATCH flavours: a node GET returns the
     * same
     * objects, but there is nothing to PATCH here.
     */
    @Test
    public void theDefinitionsCarryNoPatchFlavours() throws Exception {
        assertEquals(
            List.of("business", "customer", "customers", "inventory", "nodes",
                "service-subscription", "service-subscriptions"),
            YamlDocument.parse(generate()).keys("definitions"));
    }

    /**
     * The nodes swagger emits its standard-type properties through the same
     * {@code XSDElement.getTypeProperty} as the full swagger, so constraint facets reach it without
     * a separate emission path.
     */
    @Test
    public void testProcessWithConstraintFacets() throws Exception {
        XSDElementTest x = new XSDElementTest();
        x.setUpWithFacets();
        nodesYamlFromOxm.setXmlVersion(x.testXML, schemaConfigVersions.getAppRootVersion());

        Map<String, Object> properties = YamlDocument.parse(nodesYamlFromOxm.process())
            .map("definitions", "customer", "properties");

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
    }

    @Test
    public void testGetXMLRootElementName() throws Exception {
        generate();
        Element customer = nodesYamlFromOxm.getJavaTypeElementSwagger("Customer");
        assertEquals("customer", nodesYamlFromOxm.getXMLRootElementName(customer));
    }

    @Test
    public void testGetXmlRootElementName() throws Exception {
        generate();
        assertEquals("customer", nodesYamlFromOxm.getXmlRootElementName("Customer"));
    }

    @Test
    public void testGetJavaTypeElementSwagger() throws Exception {
        generate();
        Element customer = nodesYamlFromOxm.getJavaTypeElementSwagger("Customer");
        assertEquals("java-type", customer.getNodeName());
        assertEquals("Customer", customer.getAttribute("name"));
    }

    private String generate() throws Exception {
        nodesYamlFromOxm.setXmlVersion(testXML, schemaConfigVersions.getAppRootVersion());
        return nodesYamlFromOxm.process();
    }

    @Test
    public void testSetOxmVersion() {
        File oxmFile = new File(OXMFILENAME);
        SchemaVersion version = schemaConfigVersions.getAppRootVersion();

        nodesYamlFromOxm.setOxmVersion(oxmFile, version);

        assertEquals(version, nodesYamlFromOxm.v);
        assertEquals(oxmFile, nodesYamlFromOxm.oxmFile);
    }

    @Test
    public void testSetVersion() {
        SchemaVersion version = schemaConfigVersions.getAppRootVersion();

        nodesYamlFromOxm.setVersion(version);

        assertEquals(version, nodesYamlFromOxm.v);
    }

    /** A relationship is published as a dictionary of itself, wrapping the {@code -dict} entry. */
    @Test
    public void aDictionaryWrapsTheDefinitionItIsNamedAfter() {
        ModelImpl dictionary = nodesYamlFromOxm.dictionaryOf("business");

        assertEquals("object", dictionary.getType());
        assertEquals("dictionary of business\n", dictionary.getDescription());
        ArrayProperty entries =
            assertInstanceOf(ArrayProperty.class, dictionary.getProperties().get("business"));
        assertEquals("#/definitions/business-dict",
            assertInstanceOf(RefProperty.class, entries.getItems()).get$ref());
    }

    /**
     * The stored-only tail of a relationship - {@code related-to-property} onwards - is not part of
     * what the documents publish.
     */
    @Test
    public void thePublishedRelationshipDropsItsStoredOnlyTail() {
        ModelImpl relationship = new ModelImpl();
        relationship.addProperty("related-to", new StringProperty());
        relationship.addProperty("related-to-property", new StringProperty());
        relationship.addProperty("relationship-data", new StringProperty());

        ModelImpl published = nodesYamlFromOxm.publishedRelationshipDict(relationship);

        assertEquals(List.of("related-to"), List.copyOf(published.getProperties().keySet()));
    }

    @Test
    public void theDefinitionsCanBeRestrictedToOneNamespace() {
        nodesYamlFromOxm.javaTypeDefinitions =
            Map.of("customer", new ModelImpl(), "pserver", new ModelImpl());

        assertEquals(List.of("customer"),
            List.copyOf(nodesYamlFromOxm.definitions(Set.of("customer")).keySet()));
    }

    @Test
    public void theDefinitionsOfNothingAreEmpty() {
        nodesYamlFromOxm.javaTypeDefinitions = Map.of();

        assertTrue(nodesYamlFromOxm.definitions().isEmpty());
    }

    @Test
    public void testSetNodeIngestor() {

        Set<Translator> translatorSet = new HashSet<>();
        Translator mockTranslator = Mockito.mock(Translator.class);
        translatorSet.add(mockTranslator);

        NodeIngestor mockNodeIngestor = new NodeIngestor(translatorSet);

        nodesYamlFromOxm.setNodeIngestor(mockNodeIngestor);

        NodeIngestor result = nodesYamlFromOxm.ni;
        assertEquals(mockNodeIngestor, result, "NodeIngestor should be set correctly.");
    }

    @Test
    public void testSetEdgeIngestor() {

        Set<Translator> translatorSet = new HashSet<>();
        Translator mockTranslator = Mockito.mock(Translator.class);
        translatorSet.add(mockTranslator);

        EdgeIngestor mockEdgeIngestor = new EdgeIngestor(translatorSet);

        nodesYamlFromOxm.setEdgeIngestor(mockEdgeIngestor);

        EdgeIngestor result = nodesYamlFromOxm.ei;

        assertEquals(mockEdgeIngestor, result, "EdgeIngestor should be set correctly.");
    }

    @Test
    public void testSetCombinedJavaTypes() {
        Map<String, Integer> mockJavaTypes = Map.of("String", 1, "Integer", 2);

        nodesYamlFromOxm.setCombinedJavaTypes(mockJavaTypes);

        assertEquals(mockJavaTypes, nodesYamlFromOxm.getCombinedJavaTypes());
    }

    @Test
    public void testVersionSupportsBasePathProperty_versionBeforeMinBasepath() {
        // Act: Test with a version before the base path property support (e.g., v5)
        String version = "v5";
        boolean result = nodesYamlFromOxm.versionSupportsBasePathProperty(version);

        assertTrue(result, "Version v5 should support the base path property.");
    }

    @Test
    public void testVersionSupportsBasePathProperty_versionEqualToMinBasepath() {
        String version = "v6";
        boolean result = nodesYamlFromOxm.versionSupportsBasePathProperty(version);

        assertTrue(result, "Version v6 should support the base path property.");
    }

    @Test
    public void testVersionSupportsBasePathProperty_versionAfterMinBasepath() {
        String version = "v7";
        boolean result = nodesYamlFromOxm.versionSupportsBasePathProperty(version);

        assertFalse(result, "Version v7 should NOT support the base path property.");
    }

    @Test
    public void testVersionSupportsBasePathProperty_invalidVersionFormat() {
        String version = "v";

        assertThrows(NumberFormatException.class, () -> {
            nodesYamlFromOxm.versionSupportsBasePathProperty(version);
        });
    }

    // Adding test cases for `versionSupportsSwaggerDiff`

    @Test
    public void testVersionSupportsSwaggerDiff_versionEqualToSwaggerDiffStartVersion() {
        String version = "v6";
        boolean result = nodesYamlFromOxm.versionSupportsSwaggerDiff(version);

        assertTrue(result, "Version v6 should support Swagger Diff.");
    }

    @Test
    public void testVersionSupportsSwaggerDiff_invalidVersionFormat() {
        String version = "v";

        assertThrows(NumberFormatException.class, () -> {
            nodesYamlFromOxm.versionSupportsSwaggerDiff(version);
        });
    }

    @Test
    public void testGetTopLevelPathsCheckForFalse() {
        // Arrange: Mock XML structure and expected paths
        String xmlContent = "<root>" + "<java-attributes>"
            + "<xml-element type=\"com.example.TopLevel1\"/>"
            + "<xml-element type=\"com.example.TopLevel2\"/>" + "</java-attributes>" + "</root>";

        // Set the XML content (this assumes the setXmlVersion method processes the XML)
        nodesYamlFromOxm.setXmlVersion(xmlContent, schemaConfigVersions.getAppRootVersion());

        XSDElement mockElement = Mockito.mock(XSDElement.class);

        NodeList mockJavaAttributesNodeList = mockJavaAttributesNodeList();

        Mockito.when(mockElement.getElementsByTagName("java-attributes"))
            .thenReturn(mockJavaAttributesNodeList);

        nodesYamlFromOxm.getTopLevelPaths(mockElement);

        assertFalse(nodesYamlFromOxm.topLevelPaths.contains("TopLevel1"));
        assertFalse(nodesYamlFromOxm.topLevelPaths.contains("TopLevel2"));
    }

    private NodeList mockJavaAttributesNodeList() {
        NodeList mockNodeList = Mockito.mock(NodeList.class);

        // Create and return the mocked NodeList containing the <xml-element> nodes
        Element mockJavaAttributesElement = Mockito.mock(Element.class);

        Node mockParentElement = Mockito.mock(Node.class);

        Mockito.when(mockNodeList.getLength()).thenReturn(1);
        Mockito.when(mockNodeList.item(0)).thenReturn(mockJavaAttributesElement);

        // Mock behavior for the getElementsByTagName("xml-element") for the <java-attributes> node
        NodeList mockXmlElementNodes = mockXmlElementNodes(mockJavaAttributesElement);
        Mockito.when(mockJavaAttributesElement.getElementsByTagName("xml-element"))
            .thenReturn(mockXmlElementNodes);

        Mockito.when(mockJavaAttributesElement.getParentNode()).thenReturn(mockParentElement);

        return mockNodeList;
    }

    private NodeList mockXmlElementNodes(Element parentElement) {
        NodeList mockNodeList = Mockito.mock(NodeList.class);

        Element mockElement1 = Mockito.mock(Element.class);
        NamedNodeMap mockAttributes1 = Mockito.mock(NamedNodeMap.class);
        Attr mockAttr1 = Mockito.mock(Attr.class);
        Mockito.when(mockAttr1.getValue()).thenReturn("com.example.TopLevel1");
        Mockito.when(mockAttributes1.getNamedItem("type")).thenReturn(mockAttr1);
        Mockito.when(mockElement1.getAttributes()).thenReturn(mockAttributes1);
        Mockito.when(mockElement1.getNodeName()).thenReturn("xml-element");

        // Set the parent node for mockElement1
        Mockito.when(mockElement1.getParentNode()).thenReturn(parentElement);

        // Create and mock the second <xml-element> (TopLevel2)
        Element mockElement2 = Mockito.mock(Element.class);
        NamedNodeMap mockAttributes2 = Mockito.mock(NamedNodeMap.class);
        Attr mockAttr2 = Mockito.mock(Attr.class);
        Mockito.when(mockAttr2.getValue()).thenReturn("com.example.TopLevel2");
        Mockito.when(mockAttributes2.getNamedItem("type")).thenReturn(mockAttr2);
        Mockito.when(mockElement2.getAttributes()).thenReturn(mockAttributes2);
        Mockito.when(mockElement2.getNodeName()).thenReturn("xml-element");

        Mockito.when(mockElement2.getParentNode()).thenReturn(parentElement);

        Mockito.when(mockNodeList.getLength()).thenReturn(2); // Ensure that getLength() returns 2
        Mockito.when(mockNodeList.item(0)).thenReturn(mockElement1);
        Mockito.when(mockNodeList.item(1)).thenReturn(mockElement2);

        return mockNodeList;
    }
}
