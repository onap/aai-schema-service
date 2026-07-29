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

import org.onap.aai.schemagen.GenerateXsd;
import org.onap.aai.setup.SchemaVersion;

public class PutOperation {
    public static final String RELATIONSHIP = "relationship";
    private String useOpId;
    private String xmlRootElementName;
    private String tag;
    private String path;
    private String pathParams;
    private SchemaVersion version;
    private String basePath;

    public PutOperation(String useOpId, String xmlRootElementName, String tag, String path,
        String pathParams, SchemaVersion v, String basePath) {
        super();
        this.useOpId = useOpId;
        this.xmlRootElementName = xmlRootElementName;
        this.tag = tag;
        this.path = path;
        this.pathParams = pathParams;
        this.version = v;
        this.basePath = basePath;
    }

    @Override
    public String toString() {
        // a valid tag is necessary
        if (OperationFilter.hasNoTag(tag)) {
            return "";
        }
        // All Put operation paths end with "relationship"
        // or there is a parameter at the end of the path
        // and there is a parameter in the path
        if (OperationFilter.isRelationshipChildPath(path)) { // filter paths with relationship-list
            return "";
        }
        if (OperationFilter.isRelationshipListPath(path)) {
            return "";
        }
        if (!path.endsWith("/" + RELATIONSHIP) && !path.endsWith("}")) {
            return "";
        }
        if (OperationFilter.isSearchPath(path)) {
            return "";
        }
        YamlWriter yaml = new YamlWriter();
        boolean isRelationshipPath = path.endsWith("/" + RELATIONSHIP);
        // a relationship endpoint is not reached by any other operation, so it opens its own path;
        // otherwise the path key was already emitted by the GET
        if (isRelationshipPath) {
            yaml.key(1, path);
        }
        yaml.key(2, "put");
        yaml.key(3, "tags");
        yaml.item(4, tag);

        if (isRelationshipPath) {
            yaml.entry(3, "summary", "see node definition for valid relationships");
        } else {
            yaml.entry(3, "summary", "create or update an existing " + xmlRootElementName);
            yaml.blockScalar(3, "description");
            yaml.text(4, "Create or update an existing " + xmlRootElementName + ".");
            yaml.text(4, "#");
            yaml.text(4,
                "Note! This PUT method has a corresponding PATCH method that can be used to update just a few of the fields of an existing object, rather than a full object replacement.  An example can be found in the [PATCH section] below");
        }
        String relationshipExamples = "[Valid relationship examples shown here](apidocs" + basePath
            + "/relations/" + version.toString() + "/"
            + useOpId.replace("RelationshipListRelationship", "") + ".json)";
        yaml.entry(3, "operationId", "createOrUpdate" + useOpId);
        yaml.key(3, "consumes");
        yaml.item(4, "application/json");
        yaml.item(4, "application/xml");
        yaml.key(3, "produces");
        yaml.item(4, "application/json");
        yaml.item(4, "application/xml");
        yaml.key(3, "responses");
        yaml.key(4, "\"default\"");
        yaml.fragment(5, GenerateXsd.getResponsesUrl());
        yaml.key(3, "parameters");
        yaml.raw(pathParams); // for nesting
        yaml.item(4, "name: body");
        yaml.entry(5, "in", "body");
        yaml.entry(5, "description", xmlRootElementName
            + " object that needs to be created or updated. " + relationshipExamples);
        yaml.entry(5, "required", "true");
        yaml.key(5, "schema");
        String useElement = xmlRootElementName;
        if (xmlRootElementName.equals("relationship")) {
            useElement += "-dict";
        }
        yaml.entry(6, "$ref", "\"#/definitions/" + useElement + "\"");
        return yaml.toString();
    }

    /**
     * Registers this operation's relationship path in the run's {@link GenerationContext} when the
     * path is a relationship endpoint. Kept separate from {@link #toString()} so that rendering is
     * free of side effects; call this once, after the operation has been emitted.
     */
    public void register(GenerationContext context) {
        if (path.endsWith("/" + RELATIONSHIP)) {
            context.addPutRelationPath(useOpId, path);
        }
    }

    /**
     * @deprecated retained for backwards compatibility; use {@link #register(GenerationContext)}
     *             instead. The return value was always the empty string and is never used by
     *             callers.
     */
    @Deprecated
    public String tagRelationshipPathMapEntry(GenerationContext context) {
        register(context);
        return "";
    }

}
