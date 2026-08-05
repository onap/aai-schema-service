/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
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

import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;

import java.util.List;

/**
 * The GET of a {@code /nodes/...} endpoint: the same object as the CRUD GET, reached by node type
 * instead of by its place in the inventory tree. One per node type, tagged {@code Operations}.
 */
public class NodeGetOperation {

    private final String useOpId;
    private final String xmlRootElementName;
    private final String tag;
    /** The CRUD path this node endpoint is derived from; all the guards apply to it. */
    private final String crudPath;
    private final String path;
    private final List<Parameter> pathParams;
    /** The indexed properties of this container, as query parameters. */
    private final List<Parameter> queryParams;
    private final NodeGenerationContext context;

    public NodeGetOperation(String useOpId, String xmlRootElementName, String tag, String path,
        List<Parameter> pathParams, NodeGenerationContext context) {
        this.useOpId = useOpId;
        this.xmlRootElementName = xmlRootElementName;
        this.tag = tag;
        this.crudPath = path;
        this.path = nodePath();
        this.pathParams = pathParams == null ? List.of() : List.copyOf(pathParams);
        this.context = context;
        List<Parameter> containerProps = context.getContainerProps(xmlRootElementName);
        // copied, not held: the generator keeps adding to that list as it walks the container
        this.queryParams = containerProps == null ? List.of() : List.copyOf(containerProps);
    }

    String nodePath() {
        int loc = crudPath.indexOf(xmlRootElementName);
        return loc > 0 ? "/nodes/" + crudPath.substring(loc) : null;
    }

    /**
     * The path this operation is keyed under. An endpoint that addresses no single object is
     * queried
     * by property instead, which the key spells out.
     */
    public String getPath() {
        return path.indexOf('{') == -1 ? path + "?parameter=value[&parameter2=value2]" : path;
    }

    /** This node type's GET, or null when it does not have one. */
    public Operation build() {
        if (isFilteredOut()) {
            return null;
        }
        Operation get = new Operation();
        get.addTag("Operations");
        get.setSummary("returns " + xmlRootElementName);
        get.setDescription("returns " + xmlRootElementName);
        get.setOperationId("get" + useOpId);
        get.setProduces(OperationDefaults.JSON_AND_XML);
        get.addResponse("200", OperationDefaults.successResponse(xmlRootElementName));
        get.addResponse("default", OperationDefaults.uniformResponse());
        pathParams.forEach(get::addParameter);
        queryParams.forEach(get::addParameter);
        return get;
    }

    /**
     * On top of the shared guards: the {@code actions} and {@code nodes} trees have no node
     * endpoints of their own, and a node type yields at most one endpoint, so the first occurrence
     * of a type wins and later ones are suppressed.
     */
    private boolean isFilteredOut() {
        return OperationFilter.hasNoTag(tag) || crudPath.endsWith("/relationship")
            || OperationFilter.isRelationshipChildPath(crudPath)
            || OperationFilter.isRelationshipListPath(crudPath)
            || OperationFilter.isSearchPath(crudPath) || crudPath.startsWith("/actions")
            || crudPath.startsWith("/nodes") || context.isAlreadyEmitted(xmlRootElementName);
    }

    /**
     * Marks this node type as having had its GET emitted, so that later occurrences are suppressed.
     * Kept separate from {@link #build()} so that building is free of side effects; call this once,
     * after the operation has been emitted.
     */
    public void register() {
        context.markEmitted(xmlRootElementName);
    }
}
