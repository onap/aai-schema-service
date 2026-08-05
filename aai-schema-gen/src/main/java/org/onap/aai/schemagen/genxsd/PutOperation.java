/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright © 2018 IBM.
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
import io.swagger.models.RefModel;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;

import java.util.List;

import org.onap.aai.setup.SchemaVersion;

/** The PUT of a CRUD endpoint: it creates or wholly replaces the object the path addresses. */
public class PutOperation {
    public static final String RELATIONSHIP = "relationship";

    private final String useOpId;
    private final String xmlRootElementName;
    private final String tag;
    private final String path;
    private final List<Parameter> pathParams;
    private final SchemaVersion version;
    private final String basePath;

    public PutOperation(String useOpId, String xmlRootElementName, String tag, String path,
        List<Parameter> pathParams, SchemaVersion v, String basePath) {
        this.useOpId = useOpId;
        this.xmlRootElementName = xmlRootElementName;
        this.tag = tag;
        this.path = path;
        this.pathParams = pathParams == null ? List.of() : List.copyOf(pathParams);
        this.version = v;
        this.basePath = basePath;
    }

    /** This endpoint's PUT, or null when the endpoint does not have one. */
    public Operation build() {
        if (isFilteredOut()) {
            return null;
        }
        Operation put = new Operation();
        put.addTag(tag);
        if (isRelationshipPath()) {
            put.setSummary("see node definition for valid relationships");
        } else {
            put.setSummary("create or update an existing " + xmlRootElementName);
            // the trailing newline is what makes this render as a literal block, not as one line
            put.setDescription(String.join("\n",
                "Create or update an existing " + xmlRootElementName + ".", "#",
                "Note! This PUT method has a corresponding PATCH method that can be used to update just a few of the fields of an existing object, rather than a full object replacement.  An example can be found in the [PATCH section] below",
                ""));
        }
        put.setOperationId("createOrUpdate" + useOpId);
        put.setConsumes(OperationDefaults.JSON_AND_XML);
        put.setProduces(OperationDefaults.JSON_AND_XML);
        put.addResponse("default", OperationDefaults.uniformResponse());
        pathParams.forEach(put::addParameter);
        put.addParameter(bodyParameter());
        return put;
    }

    private BodyParameter bodyParameter() {
        BodyParameter body = new BodyParameter();
        body.setName("body");
        body.setDescription(xmlRootElementName + " object that needs to be created or updated. "
            + relationshipExamples());
        body.setRequired(true);
        // a relationship is defined as a dictionary; the payload is one of its entries
        body.setSchema(
            new RefModel(RELATIONSHIP.equals(xmlRootElementName) ? xmlRootElementName + "-dict"
                : xmlRootElementName));
        return body;
    }

    private String relationshipExamples() {
        return "[Valid relationship examples shown here](apidocs" + basePath + "/relations/"
            + version.toString() + "/" + useOpId.replace("RelationshipListRelationship", "")
            + ".json)";
    }

    /**
     * A PUT exists where there is something to address: an endpoint ending in a path parameter, or
     * a
     * relationship endpoint.
     */
    private boolean isFilteredOut() {
        return OperationFilter.hasNoTag(tag) || OperationFilter.isRelationshipChildPath(path)
            || OperationFilter.isRelationshipListPath(path)
            || (!isRelationshipPath() && !path.endsWith("}")) || OperationFilter.isSearchPath(path);
    }

    private boolean isRelationshipPath() {
        return path.endsWith("/" + RELATIONSHIP);
    }

    /**
     * Registers this operation's relationship path in the run's {@link GenerationContext} when the
     * path is a relationship endpoint. Kept separate from {@link #build()} so that building is free
     * of side effects; call this once, after the operation has been emitted.
     */
    public void register(GenerationContext context) {
        if (isRelationshipPath()) {
            context.addPutRelationPath(useOpId, path);
        }
    }
}
