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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Multimap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.EdgeRule;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.schemagen.testutils.TestUtilConfigTranslatorforEdges;
import org.onap.aai.setup.ConfigTranslator;
import org.onap.aai.setup.SchemaConfigVersions;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.SchemaVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(
    classes = {SchemaConfigVersions.class, SchemaLocationsBean.class,
        TestUtilConfigTranslatorforEdges.class, EdgeIngestor.class})
@TestPropertySource(
    properties = {
        "schemaIngestPropLoc = src/test/resources/schemaIngest/schemaIngestTest.properties"})

public class EdgeDescriptionTest {
    private static final String EDGEFILENAME =
        "src/test/resources/dbedgerules/EdgeDescriptionRules_test.json";
    @Autowired
    ConfigTranslator ct;
    @Autowired
    EdgeIngestor edgeIngestor;
    String nodeName = "availability-zone";
    String toNode = "complex";
    SchemaVersion v10 = new SchemaVersion("v10");

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        String json = "{" + "  \"rules\": [" + "    {" + "      \"from\": \"availability-zone\","
            + "      \"to\": \"complex\","
            + "      \"label\": \"org.onap.relationships.inventory.LocatedIn\","
            + "      \"direction\": \"OUT\"," + "      \"multiplicity\": \"MANY2ONE\","
            + "      \"contains-other-v\": \"NONE\"," + "      \"delete-other-v\": \"NONE\","
            + "      \"SVC-INFRA\": \"NONE\"," + "      \"prevent-delete\": \"!${direction}\","
            + "      \"default\": \"true\"," + "      \"description\":\"this description\""
            + "    }," + "    {" + "      \"from\": \"availability-zone\","
            + "      \"to\": \"service-capability\","
            + "      \"label\": \"org.onap.relationships.inventory.AppliesTo\","
            + "      \"direction\": \"OUT\"," + "      \"multiplicity\": \"MANY2MANY\","
            + "      \"contains-other-v\": \"NONE\"," + "      \"delete-other-v\": \"NONE\","
            + "      \"SVC-INFRA\": \"NONE\"," + "      \"prevent-delete\": \"!${direction}\","
            + "      \"default\": \"true\"," + "      \"description\":\"\"" + "    }," + "    {"
            + "      \"from\": \"availability-zone\"," + "      \"to\": \"cloud-region\","
            + "      \"label\": \"org.onap.relationships.inventory.BelongsTo\","
            + "      \"direction\": \"OUT\"," + "      \"multiplicity\": \"MANY2ONE\","
            + "      \"contains-other-v\": \"!${direction}\","
            + "      \"delete-other-v\": \"!${direction}\"," + "      \"SVC-INFRA\": \"NONE\","
            + "      \"prevent-delete\": \"NONE\"," + "      \"default\": \"true\","
            + "      \"description\":\"\"" + "    }," + "    {" + "      \"from\": \"ctag-pool\","
            + "      \"to\": \"availability-zone\","
            + "      \"label\": \"org.onap.relationships.inventory.AppliesTo\","
            + "      \"direction\": \"OUT\"," + "      \"multiplicity\": \"MANY2MANY\","
            + "      \"contains-other-v\": \"${direction}\","
            + "      \"delete-other-v\": \"NONE\"," + "      \"SVC-INFRA\": \"NONE\","
            + "      \"prevent-delete\": \"!${direction}\"," + "      \"default\": \"true\","
            + "      \"description\":\"\"" + "    }," + "    {" + "      \"from\": \"dvs-switch\","
            + "      \"to\": \"availability-zone\","
            + "      \"label\": \"org.onap.relationships.inventory.AppliesTo\","
            + "      \"direction\": \"OUT\"," + "      \"multiplicity\": \"MANY2MANY\","
            + "      \"contains-other-v\": \"NONE\"," + "      \"delete-other-v\": \"NONE\","
            + "      \"SVC-INFRA\": \"NONE\"," + "      \"prevent-delete\": \"!${direction}\","
            + "      \"default\": \"true\"," + "      \"description\":\"\"" + "    }," + "    {"
            + "      \"from\": \"generic-vnf\"," + "      \"to\": \"availability-zone\","
            + "      \"label\": \"org.onap.relationships.inventory.Uses\","
            + "      \"direction\": \"OUT\"," + "      \"multiplicity\": \"MANY2MANY\","
            + "      \"contains-other-v\": \"NONE\"," + "      \"delete-other-v\": \"NONE\","
            + "      \"SVC-INFRA\": \"${direction}\","
            + "      \"prevent-delete\": \"!${direction}\"," + "      \"default\": \"true\","
            + "      \"description\":\"\"" + "    }," + "    {" + "      \"from\": \"pserver\","
            + "      \"to\": \"availability-zone\","
            + "      \"label\": \"org.onap.relationships.inventory.MemberOf\","
            + "      \"direction\": \"OUT\"," + "      \"multiplicity\": \"MANY2ONE\","
            + "      \"contains-other-v\": \"NONE\"," + "      \"delete-other-v\": \"NONE\","
            + "      \"SVC-INFRA\": \"${direction}\","
            + "      \"prevent-delete\": \"!${direction}\"," + "      \"default\": \"true\","
            + "      \"description\":\"\"" + "    }," + "    {" + "      \"from\": \"vce\","
            + "      \"to\": \"availability-zone\","
            + "      \"label\": \"org.onap.relationships.inventory.Uses\","
            + "      \"direction\": \"OUT\"," + "      \"multiplicity\": \"MANY2MANY\","
            + "      \"contains-other-v\": \"NONE\"," + "      \"delete-other-v\": \"NONE\","
            + "      \"SVC-INFRA\": \"NONE\"," + "      \"prevent-delete\": \"!${direction}\","
            + "      \"default\": \"true\"," + "      \"description\":\"\"" + "    }," + "  ]}";
        BufferedWriter bw = new BufferedWriter(new FileWriter(EDGEFILENAME));
        bw.write(json);
        bw.close();
    }

    @AfterAll
    public static void tearDownAfterClass() throws Exception {
        File edges = new File(EDGEFILENAME);
        edges.delete();
    }

    @BeforeEach
    public void setUp() throws Exception {

    }

    @Test
    public void test() {

        Map<SchemaVersion, List<String>> edges = ct.getEdgeFiles();
        assertTrue(edges.containsKey(v10));
        assertTrue(1 == edges.get(v10).size());
        assertTrue("src/test/resources/dbedgerules/DbEdgeBusinessRules_test.json"
            .equals(edges.get(v10).get(0)));
    }

    @Test
    public void testGetDeleteOtherV() throws EdgeRuleNotFoundException {
        String target = "availability-zone" + "|" + toNode + "-" + "NONE";
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals(toNode) && (!i.isPrivateEdge()))).forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    assertThat(ed.getRuleKey() + "-" + ed.getDeleteOtherV(), is(target));
                });
        }
    }

    @Test
    public void testGetPreventDelete() throws EdgeRuleNotFoundException {
        String target = "availability-zone" + "|" + toNode + "-" + "IN";
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals(toNode) && (!i.isPrivateEdge()))).forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    assertThat(ed.getRuleKey() + "-" + ed.getPreventDelete(), is(target));
                });
        }
    }

    @Test
    public void testGetAlsoDeleteFootnote() throws EdgeRuleNotFoundException {
        // String toNode="cloud-region";
        // String target = "availability-zone"+"|"+toNode+"-"+"(4)";
        List<String> notedTypes = Arrays.asList("cloud-region", "ctag-pool");
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals("availability-zone") && (!i.isPrivateEdge())))
                .forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    String target =
                        ed.getRuleKey() + "-" + (notedTypes.contains(ed.getTo()) ? "(4)" : "");
                    assertThat(ed.getRuleKey() + "-" + ed.getAlsoDeleteFootnote(ed.getFrom()),
                        is(target));
                });
        }
        /*
         * for (EdgeDescription ed : edges) {
         * String modResult = ed.getRuleKey() + "-" + ed.getAlsoDeleteFootnote(ed.getFrom());
         * assertThat(modResult, is(target));
         * ed.setDeleteOtherV("IN");
         * target = "availability-zone"+"|"+"complex"+"-"+"(4)";
         * modResult = ed.getRuleKey() + "-" + ed.getAlsoDeleteFootnote(ed.getFrom());
         * assertThat(modResult, is(target));
         * target = "availability-zone"+"|"+"complex"+"-"+"(1)";
         * modResult = ed.getRuleKey() + "-" + ed.getAlsoDeleteFootnote(ed.getTo());
         * assertThat(modResult, is(target));
         * ed.setDeleteOtherV("OUT");
         * target = "availability-zone"+"|"+"complex"+"-"+"(2)";
         * modResult = ed.getRuleKey() + "-" + ed.getAlsoDeleteFootnote(ed.getFrom());
         * assertThat(modResult, is(target));
         * target = "availability-zone"+"|"+"complex"+"-"+"(3)";
         * modResult = ed.getRuleKey() + "-" + ed.getAlsoDeleteFootnote(ed.getTo());
         * assertThat(modResult, is(target));
         * }
         */
    }

    @Test
    public void testGetTo() throws EdgeRuleNotFoundException {
        String target = "availability-zone" + "|" + toNode + "-" + toNode;
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals(toNode) && (!i.isPrivateEdge()))).forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    assertThat(ed.getRuleKey() + "-" + ed.getTo(), is(target));
                });
        }
    }

    @Test
    public void testGetFrom() throws EdgeRuleNotFoundException {
        String target = "availability-zone" + "|" + toNode + "-" + "availability-zone";
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals(toNode) && (!i.isPrivateEdge()))).forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    assertThat(ed.getRuleKey() + "-" + ed.getFrom(), is(target));
                });
        }
    }

    @Test
    public void testGetRuleKey() throws EdgeRuleNotFoundException {
        String target = "availability-zone" + "|" + toNode;
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals(toNode) && (!i.isPrivateEdge()))).forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    assertThat(ed.getRuleKey(), is(target));
                });
        }
    }

    @Test
    public void testGetMultiplicity() throws EdgeRuleNotFoundException {
        String target = "availability-zone" + "|" + toNode + "-" + "MANY2ONE";
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals(toNode) && (!i.isPrivateEdge()))).forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    assertThat(ed.getRuleKey() + "-" + ed.getMultiplicity(), is(target));
                });
        }
    }

    @Test
    public void testGetDirection() throws EdgeRuleNotFoundException {
        String target = "availability-zone" + "|" + toNode + "-" + "OUT";
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals(toNode) && (!i.isPrivateEdge()))).forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    assertThat(ed.getRuleKey() + "-" + ed.getDirection(), is(target));
                });
        }
    }

    @Test
    public void testGetDescription() throws EdgeRuleNotFoundException {
        String target = "availability-zone" + "|" + toNode + "-" + "this description";
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals(toNode) && (!i.isPrivateEdge()))).forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    assertThat(ed.getRuleKey() + "-" + ed.getDescription(), is(target));
                });
        }
    }

    @Test
    public void testGetRelationshipDescription() throws EdgeRuleNotFoundException {
        String target = "availability-zone" + "|" + toNode + "-" + "this description";
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals(toNode) && (!i.isPrivateEdge()))).forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    assertThat(ed.getRuleKey() + "-" + ed.getDescription(), is(target));
                });
        }
    }

    @Test
    public void testGetType() throws EdgeRuleNotFoundException {
        String toNode = "cloud-region";
        String target = "availability-zone" + "|" + toNode + "-" + "PARENT";
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals(toNode) && (!i.isPrivateEdge()))).forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    assertThat(ed.getRuleKey() + "-" + ed.getType(), is(target));
                });
        }
    }

    @Test
    public void testGetLabel() throws EdgeRuleNotFoundException {
        String target =
            "availability-zone" + "|" + toNode + "-" + "org.onap.relationships.inventory.LocatedIn";
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals(toNode) && (!i.isPrivateEdge()))).forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    assertThat(ed.getRuleKey() + "-" + ed.getLabel(), is(target));
                });
        }
    }

    @Test
    public void testGetShortLabel() throws EdgeRuleNotFoundException {
        String target = "availability-zone" + "|" + toNode + "-" + "LocatedIn";
        Multimap<String, EdgeRule> results = edgeIngestor.getAllRules(v10);
        SortedSet<String> ss = new TreeSet<String>(results.keySet());
        for (String key : ss) {
            results.get(key).stream()
                .filter((i) -> (i.getTo().equals(toNode) && (!i.isPrivateEdge()))).forEach((i) -> {
                    EdgeDescription ed = new EdgeDescription(i);
                    assertThat(ed.getRuleKey() + "-" + ed.getShortLabel(), is(target));
                });
        }
    }
}
