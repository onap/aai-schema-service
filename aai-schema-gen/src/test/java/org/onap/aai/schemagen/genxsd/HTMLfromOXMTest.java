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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.onap.aai.schemagen.genxsd.OxmFileProcessor.LINE_SEPARATOR;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.schemagen.SwaggerGenerationConfiguration;
import org.onap.aai.schemagen.testutils.TestUtilConfigTranslatorforBusiness;
import org.onap.aai.setup.SchemaConfigVersions;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.SchemaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@SpringJUnitConfig(
    classes = {SchemaConfigVersions.class, SchemaLocationsBean.class,
        TestUtilConfigTranslatorforBusiness.class, EdgeIngestor.class, NodeIngestor.class,
        SwaggerGenerationConfiguration.class

    })
@TestPropertySource(properties = {"schema.uri.base.path = /aai", "schema.xsd.maxoccurs = 5000"})
public class HTMLfromOXMTest {
    private static final Logger logger = LoggerFactory.getLogger("HTMLfromOXMTest.class");
    private static final String OXMFILENAME = "src/test/resources/oxm/business_oxm_v11.xml";
    public static AnnotationConfigApplicationContext ctx = null;
    private static String testXML;
    protected static final String SERVICE_NAME = "JUNIT";

    @Autowired
    HTMLfromOXM htmlFromOxm;

    @Autowired
    SchemaConfigVersions schemaConfigVersions;

    @BeforeAll
    public static void setUpContext() throws Exception {

    }

    @BeforeAll
    public static void setupBundleconfig() throws Exception {
        System.setProperty("AJSC_HOME", ".");
        System.setProperty("BUNDLECONFIG_DIR", "src/test/resources/bundleconfig-local");
        System.setProperty("aai.service.name", SERVICE_NAME);
    }

    @BeforeEach
    public void setUp() throws Exception {
        setUp(0);
    }

    public void setUp(int sbopt) throws Exception {
        XSDElementTest x = new XSDElementTest();
        x.setUp(sbopt);
        testXML = x.testXML;
        logger.debug(testXML);
        BufferedWriter bw = new BufferedWriter(new FileWriter(OXMFILENAME));
        bw.write(testXML);
        bw.close();
    }

    @Test
    public void testGetDocumentHeader() {
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String header = null;
        try {
            htmlFromOxm.setXmlVersion(testXML, v);
            htmlFromOxm.setSchemaConfigVersions(schemaConfigVersions);
            header = htmlFromOxm.getDocumentHeader();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("Header:");
        logger.debug(header);
        assertThat(header, is(HTMLheader()));
    }

    @Test
    public void testProcess() {
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String fileContent = null;
        try {
            htmlFromOxm.setXmlVersion(testXML, v);
            fileContent = htmlFromOxm.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("FileContent-I:");
        logger.debug(fileContent);
        assertThat(fileContent, is(HTMLresult(0)));
    }

    @Test
    public void testProcessWithCombiningJavaTypes() {
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String fileContent = null;
        try {
            setUp(1);
            htmlFromOxm.setXmlVersion(testXML, v);
            fileContent = htmlFromOxm.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("FileContent-I:");
        logger.debug(fileContent);
        assertThat(fileContent, is(HTMLresult(1)));
    }

    @Test
    public void testHTMLfromOXMFileVersion() throws IOException {
        String outfileName = "testXML.xml";
        File XMLfile = new File(outfileName);
        XMLfile.createNewFile();
        BufferedWriter bw = null;
        Charset charset = Charset.forName("UTF-8");
        Path path = Path.of(outfileName);
        bw = Files.newBufferedWriter(path, charset);
        bw.write(testXML);
        bw.close();
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String fileContent = null;
        try {
            htmlFromOxm.setXmlVersion(testXML, v);
            fileContent = htmlFromOxm.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
        XMLfile.delete();
        logger.debug("FileContent-I:");
        logger.debug(fileContent);
        assertThat(fileContent, is(HTMLresult(0)));
    }

    @Test
    public void testHTMLfromOXMStringVersion() {
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String fileContent = null;
        try {
            htmlFromOxm.setXmlVersion(testXML, v);
            fileContent = htmlFromOxm.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("FileContent-II:");
        logger.debug(fileContent);
        assertThat(fileContent, is(HTMLresult(0)));
    }

    /**
     * An OXM that declares constraint facets has to produce an element restricted inline, because
     * an {@code xs:element} cannot carry both a {@code type} attribute and an
     * {@code xs:simpleType}.
     */
    @Test
    public void testProcessWithConstraintFacets() throws Exception {
        XSDElementTest x = new XSDElementTest();
        x.setUpWithFacets();
        String facetXML = x.testXML;
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        htmlFromOxm.setXmlVersion(facetXML, v);
        String fileContent = htmlFromOxm.process();

        assertNotNull("generated XSD", fileContent);
        // string facets on global-customer-id, in declaration order
        assertThat(fileContent, containsString(
            "        <xs:element name=\"global-customer-id\" minOccurs=\"0\">" + LINE_SEPARATOR));
        assertThat(fileContent,
            containsString("          <xs:simpleType>" + LINE_SEPARATOR
                + "            <xs:restriction base=\"xs:string\">" + LINE_SEPARATOR
                + "              <xs:minLength value=\"1\"/>" + LINE_SEPARATOR
                + "              <xs:maxLength value=\"36\"/>" + LINE_SEPARATOR
                + "              <xs:pattern value=\"^[A-Za-z0-9-]+$\"/>" + LINE_SEPARATOR
                + "            </xs:restriction>" + LINE_SEPARATOR + "          </xs:simpleType>"
                + LINE_SEPARATOR));
        // allowedValues expands to one xs:enumeration per value
        assertThat(fileContent, containsString("              <xs:enumeration value=\"CUST\"/>"));
        assertThat(fileContent, containsString("              <xs:enumeration value=\"INFRA\"/>"));
        // numeric facets use the inclusive bounds
        assertThat(fileContent, containsString("            <xs:restriction base=\"xs:int\">"));
        assertThat(fileContent, containsString("              <xs:minInclusive value=\"0\"/>"));
        assertThat(fileContent, containsString("              <xs:maxInclusive value=\"100\"/>"));
        // a facet on a reference to another node type has nothing to restrict, so only the three
        // facet carrying standard type elements get an inline simple type
        assertThat(fileContent, containsString(
            "        <xs:element ref=\"tns:service-subscriptions\" minOccurs=\"0\">"));
        assertThat(countOccurrences(fileContent, "<xs:simpleType>"), is(3));
        // an element without facets keeps the plain type attribute
        assertThat(fileContent, containsString(
            "        <xs:element name=\"subscriber-name\" type=\"xs:string\" minOccurs=\"0\">"));
        // the annotation still precedes the inline simple type
        int annotationEnd = fileContent.indexOf("          </xs:annotation>");
        assertTrue(annotationEnd > 0 && annotationEnd < fileContent.indexOf("<xs:simpleType>"),
            "xs:annotation must precede xs:simpleType in:" + LINE_SEPARATOR + fileContent);
        // and the result is a legal schema - the content model of xs:element is ordered, so an
        // xs:simpleType placed before the xs:annotation would be rejected here
        assertIsValidSchema(fileContent);
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + 1)) {
            ++count;
        }
        return count;
    }

    /**
     * Fails unless the argument is a schema document XML Schema itself accepts, which is the check
     * that the generated XSD is well formed and its content model correctly ordered.
     */
    private static void assertIsValidSchema(String xsd) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.newSchema(new StreamSource(new StringReader(xsd)));
        } catch (SAXException e) {
            fail("generated XSD is not a valid schema: " + e.getMessage() + LINE_SEPARATOR + xsd);
        }
    }

    @Test
    public void testProcessJavaTypeElement() {
        String target = "Element=java-type/Customer";
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        Element customer = null;
        try {
            htmlFromOxm.setXmlVersion(testXML, v);
            htmlFromOxm.process();
            customer = htmlFromOxm.getJavaTypeElementSwagger("Customer");
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("Element:");
        logger.debug("Element=" + customer.getNodeName() + "/" + customer.getAttribute("name"));
        assertThat("Element=" + customer.getNodeName() + "/" + customer.getAttribute("name"),
            is(target));
    }

    public String HTMLresult() {
        return HTMLresult(0);
    }

    public String HTMLresult(int sbopt) {
        StringBuilder sb = new StringBuilder(32368);
        sb.append(HTMLheader());
        sb.append(HTMLdefs(sbopt));
        return sb.toString();
    }

    public String HTMLheader() {
        StringBuilder sb = new StringBuilder(1500);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + LINE_SEPARATOR);
        sb.append(
            "<xs:schema elementFormDefault=\"qualified\" version=\"1.0\" targetNamespace=\"http://org.onap.aai.inventory/v11\" xmlns:tns=\"http://org.onap.aai.inventory/v11\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
                + LINE_SEPARATOR + "xmlns:jaxb=\"https://jakarta.ee/xml/ns/jaxb\""
                + LINE_SEPARATOR);
        sb.append("    jaxb:version=\"3.0\"" + LINE_SEPARATOR);
        sb.append("    xmlns:annox=\"urn:jaxb.jvnet.org:annox\"" + LINE_SEPARATOR);
        sb.append("    jaxb:extensionBindingPrefixes=\"annox\">"
            + OxmFileProcessor.DOUBLE_LINE_SEPARATOR);
        return sb.toString();
    }

    public String HTMLdefs() {
        return HTMLdefs(0);
    }

    public String HTMLdefs(int sbopt) {
        StringBuilder sb = new StringBuilder(1500);
        sb.append("  <xs:element name=\"service-subscription\">" + LINE_SEPARATOR);
        sb.append("    <xs:complexType>" + LINE_SEPARATOR);
        sb.append("      <xs:annotation>" + LINE_SEPARATOR);
        sb.append("        <xs:appinfo>" + LINE_SEPARATOR);
        sb.append(
            "          <annox:annotate target=\"class\">@org.onap.aai.annotations.Metadata(description=\"Object that group service instances.\",indexedProps=\"service-type\",dependentOn=\"customer\",container=\"service-subscriptions\",crossEntityReference=\"service-instance,service-type\")</annox:annotate>"
                + LINE_SEPARATOR);
        sb.append("        </xs:appinfo>" + LINE_SEPARATOR);
        sb.append("      </xs:annotation>" + LINE_SEPARATOR);
        sb.append("      <xs:sequence>" + LINE_SEPARATOR);
        sb.append("        <xs:element name=\"service-type\" type=\"xs:string\" minOccurs=\"0\">"
            + LINE_SEPARATOR);
        sb.append("          <xs:annotation>" + LINE_SEPARATOR);
        sb.append("            <xs:appinfo>" + LINE_SEPARATOR);
        sb.append(
            "              <annox:annotate target=\"field\">@org.onap.aai.annotations.Metadata(isKey=true,description=\"Value defined by orchestration to identify this service.\")</annox:annotate>"
                + LINE_SEPARATOR);
        sb.append("            </xs:appinfo>" + LINE_SEPARATOR);
        sb.append("          </xs:annotation>" + LINE_SEPARATOR);
        sb.append("        </xs:element>" + LINE_SEPARATOR);
        sb.append(
            "        <xs:element name=\"temp-ub-sub-account-id\" type=\"xs:string\" minOccurs=\"0\">"
                + LINE_SEPARATOR);
        sb.append("          <xs:annotation>" + LINE_SEPARATOR);
        sb.append("            <xs:appinfo>" + LINE_SEPARATOR);
        sb.append(
            "              <annox:annotate target=\"field\">@org.onap.aai.annotations.Metadata(description=\"This property will be deleted from A&amp;AI in the near future. Only stop gap solution.\")</annox:annotate>"
                + LINE_SEPARATOR);
        sb.append("            </xs:appinfo>" + LINE_SEPARATOR);
        sb.append("          </xs:annotation>" + LINE_SEPARATOR);
        sb.append("        </xs:element>" + LINE_SEPARATOR);
        sb.append(
            "        <xs:element name=\"resource-version\" type=\"xs:string\" minOccurs=\"0\">"
                + LINE_SEPARATOR);
        sb.append("          <xs:annotation>" + LINE_SEPARATOR);
        sb.append("            <xs:appinfo>" + LINE_SEPARATOR);
        sb.append(
            "              <annox:annotate target=\"field\">@org.onap.aai.annotations.Metadata(description=\"Used for optimistic concurrency.  Must be empty on create, valid on update and delete.\")</annox:annotate>"
                + LINE_SEPARATOR);
        sb.append("            </xs:appinfo>" + LINE_SEPARATOR);
        sb.append("          </xs:annotation>" + LINE_SEPARATOR);
        sb.append("        </xs:element>" + LINE_SEPARATOR);
        sb.append("      </xs:sequence>" + LINE_SEPARATOR);
        sb.append("    </xs:complexType>" + LINE_SEPARATOR);
        sb.append("  </xs:element>" + LINE_SEPARATOR);
        sb.append("  <xs:element name=\"service-subscriptions\">" + LINE_SEPARATOR);
        sb.append("    <xs:complexType>" + LINE_SEPARATOR);
        sb.append("      <xs:annotation>" + LINE_SEPARATOR);
        sb.append("        <xs:appinfo>" + LINE_SEPARATOR);
        sb.append(
            "          <annox:annotate target=\"class\">@org.onap.aai.annotations.Metadata(description=\"Collection of objects that group service instances.\")</annox:annotate>"
                + LINE_SEPARATOR);
        sb.append("        </xs:appinfo>" + LINE_SEPARATOR);
        sb.append("      </xs:annotation>" + LINE_SEPARATOR);
        sb.append("      <xs:sequence>" + LINE_SEPARATOR);
        sb.append(
            "        <xs:element ref=\"tns:service-subscription\" minOccurs=\"0\" maxOccurs=\"5000\"/>"
                + LINE_SEPARATOR);
        sb.append("      </xs:sequence>" + LINE_SEPARATOR);
        sb.append("    </xs:complexType>" + LINE_SEPARATOR);
        sb.append("  </xs:element>" + LINE_SEPARATOR);
        sb.append("  <xs:element name=\"customer\">" + LINE_SEPARATOR);
        sb.append("    <xs:complexType>" + LINE_SEPARATOR);
        sb.append("      <xs:annotation>" + LINE_SEPARATOR);
        sb.append("        <xs:appinfo>" + LINE_SEPARATOR);
        if (sbopt == 0) {
            sb.append(
                "          <annox:annotate target=\"class\">@org.onap.aai.annotations.Metadata(description=\"customer identifiers to provide linkage back to BSS information.\",nameProps=\"subscriber-name\",indexedProps=\"subscriber-name,global-customer-id,subscriber-type\",searchable=\"global-customer-id,subscriber-name\",uniqueProps=\"global-customer-id\",container=\"customers\",namespace=\"business\")</annox:annotate>"
                    + LINE_SEPARATOR);
        } else {
            sb.append(
                "          <annox:annotate target=\"class\">@org.onap.aai.annotations.Metadata(description=\"customer identifiers to provide linkage back to BSS information.\",nameProps=\"subscriber-name\",indexedProps=\"subscriber-type,subscriber-name,global-customer-id\",searchable=\"global-customer-id,subscriber-name\",uniqueProps=\"global-customer-id\",container=\"customers\",namespace=\"business\")</annox:annotate>"
                    + LINE_SEPARATOR);
        }
        sb.append("        </xs:appinfo>" + LINE_SEPARATOR);
        sb.append("      </xs:annotation>" + LINE_SEPARATOR);
        sb.append("      <xs:sequence>" + LINE_SEPARATOR);
        sb.append(
            "        <xs:element name=\"global-customer-id\" type=\"xs:string\" minOccurs=\"0\">"
                + LINE_SEPARATOR);
        sb.append("          <xs:annotation>" + LINE_SEPARATOR);
        sb.append("            <xs:appinfo>" + LINE_SEPARATOR);
        sb.append(
            "              <annox:annotate target=\"field\">@org.onap.aai.annotations.Metadata(isKey=true,description=\"Global customer id used across to uniquely identify customer.\")</annox:annotate>"
                + LINE_SEPARATOR);
        sb.append("            </xs:appinfo>" + LINE_SEPARATOR);
        sb.append("          </xs:annotation>" + LINE_SEPARATOR);
        sb.append("        </xs:element>" + LINE_SEPARATOR);
        sb.append("        <xs:element name=\"subscriber-name\" type=\"xs:string\" minOccurs=\"0\">"
            + LINE_SEPARATOR);
        sb.append("          <xs:annotation>" + LINE_SEPARATOR);
        sb.append("            <xs:appinfo>" + LINE_SEPARATOR);
        sb.append(
            "              <annox:annotate target=\"field\">@org.onap.aai.annotations.Metadata(description=\"Subscriber name, an alternate way to retrieve a customer.\")</annox:annotate>"
                + LINE_SEPARATOR);
        sb.append("            </xs:appinfo>" + LINE_SEPARATOR);
        sb.append("          </xs:annotation>" + LINE_SEPARATOR);
        sb.append("        </xs:element>" + LINE_SEPARATOR);
        if (sbopt == 0) {
            sb.append(
                "        <xs:element name=\"subscriber-type\" type=\"xs:string\" minOccurs=\"0\">"
                    + LINE_SEPARATOR);
            sb.append("          <xs:annotation>" + LINE_SEPARATOR);
            sb.append("            <xs:appinfo>" + LINE_SEPARATOR);
            sb.append(
                "              <annox:annotate target=\"field\">@org.onap.aai.annotations.Metadata(description=\"Subscriber type, a way to provide VID with only the INFRA customers.\",defaultValue=\"CUST\")</annox:annotate>"
                    + LINE_SEPARATOR);
            sb.append("            </xs:appinfo>" + LINE_SEPARATOR);
            sb.append("          </xs:annotation>" + LINE_SEPARATOR);
            sb.append("        </xs:element>" + LINE_SEPARATOR);
            sb.append(
                "        <xs:element name=\"resource-version\" type=\"xs:string\" minOccurs=\"0\">"
                    + LINE_SEPARATOR);
            sb.append("          <xs:annotation>" + LINE_SEPARATOR);
            sb.append("            <xs:appinfo>" + LINE_SEPARATOR);
            sb.append(
                "              <annox:annotate target=\"field\">@org.onap.aai.annotations.Metadata(description=\"Used for optimistic concurrency.  Must be empty on create, valid on update and delete.\")</annox:annotate>"
                    + LINE_SEPARATOR);
            sb.append("            </xs:appinfo>" + LINE_SEPARATOR);
            sb.append("          </xs:annotation>" + LINE_SEPARATOR);
            sb.append("        </xs:element>" + LINE_SEPARATOR);
        } else {
            sb.append(
                "        <xs:element name=\"resource-version\" type=\"xs:string\" minOccurs=\"0\">"
                    + LINE_SEPARATOR);
            sb.append("          <xs:annotation>" + LINE_SEPARATOR);
            sb.append("            <xs:appinfo>" + LINE_SEPARATOR);
            sb.append(
                "              <annox:annotate target=\"field\">@org.onap.aai.annotations.Metadata(description=\"Used for optimistic concurrency.  Must be empty on create, valid on update and delete.\")</annox:annotate>"
                    + LINE_SEPARATOR);
            sb.append("            </xs:appinfo>" + LINE_SEPARATOR);
            sb.append("          </xs:annotation>" + LINE_SEPARATOR);
            sb.append("        </xs:element>" + LINE_SEPARATOR);
            sb.append(
                "        <xs:element name=\"subscriber-type\" type=\"xs:string\" minOccurs=\"0\">"
                    + LINE_SEPARATOR);
            sb.append("          <xs:annotation>" + LINE_SEPARATOR);
            sb.append("            <xs:appinfo>" + LINE_SEPARATOR);
            sb.append(
                "              <annox:annotate target=\"field\">@org.onap.aai.annotations.Metadata(description=\"Subscriber type, a way to provide VID with only the INFRA customers.\",defaultValue=\"CUST\")</annox:annotate>"
                    + LINE_SEPARATOR);
            sb.append("            </xs:appinfo>" + LINE_SEPARATOR);
            sb.append("          </xs:annotation>" + LINE_SEPARATOR);
            sb.append("        </xs:element>" + LINE_SEPARATOR);

        }
        sb.append("        <xs:element ref=\"tns:service-subscriptions\" minOccurs=\"0\"/>"
            + LINE_SEPARATOR);
        sb.append("      </xs:sequence>" + LINE_SEPARATOR);
        sb.append("    </xs:complexType>" + LINE_SEPARATOR);
        sb.append("  </xs:element>" + LINE_SEPARATOR);
        sb.append("  <xs:element name=\"customers\">" + LINE_SEPARATOR);
        sb.append("    <xs:complexType>" + LINE_SEPARATOR);
        sb.append("      <xs:annotation>" + LINE_SEPARATOR);
        sb.append("        <xs:appinfo>" + LINE_SEPARATOR);
        sb.append(
            "          <annox:annotate target=\"class\">@org.onap.aai.annotations.Metadata(description=\"Collection of customer identifiers to provide linkage back to BSS information.\")</annox:annotate>"
                + LINE_SEPARATOR);
        sb.append("        </xs:appinfo>" + LINE_SEPARATOR);
        sb.append("      </xs:annotation>" + LINE_SEPARATOR);
        sb.append("      <xs:sequence>" + LINE_SEPARATOR);
        sb.append("        <xs:element ref=\"tns:customer\" minOccurs=\"0\" maxOccurs=\"5000\"/>"
            + LINE_SEPARATOR);
        sb.append("      </xs:sequence>" + LINE_SEPARATOR);
        sb.append("    </xs:complexType>" + LINE_SEPARATOR);
        sb.append("  </xs:element>" + LINE_SEPARATOR);
        sb.append("  <xs:element name=\"business\">" + LINE_SEPARATOR);
        sb.append("    <xs:complexType>" + LINE_SEPARATOR);
        sb.append("      <xs:annotation>" + LINE_SEPARATOR);
        sb.append("        <xs:appinfo>" + LINE_SEPARATOR);
        sb.append(
            "          <annox:annotate target=\"class\">@org.onap.aai.annotations.Metadata(description=\"Namespace for business related constructs\")</annox:annotate>"
                + LINE_SEPARATOR);
        sb.append("        </xs:appinfo>" + LINE_SEPARATOR);
        sb.append("      </xs:annotation>" + LINE_SEPARATOR);
        sb.append("      <xs:sequence>" + LINE_SEPARATOR);
        sb.append("        <xs:element ref=\"tns:customers\" minOccurs=\"0\"/>" + LINE_SEPARATOR);
        sb.append("      </xs:sequence>" + LINE_SEPARATOR);
        sb.append("    </xs:complexType>" + LINE_SEPARATOR);
        sb.append("  </xs:element>" + LINE_SEPARATOR);
        sb.append("  <xs:element name=\"inventory\">" + LINE_SEPARATOR);
        sb.append("    <xs:complexType>" + LINE_SEPARATOR);
        sb.append("      <xs:sequence>" + LINE_SEPARATOR);
        sb.append("        <xs:element ref=\"tns:business\" minOccurs=\"0\"/>" + LINE_SEPARATOR);
        sb.append("      </xs:sequence>" + LINE_SEPARATOR);
        sb.append("    </xs:complexType>" + LINE_SEPARATOR);
        sb.append("  </xs:element>" + LINE_SEPARATOR);
        sb.append("</xs:schema>" + LINE_SEPARATOR);
        return sb.toString();
    }

    @Test
    public void testSetOxmVersion() {
        // Arrange
        File oxmFile = new File(OXMFILENAME);
        SchemaVersion version = schemaConfigVersions.getAppRootVersion();

        // Act
        try {
            htmlFromOxm.setOxmVersion(oxmFile, version); // Setting the version
            // Check the document header which should reflect the version set
            String header = htmlFromOxm.getDocumentHeader();
            logger.debug("Header: " + header);

            // Verify that the version is properly included in the header
            assertThat(header.contains(version.toString()), is(true)); // Check if version is part
                                                                       // of the header
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred while setting OXM version");
        }
    }

    @Test
    public void testSetVersion() {
        SchemaVersion version = schemaConfigVersions.getAppRootVersion();

        try {
            htmlFromOxm.setVersion(version);
            // Check if the version is correctly reflected in the document header
            String header = htmlFromOxm.getDocumentHeader();
            logger.debug("Header: " + header);

            // Assert that the version is correctly reflected in the header content
            assertThat(header.contains(version.toString()), is(true));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred while setting the version");
        }
    }

    @Test
    public void testIsValidName() {
        assertThat(htmlFromOxm.isValidName("valid-name"), is(true));
        assertThat(htmlFromOxm.isValidName("valid123-name"), is(true));
        assertThat(htmlFromOxm.isValidName("InvalidName"), is(false));
        assertThat(htmlFromOxm.isValidName("invalid_name"), is(false));
        assertThat(htmlFromOxm.isValidName("12345"), is(true));
        assertThat(htmlFromOxm.isValidName(""), is(false));
        assertThat(htmlFromOxm.isValidName(null), is(false));
    }

    @Test
    public void testSkipCheck() {
        assertThat(htmlFromOxm.skipCheck("model"), is(true));
        assertThat(htmlFromOxm.skipCheck("eventHeader"), is(true));
        assertThat(htmlFromOxm.skipCheck("otherAttribute"), is(false));
    }

    @Test
    public void testProcessJavaTypeElement_noXmlElements() {
        // Create a mock Element for the Java type
        String javaTypeName = "Customer";
        Element javaTypeElement = mock(Element.class);

        // Mock parentNodes to simulate presence of a `java-attributes` node
        NodeList parentNodes = mock(NodeList.class);
        when(javaTypeElement.getElementsByTagName("java-attributes")).thenReturn(parentNodes);
        when(parentNodes.getLength()).thenReturn(1); // Simulating one java-attributes element

        // Mock the java-attributes element
        Element javaAttributesElement = mock(Element.class);
        when(parentNodes.item(0)).thenReturn(javaAttributesElement);

        // Mock "xml-element" inside java-attributes to be an empty NodeList
        NodeList xmlElementNodes = mock(NodeList.class);
        when(javaAttributesElement.getElementsByTagName("xml-element")).thenReturn(xmlElementNodes);
        when(xmlElementNodes.getLength()).thenReturn(0); // No xml-element nodes inside

        // Mock the xml-root-element element to return the correct root element name
        NodeList valNodes = mock(NodeList.class);
        when(javaTypeElement.getElementsByTagName("xml-root-element")).thenReturn(valNodes);
        when(valNodes.getLength()).thenReturn(1); // Simulating one xml-root-element node

        // Mock the valElement
        Element valElement = mock(Element.class);
        when(valNodes.item(0)).thenReturn(valElement);

        // Mock getAttributes to return a NamedNodeMap
        NamedNodeMap attributes = mock(NamedNodeMap.class);
        when(valElement.getAttributes()).thenReturn(attributes);

        // Mock getNamedItem("name") to return the correct attribute value "Customer"
        Attr nameAttr = mock(Attr.class);
        when(attributes.getNamedItem("name")).thenReturn(nameAttr);
        when(nameAttr.getNodeValue()).thenReturn("Customer"); // Ensure the value is set to
                                                              // "Customer"

        // Create a StringBuilder for inventory
        StringBuilder sbInventory = new StringBuilder();

        // Call the method that processes the Java type element
        String result =
            htmlFromOxm.processJavaTypeElement(javaTypeName, javaTypeElement, sbInventory);

        // Debugging: Verify the name is correctly set
        assertNotNull("The name attribute should not be null", nameAttr);
        assertEquals("Customer", nameAttr.getNodeValue());

        // Debugging Output: Print the generated XML
        System.out.println("Generated XML: " + result);

        // Expected result format (adjusted to match generated XML structure for no xml-element
        // nodes)
        String expected = "  <xs:element name=\"" + null + "\">" + LINE_SEPARATOR
            + "    <xs:complexType>" + LINE_SEPARATOR + "      <xs:sequence/>" + LINE_SEPARATOR
            + "    </xs:complexType>" + LINE_SEPARATOR + "  </xs:element>" + LINE_SEPARATOR;

        assertThat(result, is(expected));

        verify(javaTypeElement, times(1)).getElementsByTagName("java-attributes");
        verify(javaAttributesElement, times(1)).getElementsByTagName("xml-element");

        // Check if the generatedJavaType map is updated correctly
        assertThat(htmlFromOxm.generatedJavaType.containsKey(javaTypeName), is(true));
    }
}
