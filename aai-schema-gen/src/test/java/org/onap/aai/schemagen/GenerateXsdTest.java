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

package org.onap.aai.schemagen;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.schemagen.genxsd.HTMLfromOXM;
import org.onap.aai.schemagen.genxsd.HTMLfromOXMTest;
import org.onap.aai.schemagen.genxsd.XSDElementTest;
import org.onap.aai.schemagen.genxsd.YAMLfromOXM;
import org.onap.aai.schemagen.genxsd.YAMLfromOXMTest;
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
        BufferedWriter bw = new BufferedWriter(new FileWriter(OXMFILENAME));
        bw.write(testXML);
        bw.close();
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(EDGEFILENAME));
        bw1.write(YAMLfromOXMTest.EdgeDefs());
        bw1.close();

    }

    @BeforeEach
    public void setUp() throws Exception {
        // PowerMockito.mockStatic(GenerateXsd.class);
        XSDElementTest x = new XSDElementTest();
        x.setUp();
        testXML = x.getTestXML();
        // logger.info(testXML);
    }

    @Test
    public void test_generateSwaggerFromOxmFile() {

        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String apiVersion = v.toString();
        String fileContent = null;
        try {

            yamlFromOxm.setXmlVersion(testXML, v);
            fileContent = yamlFromOxm.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(fileContent, is(new YAMLfromOXMTest().YAMLresult()));
    }

    @Test
    public void test_generateXSDFromOxmFile() {

        SchemaVersion v = schemaConfigVersions.getAppRootVersion();
        String fileContent = null;
        try {
            htmlFromOxm.setXmlVersion(testXML, v);
            fileContent = htmlFromOxm.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // logger.debug(fileContent);
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
    public void testGetResponsesUrl() {
        assertNull(GenerateXsd.getResponsesUrl());
    }
}
