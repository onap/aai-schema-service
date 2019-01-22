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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.queries.common;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

import java.util.Map;

public class GetComponentList3Test extends QueryTest {


	public GetComponentList3Test() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {



		Vertex serviceinstance = graph.addVertex(T.label, "service-instance", T.id, "1", "aai-node-type", "service-instance", "service-instance-id", "serviceinstanceid0");

		Vertex l3network = graph.addVertex(T.label, "l3-network", T.id, "2", "aai-node-type", "l3-network", "network-id", "networkid0");
		Vertex genericvnf = graph.addVertex(T.label, "generic-vnf", T.id, "3", "aai-node-type", "generic-vnf", "vnf-id", "vnfid0");
		Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "4", "aai-node-type", "vserver", "vserver-id", "vserverid0");
		Vertex volume = graph.addVertex(T.label, "volume-group", T.id, "5", "aai-node-type", "volume-group", "volume-group-id", "volume-group-id-0", "volume-group-name", "volume-group-name0");
		Vertex l3network2 = graph.addVertex(T.label, "l3-network", T.id, "13", "aai-node-type", "l3-network", "network-id", "networkid2");
		Vertex vfmodule = graph.addVertex(T.label, "vf-module", T.id, "6", "aai-node-type", "vf-module", "vf-module-group-id", "vf-module-id-0", "vf-module-name", "vf-module-name0");
		Vertex subnet = graph.addVertex(T.label, "subnet", T.id, "16", "aai-node-type", "subnet", "subnet-id", "subnet0");
		Vertex subnet1 = graph.addVertex(T.label, "subnet", T.id, "18", "aai-node-type", "subnet", "subnet-id1", "subnet1");
		Vertex volume1 = graph.addVertex(T.label, "volume-group", T.id, "17", "aai-node-type", "volume-group", "volume-group-id", "volume-group-id-1", "volume-group-name", "volume-group-name1");
		Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "31", "aai-node-type", "pserver", "pserver-id", "pserver-id-0", "pserver-name", "pserver-name0");

		Vertex serviceinstance1 = graph.addVertex(T.label, "service-instance", T.id, "19", "aai-node-type", "service-instance", "service-instance-id", "serviceinstanceid1");
		Vertex config1 = graph.addVertex(T.label, "configuration", T.id, "20", "aai-node-type", "configuration", "configuration-id", "configuration1");
		Vertex l3network3 = graph.addVertex(T.label, "l3-network", T.id, "21", "aai-node-type", "l3-network", "network-id", "networkid3");
		Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "23", "aai-node-type", "generic-vnf", "vnf-id", "vnfid1");
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "24", "aai-node-type", "vserver", "vserver-id", "vserverid1");
		Vertex volume2 = graph.addVertex(T.label, "volume-group", T.id, "25", "aai-node-type", "volume-group", "volume-group-id", "volume-group-id-2", "volume-group-name", "volume-group-name2");
		Vertex l3network4 = graph.addVertex(T.label, "l3-network", T.id, "26", "aai-node-type", "l3-network", "network-id", "networkid4");
		Vertex vfmodule1 = graph.addVertex(T.label, "vf-module", T.id, "27", "aai-node-type", "vf-module", "vf-module-group-id", "vf-module-id-1", "vf-module-name", "vf-module-name1");
		Vertex subnet2 = graph.addVertex(T.label, "subnet", T.id, "28", "aai-node-type", "subnet", "subnet-id", "subnet2");
		Vertex subnet3 = graph.addVertex(T.label, "subnet", T.id, "29", "aai-node-type", "subnet", "subnet-id", "subnet3");
		Vertex volume3 = graph.addVertex(T.label, "volume-group", T.id, "30", "aai-node-type", "volume-group", "volume-group-id", "volume-group-id-3", "volume-group-name", "volume-group-name3");
		Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "32", "aai-node-type", "pserver", "pserver-id", "pserver-id-1", "pserver-name", "pserver-name1");

		GraphTraversalSource g = graph.traversal();


		rules.addEdge(g, serviceinstance, l3network);
		rules.addTreeEdge(g, l3network, subnet);
		rules.addEdge(g, serviceinstance, genericvnf);
		rules.addEdge(g, genericvnf, volume);
		rules.addTreeEdge(g, genericvnf, vfmodule);
		rules.addEdge(g, vfmodule, l3network2);
		rules.addTreeEdge(g, l3network2, subnet1);
		rules.addEdge(g, vfmodule, vserver);
		rules.addEdge(g, vserver, pserver);
		rules.addEdge(g, vfmodule, volume1);

		//false
		rules.addEdge(g, serviceinstance1, config1);
		rules.addEdge(g, serviceinstance1, l3network3);
		rules.addTreeEdge(g, l3network3, subnet2);
		rules.addEdge(g, serviceinstance1, genericvnf1);
		rules.addEdge(g, genericvnf1, volume2);
		rules.addTreeEdge(g, genericvnf1, vfmodule1);
		rules.addEdge(g, vfmodule1, l3network4);
		rules.addTreeEdge(g, l3network4, subnet3);
		rules.addEdge(g, vfmodule1, vserver1);
		rules.addEdge(g, vserver1, pserver1);
		rules.addEdge(g, vfmodule1, volume3);

		//Not expected in result


		expectedResult.add(l3network);
		expectedResult.add(subnet);
		expectedResult.add(genericvnf);
		expectedResult.add(volume);
		expectedResult.add(vfmodule);
		expectedResult.add(l3network2);
		expectedResult.add(subnet1);
		expectedResult.add(pserver);
		expectedResult.add(volume1);


	}

	@Override
	protected String getQueryName() {
		return	"getComponentList3";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "service-instance").has("service-instance-id", "serviceinstanceid0");

	}
	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}

}
