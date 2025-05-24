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
package org.onap.aai.schemagen;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.schemagen.genxsd.*;
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
import org.xml.sax.SAXException;
import lombok.SneakyThrows;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.xml.parsers.ParserConfigurationException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(
    classes = {SchemaLocationsBean.class, TestUtilConfigTranslatorforBusiness.class,
        EdgeIngestor.class, NodeIngestor.class, SwaggerGenerationConfiguration.class,
        SchemaConfigVersions.class})
@TestPropertySource(properties = {"schema.uri.base.path = /aai", "schema.xsd.maxoccurs = 5000"})
public class GenerateXsdTest {

    private static final Logger logger = LoggerFactory.getLogger("GenerateXsd.class");
    private static final String OXMFILENAME = "src/test/resources/oxm/business_oxm_v11.xml";
    private static final String EDGEFILENAME =
        "src/test/resources/dbedgerules/DbEdgeBusinessRules_test.json";
    public static AnnotationConfigApplicationContext ctx = null;
    private static String testXML;

    @Autowired
    YAMLfromOXM yamlFromOxm;

    @Autowired
    HTMLfromOXM htmlFromOxm;

    @Autowired
    SchemaConfigVersions schemaConfigVersions;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        XSDElementTest x = new XSDElementTest();
        x.setUp();
        testXML = x.getTestXML();
        logger.debug(testXML);
        // Write test XML to file (OXM)
        BufferedWriter bw = new BufferedWriter(new FileWriter(OXMFILENAME));
        bw.write(testXML);
        bw.close();
        // Write Edge Rules to file
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(EDGEFILENAME));
        bw1.write(YAMLfromOXMTest.EdgeDefs());
        bw1.close();
    }

    @BeforeEach
    public void setUp() throws Exception {
        XSDElementTest x = new XSDElementTest();
        x.setUp();
        testXML = x.getTestXML();
    }

    @Test
    @SneakyThrows
    public void test_generateSwaggerFromOxmFile() {
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String apiVersion = v.toString();

        yamlFromOxm.setXmlVersion(testXML, v);
        String fileContent = yamlFromOxm.process();

        assertThat(fileContent, is(new YAMLfromOXMTest().YAMLresult()));
    }

    @Test
    @SneakyThrows
    public void test_generateXSDFromOxmFile() {
        SchemaVersion v = schemaConfigVersions.getAppRootVersion();

        htmlFromOxm.setXmlVersion(testXML, v);
        String fileContent = htmlFromOxm.process();

        assertThat(fileContent, is(new HTMLfromOXMTest().HTMLresult()));
    }

    @Test
    public void testGetAPIVersion() {
        GenerateXsd.apiVersion = schemaConfigVersions.getAppRootVersion().toString();
        assertThat(GenerateXsd.getAPIVersion(), is("v11"));
    }

    @Test
    public void testGetYamlDir() {
        assertThat(GenerateXsd.getYamlDir(),
            is("aai-schema/src/main/resources/onap/aai_swagger_yaml"));
    }

    @Test
    public void testValidVersionWithAll() throws Exception {
        // Access the private method using reflection
        Method validVersionMethod = GenerateXsd.class.getDeclaredMethod("validVersion", String.class);
        validVersionMethod.setAccessible(true);

        // Test "ALL"
        boolean result = (boolean) validVersionMethod.invoke(null, "ALL");
        assertTrue(result, "\"ALL\" should be considered a valid version.");
    }

    // Test for versionSupportsSwagger method using Reflection
    @Test
    public void testVersionSupportsSwaggerWithValidVersion() throws Exception {
        // Accessing the private method using reflection
        Method versionSupportsSwaggerMethod = GenerateXsd.class.getDeclaredMethod("versionSupportsSwagger", String.class);
        versionSupportsSwaggerMethod.setAccessible(true);

        // Test version >= v1
        boolean result = (boolean) versionSupportsSwaggerMethod.invoke(null, "v1");
        assertTrue(result, "\"v1\" should be supported for Swagger.");

        result = (boolean) versionSupportsSwaggerMethod.invoke(null, "v5");
        assertTrue(result, "\"v5\" should be supported for Swagger.");
    }

    @Test
    public void testVersionSupportsSwaggerWithInvalidVersion() throws Exception {
        // Accessing the private method using reflection
        Method versionSupportsSwaggerMethod = GenerateXsd.class.getDeclaredMethod("versionSupportsSwagger", String.class);
        versionSupportsSwaggerMethod.setAccessible(true);

        // Test version < v1
        boolean result = (boolean) versionSupportsSwaggerMethod.invoke(null, "v0");
        assertFalse(result, "\"v0\" should not be supported for Swagger.");
    }

    @Test
    public void mainMethod() throws IOException {
        System.setProperty("gen_version","v12");
        System.setProperty("gen_type","xsd");
        GenerateXsd.main(new String[]{});
    }

}
