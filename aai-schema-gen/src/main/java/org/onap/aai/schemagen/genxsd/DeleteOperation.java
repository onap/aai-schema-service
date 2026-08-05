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
import io.swagger.models.parameters.QueryParameter;

import java.util.List;

/** The DELETE of a CRUD endpoint: it removes the object the path addresses. */
public class DeleteOperation {

    private final String useOpId;
    private final String xmlRootElementName;
    private final String tag;
    private final String path;
    private final List<Parameter> pathParams;

    public DeleteOperation(String useOpId, String xmlRootElementName, String tag, String path,
        List<Parameter> pathParams) {
        this.useOpId = useOpId;
        this.xmlRootElementName = xmlRootElementName;
        this.tag = tag;
        this.path = path;
        this.pathParams = pathParams == null ? List.of() : List.copyOf(pathParams);
    }

    /** This endpoint's DELETE, or null when the endpoint does not have one. */
    public Operation build() {
        if (isFilteredOut()) {
            return null;
        }
        Operation delete = new Operation();
        delete.addTag(tag);
        delete.setSummary("delete an existing " + xmlRootElementName);
        delete.setDescription("delete an existing " + xmlRootElementName);
        delete.setOperationId("delete" + useOpId);
        delete.setConsumes(OperationDefaults.JSON_AND_XML);
        delete.setProduces(OperationDefaults.JSON_AND_XML);
        delete.addResponse("default", OperationDefaults.uniformResponse());
        pathParams.forEach(delete::addParameter);
        if (!isRelationshipPath()) {
            delete.addParameter(resourceVersionParameter());
        }
        return delete;
    }

    /**
     * Deleting an object requires the caller to state which revision they saw; deleting a
     * relationship does not, as a relationship carries no revision of its own.
     */
    private static QueryParameter resourceVersionParameter() {
        QueryParameter resourceVersion = new QueryParameter();
        resourceVersion.setName("resource-version");
        resourceVersion.setDescription("resource-version for concurrency");
        resourceVersion.setRequired(true);
        resourceVersion.setType("string");
        return resourceVersion;
    }

    /**
     * A DELETE exists where there is something to address: an endpoint ending in a path parameter,
     * or
     * a relationship endpoint.
     */
    private boolean isFilteredOut() {
        return OperationFilter.hasNoTag(tag) || OperationFilter.isRelationshipChildPath(path)
            || OperationFilter.isRelationshipListPath(path) || OperationFilter.isSearchPath(path)
            || (!isRelationshipPath() && !path.endsWith("}"));
    }

    private boolean isRelationshipPath() {
        return path.endsWith("/" + PutOperation.RELATIONSHIP);
    }

    /**
     * Registers this operation's path in the run's {@link GenerationContext} (unless it is a
     * relationship endpoint). Kept separate from {@link #build()} so that building is free of side
     * effects; call this once, after the operation has been emitted.
     */
    public void register(GenerationContext context) {
        if (!isRelationshipPath()) {
            context.addDeletePath(path, xmlRootElementName);
        }
    }
}
