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

/** The GET of a CRUD endpoint: it returns the object the path addresses. */
public class GetOperation {

    private final String useOpId;
    private final String xmlRootElementName;
    private final String tag;
    private final String path;
    private final List<Parameter> pathParams;
    /** The indexed properties of this container, as query parameters. */
    private final List<Parameter> queryParams;

    public GetOperation(String useOpId, String xmlRootElementName, String tag, String path,
        List<Parameter> pathParams, GenerationContext context) {
        this.useOpId = useOpId;
        this.xmlRootElementName = xmlRootElementName;
        this.tag = tag;
        this.path = path;
        this.pathParams = pathParams == null ? List.of() : List.copyOf(pathParams);
        List<Parameter> containerProps = context.getContainerProps(xmlRootElementName);
        // copied, not held: the generator keeps adding to that list as it walks the container
        this.queryParams = containerProps == null ? List.of() : List.copyOf(containerProps);
    }

    /** This endpoint's GET, or null when the endpoint does not have one. */
    public Operation build() {
        if (isFilteredOut()) {
            return null;
        }
        Operation get = new Operation();
        get.addTag(tag);
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

    /** A relationship endpoint is written to, not read: its node definition documents it. */
    private boolean isFilteredOut() {
        return OperationFilter.hasNoTag(tag) || path.endsWith("/relationship")
            || OperationFilter.isRelationshipChildPath(path)
            || OperationFilter.isRelationshipListPath(path) || OperationFilter.isSearchPath(path);
    }
}
