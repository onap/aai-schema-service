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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

        NodeGetOperation.checklist.clear();
        XSDElementTest x = new XSDElementTest();
        x.setUp();
        testXML = x.testXML;

        logger.debug(testXML);
    }

    @Test
    public void testGetDocumentHeader() {
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String apiVersion = v.toString();
        String header = null;
        try {
            nodesYamlFromOxm.setXmlVersion(testXML, v);
            nodesYamlFromOxm.process();
            header = nodesYamlFromOxm.getDocumentHeader();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat("Header:\n" + header, header, is(YAMLheader()));
    }

    @Test
    public void testProcess() {

        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String apiVersion = v.toString();
        String fileContent = null;
        try {
            nodesYamlFromOxm.setXmlVersion(testXML, v);
            fileContent = nodesYamlFromOxm.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat("FileContent-I:\n" + fileContent, fileContent, is(YAMLresult()));
    }

    @Test
    public void testNodesYAMLfromOXMFileVersionFile() throws IOException {
        String outfileName = "testXML.xml";
        File XMLfile = new File(outfileName);
        XMLfile.createNewFile();
        BufferedWriter bw = null;
        Charset charset = Charset.forName("UTF-8");
        Path path = Paths.get(outfileName);
        bw = Files.newBufferedWriter(path, charset);
        bw.write(testXML);
        bw.close();
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String apiVersion = v.toString();
        String fileContent = null;
        try {
            nodesYamlFromOxm.setXmlVersion(testXML, v);
            fileContent = nodesYamlFromOxm.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
        XMLfile.delete();
        assertThat("FileContent:\n" + fileContent, fileContent, is(YAMLresult()));
    }

    @Test
    public void testNodesYAMLfromOXMStringVersionFile() {
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String apiVersion = v.toString();
        String fileContent = null;
        try {
            nodesYamlFromOxm.setXmlVersion(testXML, v);
            fileContent = nodesYamlFromOxm.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat("FileContent-II:\n" + fileContent, fileContent, is(YAMLresult()));
    }

    @Test
    public void testAppendDefinitions() {
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String apiVersion = v.toString();
        String definitions = null;
        try {
            nodesYamlFromOxm.setXmlVersion(testXML, v);
            nodesYamlFromOxm.process();
            definitions = nodesYamlFromOxm.appendDefinitions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat("Definitions:\n" + definitions, definitions, is(YAMLgetDefs()));
    }

    @Test
    public void testGetXMLRootElementName() {
        String target = "RootElement=customer";
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String apiVersion = v.toString();
        Element customer = null;
        String root = null;
        try {
            nodesYamlFromOxm.setXmlVersion(testXML, v);
            nodesYamlFromOxm.process();
            customer = nodesYamlFromOxm.getJavaTypeElementSwagger("Customer");
            root = nodesYamlFromOxm.getXMLRootElementName(customer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat("RootElement=" + root, is(target));
    }

    @Test
    public void testGetXmlRootElementName() {
        String target = "RootElement=customer";
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String apiVersion = v.toString();
        String root = null;
        try {
            nodesYamlFromOxm.setXmlVersion(testXML, v);
            nodesYamlFromOxm.process();
            root = nodesYamlFromOxm.getXmlRootElementName("Customer");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat("RootElement=" + root, is(target));
    }

    @Test
    public void testGetJavaTypeElementSwagger() {
        String target = "Element=java-type/Customer";
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String apiVersion = v.toString();
        Element customer = null;
        try {
            nodesYamlFromOxm.setXmlVersion(testXML, v);
            nodesYamlFromOxm.process();
            customer = nodesYamlFromOxm.getJavaTypeElementSwagger("Customer");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat("Element=" + customer.getNodeName() + "/" + customer.getAttribute("name"),
            is(target));
    }

    public String YAMLresult() {
        StringBuilder sb = new StringBuilder(32368);
        sb.append(YAMLheader());
        sb.append(YAMLops());
        sb.append(YAMLgetDefs());
        return sb.toString();
    }

    public String YAMLheader() {
        StringBuilder sb = new StringBuilder(1500);
        sb.append("#").append(OxmFileProcessor.LINE_SEPARATOR).append(
            "# ============LICENSE_START=======================================================")
            .append(OxmFileProcessor.LINE_SEPARATOR).append("# org.onap.aai")
            .append(OxmFileProcessor.LINE_SEPARATOR)
            .append(
                "# ================================================================================")
            .append(OxmFileProcessor.LINE_SEPARATOR)
            .append("# Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.")
            .append(OxmFileProcessor.LINE_SEPARATOR)
            .append(
                "# ================================================================================")
            .append(OxmFileProcessor.LINE_SEPARATOR)
            .append(
                "# Licensed under the Creative Commons License, Attribution 4.0 Intl. (the \"License\");")
            .append(OxmFileProcessor.LINE_SEPARATOR)
            .append("# you may not use this file except in compliance with the License.")
            .append(OxmFileProcessor.LINE_SEPARATOR)
            .append("# You may obtain a copy of the License at")
            .append(OxmFileProcessor.LINE_SEPARATOR).append("# <p>")
            .append(OxmFileProcessor.LINE_SEPARATOR)
            .append("# https://creativecommons.org/licenses/by/4.0/")
            .append(OxmFileProcessor.LINE_SEPARATOR).append("# <p>")
            .append(OxmFileProcessor.LINE_SEPARATOR)
            .append("# Unless required by applicable law or agreed to in writing, software")
            .append(OxmFileProcessor.LINE_SEPARATOR)
            .append("# distributed under the License is distributed on an \"AS IS\" BASIS,")
            .append(OxmFileProcessor.LINE_SEPARATOR)
            .append("# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.")
            .append(OxmFileProcessor.LINE_SEPARATOR)
            .append("# See the License for the specific language governing permissions and")
            .append(OxmFileProcessor.LINE_SEPARATOR).append("# limitations under the License.")
            .append(OxmFileProcessor.LINE_SEPARATOR)
            .append(
                "# ============LICENSE_END=========================================================")
            .append(OxmFileProcessor.LINE_SEPARATOR).append("#")
            .append(OxmFileProcessor.LINE_SEPARATOR).append(OxmFileProcessor.LINE_SEPARATOR);
        sb.append("swagger: \"2.0\"\n");
        sb.append("info:" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("  description: |\n");
        sb.append(
            "    [Differences versus the previous schema version](apidocs/aai/aai_swagger_v11.diff)"
                + OxmFileProcessor.DOUBLE_LINE_SEPARATOR);
        sb.append(
            "    This document is best viewed with Firefox or Chrome. Nodes can be found by opening the models link below and finding the node-type. Edge definitions can be found with the node definitions."
                + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("  version: \"v11\"" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append(
            "  title: Active and Available Inventory REST API" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("  license:" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("    name: Apache 2.0" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("    url: http://www.apache.org/licenses/LICENSE-2.0.html"
            + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("host: localhost" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("basePath: /aai/v11" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("schemes:" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("  - https\n");
        sb.append("paths:" + OxmFileProcessor.LINE_SEPARATOR);
        return sb.toString();
    }

    public String YAMLops() {
        StringBuilder sb = new StringBuilder(16384);
        sb.append("  /nodes/customers/customer/{global-customer-id}:\n");
        sb.append("    get:\n");
        sb.append("      tags:\n");
        sb.append("        - Operations\n");
        sb.append("      summary: returns customer\n");
        sb.append("      description: returns customer\n");
        sb.append("      operationId: getBusinessCustomersCustomer\n");
        sb.append("      produces:\n");
        sb.append("        - application/json\n");
        sb.append("        - application/xml\n");
        sb.append("      responses:\n");
        sb.append("        \"200\":\n");
        sb.append("          description: successful operation\n");
        sb.append("          schema:\n");
        sb.append("              $ref: \"#/definitions/customer\"\n");
        sb.append("        \"default\":\n");
        sb.append("          null\n      parameters:\n");
        sb.append("        - name: global-customer-id\n");
        sb.append("          in: path\n");
        sb.append(
            "          description: Global customer id used across to uniquely identify customer.\n");
        sb.append("          required: true\n");
        sb.append("          type: string\n");
        sb.append("  /nodes/customers?parameter=value[&parameter2=value2]:\n");
        sb.append("    get:\n");
        sb.append("      tags:\n");
        sb.append("        - Operations\n");
        sb.append("      summary: returns customers\n");
        sb.append("      description: returns customers\n");
        sb.append("      operationId: getBusinessCustomers\n");
        sb.append("      produces:\n");
        sb.append("        - application/json\n");
        sb.append("        - application/xml\n");
        sb.append("      responses:\n");
        sb.append("        \"200\":\n");
        sb.append("          description: successful operation\n");
        sb.append("          schema:\n");
        sb.append("              $ref: \"#/definitions/customers\"\n");
        sb.append("        \"default\":\n");
        sb.append("          null\n      parameters:\n");
        sb.append("        - name: global-customer-id\n");
        sb.append("          in: query\n");
        sb.append("          required: false\n");
        sb.append("          type: string\n");
        sb.append("        - name: subscriber-name\n");
        sb.append("          in: query\n");
        sb.append("          required: false\n");
        sb.append("          type: string\n");
        sb.append("        - name: subscriber-type\n");
        sb.append("          in: query\n");
        sb.append("          required: false\n");
        sb.append("          type: string\n");
        sb.append("  /nodes/service-subscriptions/service-subscription/{service-type}:\n");
        sb.append("    get:\n");
        sb.append("      tags:\n");
        sb.append("        - Operations\n");
        sb.append("      summary: returns service-subscription\n");
        sb.append("      description: returns service-subscription\n");
        sb.append(
            "      operationId: getBusinessCustomersCustomerServiceSubscriptionsServiceSubscription\n");
        sb.append("      produces:\n");
        sb.append("        - application/json\n");
        sb.append("        - application/xml\n");
        sb.append("      responses:\n");
        sb.append("        \"200\":\n");
        sb.append("          description: successful operation\n");
        sb.append("          schema:\n");
        sb.append("              $ref: \"#/definitions/service-subscription\"\n");
        sb.append("        \"default\":\n");
        sb.append("          null\n      parameters:\n");
        sb.append("        - name: service-type\n");
        sb.append("          in: path\n");
        sb.append(
            "          description: Value defined by orchestration to identify this service.\n");
        sb.append("          required: true\n");
        sb.append("          type: string\n");
        sb.append("  /nodes/service-subscriptions?parameter=value[&parameter2=value2]:\n");
        sb.append("    get:\n");
        sb.append("      tags:\n");
        sb.append("        - Operations\n");
        sb.append("      summary: returns service-subscriptions\n");
        sb.append("      description: returns service-subscriptions\n");
        sb.append("      operationId: getBusinessCustomersCustomerServiceSubscriptions\n");
        sb.append("      produces:\n");
        sb.append("        - application/json\n");
        sb.append("        - application/xml\n");
        sb.append("      responses:\n");
        sb.append("        \"200\":\n");
        sb.append("          description: successful operation\n");
        sb.append("          schema:\n");
        sb.append("              $ref: \"#/definitions/service-subscriptions\"\n");
        sb.append("        \"default\":\n");
        sb.append("          null\n      parameters:\n");
        sb.append("        - name: service-type\n");
        sb.append("          in: query\n");
        sb.append("          required: false\n");
        sb.append("          type: string\n");
        return sb.toString();
    }

    public String YAMLgetDefs() {
        StringBuilder sb = new StringBuilder(8092);
        sb.append("definitions:\n");
        sb.append("  business:\n");
        sb.append("    description: |\n");
        sb.append("      Namespace for business related constructs\n");
        sb.append("    properties:\n");
        sb.append("      customers:\n");
        sb.append("        type: array\n");
        sb.append("        items:\n");
        sb.append("          $ref: \"#/definitions/customer\"\n");
        sb.append("  customer:\n");
        sb.append("    description: |\n");
        sb.append("      customer identifiers to provide linkage back to BSS information.\n");
        sb.append("      ###### Related Nodes\n");
        sb.append(
            "      - FROM service-subscription (CHILD of customer, service-subscription BelongsTo customer, MANY2ONE)(1)\n");
        sb.append("\n");
        sb.append("      -(1) IF this CUSTOMER node is deleted, this FROM node is DELETED also\n");
        sb.append("    required:\n");
        sb.append("    - global-customer-id\n");
        sb.append("    - subscriber-name\n");
        sb.append("    - subscriber-type\n");
        sb.append("    properties:\n");
        sb.append("      global-customer-id:\n");
        sb.append("        type: string\n");
        sb.append(
            "        description: Global customer id used across to uniquely identify customer.\n");
        sb.append("      subscriber-name:\n");
        sb.append("        type: string\n");
        sb.append(
            "        description: Subscriber name, an alternate way to retrieve a customer.\n");
        sb.append("      subscriber-type:\n");
        sb.append("        type: string\n");
        sb.append(
            "        description: Subscriber type, a way to provide VID with only the INFRA customers.\n");
        sb.append("      resource-version:\n");
        sb.append("        type: string\n");
        sb.append(
            "        description: Used for optimistic concurrency.  Must be empty on create, valid on update and delete.\n");
        sb.append("      service-subscriptions:\n");
        sb.append("        type: array\n");
        sb.append("        items:\n");
        sb.append("          $ref: \"#/definitions/service-subscription\"\n");
        sb.append("  customers:\n");
        sb.append("    description: |\n");
        sb.append(
            "      Collection of customer identifiers to provide linkage back to BSS information.\n");
        sb.append("    properties:\n");
        sb.append("      customer:\n");
        sb.append("        type: array\n");
        sb.append("        items:          \n");
        sb.append("          $ref: \"#/definitions/customer\"\n");
        sb.append("  inventory:\n");
        sb.append("    properties:\n");
        sb.append("      business:\n");
        sb.append("        type: object\n");
        sb.append("        $ref: \"#/definitions/business\"\n");
        sb.append("  nodes:" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("    properties:" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("      inventory-item-data:" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("        type: array" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("        items:" + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("          $ref: \"#/definitions/inventory-item-data\""
            + OxmFileProcessor.LINE_SEPARATOR);
        sb.append("  service-subscription:\n");
        sb.append("    description: |\n");
        sb.append("      Object that group service instances.\n");
        sb.append("      ###### Related Nodes\n");
        sb.append(
            "      - TO customer (PARENT of service-subscription, service-subscription BelongsTo customer, MANY2ONE)(4)\n");
        sb.append("      - TO tenant( service-subscription Uses tenant, MANY2MANY)\n");
        sb.append(
            "      - FROM service-instance (CHILD of service-subscription, service-instance BelongsTo service-subscription, MANY2ONE)(1)\n");
        sb.append("\n");
        sb.append(
            "      -(1) IF this SERVICE-SUBSCRIPTION node is deleted, this FROM node is DELETED also\n");
        sb.append(
            "      -(4) IF this TO node is deleted, this SERVICE-SUBSCRIPTION is DELETED also\n");
        sb.append("    required:\n");
        sb.append("    - service-type\n");
        sb.append("    properties:\n");
        sb.append("      service-type:\n");
        sb.append("        type: string\n");
        sb.append(
            "        description: Value defined by orchestration to identify this service.\n");
        sb.append("      temp-ub-sub-account-id:\n");
        sb.append("        type: string\n");
        sb.append(
            "        description: This property will be deleted from A&AI in the near future. Only stop gap solution.\n");
        sb.append("      resource-version:\n");
        sb.append("        type: string\n");
        sb.append(
            "        description: Used for optimistic concurrency.  Must be empty on create, valid on update and delete.\n");
        sb.append("  service-subscriptions:\n");
        sb.append("    description: |\n");
        sb.append("      Collection of objects that group service instances.\n");
        sb.append("    properties:\n");
        sb.append("      service-subscription:\n");
        sb.append("        type: array\n");
        sb.append("        items:          \n");
        sb.append("          $ref: \"#/definitions/service-subscription\"\n");
        return sb.toString();
    }

    @Test
    public void testSetOxmVersion() {
        File oxmFile = new File(OXMFILENAME);
        SchemaVersion version = schemaConfigVersions.getAppRootVersion();

        nodesYamlFromOxm.setOxmVersion(oxmFile, version);
        String header = nodesYamlFromOxm.getDocumentHeader();
        logger.debug("Header: " + header);

        assertTrue(header.contains(version.toString()));
    }

    @Test
    public void testSetVersion() {
        SchemaVersion version = schemaConfigVersions.getAppRootVersion();

        nodesYamlFromOxm.setVersion(version);
        String header = nodesYamlFromOxm.getDocumentHeader();
        logger.debug("Header: " + header);

        assertTrue(header.contains(version.toString()));
    }


    @Test
    public void testGetDictionary() throws Exception {
        String resource = "business";
        Method getDictionaryMethod = NodesYAMLfromOXM.class.getDeclaredMethod("getDictionary", String.class);
        getDictionaryMethod.setAccessible(true);

        String result = (String) getDictionaryMethod.invoke(nodesYamlFromOxm, resource);

        String expectedResult = "  business:\n"
            + "    description: |\n"
            + "      dictionary of business\n"
            + "    type: object\n"
            + "    properties:\n"
            + "      business:\n"
            + "        type: array\n"
            + "        items:\n"
            + "          $ref: \"#/definitions/business-dict\"\n";

        // Verify that the result matches the expected output
        assertThat(result, is(expectedResult));
    }

    @Test
    public void testAppendDefinitions_WithFilter() {
        Map<String, String> mockJavaTypeDefinitions = new HashMap<>();

        mockJavaTypeDefinitions.put("namespace1", "definition1");
        mockJavaTypeDefinitions.put("namespace2", "definition2");
        mockJavaTypeDefinitions.put("relationship-dict", "definition-for-relationship-dict related-to-property: extra-info-to-remove");

        nodesYamlFromOxm.javaTypeDefinitions = mockJavaTypeDefinitions;

        Set<String> namespaceFilter = new HashSet<>();
        namespaceFilter.add("namespace1");

        String definitions = nodesYamlFromOxm.appendDefinitions(namespaceFilter);

        String expectedDefinitions = "definitions:\ndefinition1";

        assertThat("Definitions (with filter):\n" + definitions, definitions, is(expectedDefinitions));
    }

    @Test
    public void testAppendDefinitions_EmptyDefinitions() {
        Map<String, String> mockJavaTypeDefinitions = new HashMap<>();

        nodesYamlFromOxm.javaTypeDefinitions = mockJavaTypeDefinitions;

        String definitions = nodesYamlFromOxm.appendDefinitions(); // This calls appendDefinitions(null)

        String expectedDefinitions = "definitions:\n";

        assertThat("Definitions (empty map):\n" + definitions, definitions, is(expectedDefinitions));
    }

    @Test
    public void testAppendDefinitions_RelationshipDictProcessing() {
        Map<String, String> mockJavaTypeDefinitions = new HashMap<>();

        mockJavaTypeDefinitions.put("namespace1", "definition1");
        mockJavaTypeDefinitions.put("relationship-dict", "definition-for-relationship-dict related-to-property: extra-info-to-remove");

        nodesYamlFromOxm.javaTypeDefinitions = mockJavaTypeDefinitions;

        String definitions = nodesYamlFromOxm.appendDefinitions();

        String expectedDefinitions = "definitions:\ndefinition1definition-for-relationship-dict";

        assertEquals(expectedDefinitions,definitions);
    }

    @Test
    public void testAppendDefinitions_NoFilter() {
        Map<String, String> mockJavaTypeDefinitions = new HashMap<>();

        mockJavaTypeDefinitions.put("namespace1", "definition1");
        mockJavaTypeDefinitions.put("namespace2", "definition2");
        mockJavaTypeDefinitions.put("relationship-dict", "definition-for-relationship-dict related-to-property: extra-info-to-remove");

        nodesYamlFromOxm.javaTypeDefinitions = mockJavaTypeDefinitions;

        String definitions = nodesYamlFromOxm.appendDefinitions();

        String expectedDefinitions = "definitions:\ndefinition1definition2definition-for-relationship-dict";

        assertEquals( expectedDefinitions,definitions);
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
    public void testGetCombinedJavaTypes() {
        Map<String, Integer> mockJavaTypes = new HashMap<>();
        mockJavaTypes.put("String", 1);
        mockJavaTypes.put("Integer", 2);
        nodesYamlFromOxm.setCombinedJavaTypes(mockJavaTypes);

        Map<String, Integer> result = nodesYamlFromOxm.getCombinedJavaTypes();

        assertEquals(mockJavaTypes, result, "The combinedJavaTypes should match.");
    }

    @Test
    public void testSetCombinedJavaTypes() {
        Map<String, Integer> mockJavaTypes = new HashMap<>();
        mockJavaTypes.put("String", 1);
        mockJavaTypes.put("Integer", 2);

        nodesYamlFromOxm.setCombinedJavaTypes(mockJavaTypes);

        Map<String, Integer> result = nodesYamlFromOxm.getCombinedJavaTypes();
        assertEquals(mockJavaTypes, result, "The combinedJavaTypes map should be set correctly.");
    }

    // Adding the test cases for `versionSupportsBasePathProperty`

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
    public void testGetTopLevelPathsCheckForFalse(){
        // Arrange: Mock XML structure and expected paths
        String xmlContent = "<root>" +
            "<java-attributes>" +
            "<xml-element type=\"com.example.TopLevel1\"/>" +
            "<xml-element type=\"com.example.TopLevel2\"/>" +
            "</java-attributes>" +
            "</root>";

        // Set the XML content (this assumes the setXmlVersion method processes the XML)
        nodesYamlFromOxm.setXmlVersion(xmlContent, schemaConfigVersions.getAppRootVersion());

        XSDElement mockElement = Mockito.mock(XSDElement.class);

        NodeList mockJavaAttributesNodeList = mockJavaAttributesNodeList();

        Mockito.when(mockElement.getElementsByTagName("java-attributes")).thenReturn(mockJavaAttributesNodeList);

        nodesYamlFromOxm.getTopLevelPaths(mockElement);

        assertFalse(nodesYamlFromOxm.topLevelPaths.contains("TopLevel1"));
        assertFalse(nodesYamlFromOxm.topLevelPaths.contains("TopLevel2"));
    }

    private NodeList mockJavaAttributesNodeList() {
        NodeList mockNodeList = Mockito.mock(NodeList.class);

        // Create and return the mocked NodeList containing the <xml-element> nodes
        Element mockJavaAttributesElement = Mockito.mock(Element.class);

        XSDElement mockParentElement = Mockito.mock(XSDElement.class);

        Mockito.when(mockNodeList.getLength()).thenReturn(1);
        Mockito.when(mockNodeList.item(0)).thenReturn(mockJavaAttributesElement);

        // Mock behavior for the getElementsByTagName("xml-element") for the <java-attributes> node
        NodeList mockXmlElementNodes = mockXmlElementNodes(mockJavaAttributesElement);
        Mockito.when(mockJavaAttributesElement.getElementsByTagName("xml-element")).thenReturn(mockXmlElementNodes);

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

        Mockito.when(mockNodeList.getLength()).thenReturn(2);  // Ensure that getLength() returns 2
        Mockito.when(mockNodeList.item(0)).thenReturn(mockElement1);
        Mockito.when(mockNodeList.item(1)).thenReturn(mockElement2);

        return mockNodeList;
    }
}
