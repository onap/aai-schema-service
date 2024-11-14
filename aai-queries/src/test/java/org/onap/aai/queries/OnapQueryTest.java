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

package org.onap.aai.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.config.IntrospectionConfig;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.exceptions.AmbiguousRuleChoiceException;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.query.builder.GremlinTraversal;
import org.onap.aai.restcore.search.GremlinGroovyShell;
import org.onap.aai.restcore.search.GroovyQueryBuilder;
import org.onap.aai.serialization.db.EdgeSerializer;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.setup.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(
    classes = {SchemaLocationsBean.class, SchemaConfigVersions.class, AAIConfigTranslator.class,
        EdgeIngestor.class, EdgeSerializer.class, NodeIngestor.class, SpringContextAware.class,
        GremlinServerSingleton.class, IntrospectionConfig.class, LoaderFactory.class})
@TestPropertySource(
    properties = {"schema.uri.base.path = /aai", "schema.source.name = onap",
        "schema.ingest.file = src/test/resources/application-test.properties"})
public abstract class OnapQueryTest {

    protected Logger logger;
    protected Graph graph;
    protected GremlinGroovyShell shell;
    @Mock
    protected TransactionalGraphEngine dbEngine;
    protected final List<Vertex> expectedResult = new ArrayList<>();

    @Autowired
    protected EdgeIngestor edgeRules;

    @Autowired
    protected EdgeSerializer rules;

    @Autowired
    protected LoaderFactory loaderFactory;

    @Autowired
    protected SchemaVersions schemaVersions;

    protected Loader loader;
    protected GraphTraversalSource gts;

    @Autowired
    protected GremlinServerSingleton gremlinServerSingleton;

    public SchemaVersion version;

    protected String query;

    LinkedHashMap<String, Object> params;

    @Autowired
    private Environment env;

    @BeforeAll
    public static void setupBundleconfig() {
        System.setProperty("AJSC_HOME", "./");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources/");
    }

    @BeforeEach
    public void setUp() throws AAIException, NoEdgeRuleFoundException, EdgeRuleNotFoundException,
        AmbiguousRuleChoiceException, IOException {
        System.setProperty("AJSC_HOME", ".");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources");
        logger = LoggerFactory.getLogger(getClass());
        MockitoAnnotations.openMocks(this);
        graph = TinkerGraph.open();
        gts = graph.traversal();
        createGraph();
        shell = new GremlinGroovyShell();

    }

    protected void setUpQuery() {
        query = gremlinServerSingleton.getStoredQueryFromConfig(getQueryName());
        params = new LinkedHashMap<>();
        addParam(params);
        when(dbEngine.getQueryBuilder(any(QueryStyle.class)))
            .thenReturn(new GremlinTraversal<>(loader, graph.traversal()));
        logger.info("Stored query in abstraction form {}", query);
        query = new GroovyQueryBuilder().executeTraversal(dbEngine, query, params);
        logger.info("After converting to gremlin query {}", query);
        query = "g" + query;
        GraphTraversal<Vertex, Vertex> g = graph.traversal().V();
        addStartNode(g);
        params.put("g", g);
    }

    public void run() {

        GraphTraversal<Vertex, Vertex> result =
            (GraphTraversal<Vertex, Vertex>) shell.executeTraversal(query, params);

        List<Vertex> vertices = result.toList();

        logger.info("Expected result set of vertexes [{}]", convert(expectedResult));
        logger.info("Actual Result set of vertexes [{}]", convert(vertices));

        List<Vertex> nonDuplicateExpectedResult = new ArrayList<>(new HashSet<>(expectedResult));
        vertices = new ArrayList<>(new HashSet<>(vertices));

        nonDuplicateExpectedResult.sort(Comparator.comparing(vertex -> vertex.id().toString()));
        vertices.sort(Comparator.comparing(vertex -> vertex.id().toString()));

        // Use this instead of the assertTrue as this provides more useful
        // debugging information such as this when expected and actual differ:
        // java.lang.AssertionError: Expected all the vertices to be found
        // Expected :[v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11], v[12]]
        // Actual :[v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11], v[12]]
        assertEquals(nonDuplicateExpectedResult, vertices, "Expected all the vertices to be found");

    }

    protected String convert(List<Vertex> vertices) {
        return vertices.stream().map(vertex -> vertex.property("aai-node-type").value().toString())
            .collect(Collectors.joining(","));
    }

    protected abstract void createGraph() throws AAIException, NoEdgeRuleFoundException,
        EdgeRuleNotFoundException, AmbiguousRuleChoiceException;

    protected abstract String getQueryName();

    protected abstract void addStartNode(GraphTraversal<Vertex, Vertex> g);

    protected abstract void addParam(Map<String, Object> params);

    public void initOnapQueryTest(SchemaVersion version) {
        this.version = version;
    }

}
