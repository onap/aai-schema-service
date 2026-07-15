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
        StringBuilder pathSb = new StringBuilder();
        StringBuilder relationshipExamplesSb = new StringBuilder();
        if (path.endsWith("/" + RELATIONSHIP)) {
            pathSb.append("  ").append(path).append(":\n");
        }
        pathSb.append("    put:\n");
        pathSb.append("      tags:\n");
        pathSb.append("        - ").append(tag).append("\n");

        if (path.endsWith("/" + RELATIONSHIP)) {
            pathSb.append("      summary: see node definition for valid relationships\n");
        } else {
            pathSb.append("      summary: create or update an existing ").append(xmlRootElementName)
                .append("\n");
            pathSb.append("      description: |\n        Create or update an existing ")
                .append(xmlRootElementName).append(
                    ".\n        #\n        Note! This PUT method has a corresponding PATCH method that can be used to update just a few of the fields of an existing object, rather than a full object replacement.  An example can be found in the [PATCH section] below\n");
        }
        relationshipExamplesSb.append("[Valid relationship examples shown here](apidocs")
            .append(basePath).append("/relations/").append(version.toString()).append("/")
            .append(useOpId.replace("RelationshipListRelationship", "")).append(".json)");
        pathSb.append("      operationId: createOrUpdate").append(useOpId).append("\n");
        pathSb.append("      consumes:\n");
        pathSb.append("        - application/json\n");
        pathSb.append("        - application/xml\n");
        pathSb.append("      produces:\n");
        pathSb.append("        - application/json\n");
        pathSb.append("        - application/xml\n");
        pathSb.append("      responses:\n");
        pathSb.append("        \"default\":\n");
        pathSb.append("          ").append(GenerateXsd.getResponsesUrl());

        pathSb.append("      parameters:\n");
        pathSb.append(pathParams); // for nesting
        pathSb.append("        - name: body\n");
        pathSb.append("          in: body\n");
        pathSb.append("          description: ").append(xmlRootElementName)
            .append(" object that needs to be created or updated. ")
            .append(relationshipExamplesSb.toString()).append("\n");
        pathSb.append("          required: true\n");
        pathSb.append("          schema:\n");
        String useElement = xmlRootElementName;
        if (xmlRootElementName.equals("relationship")) {
            useElement += "-dict";
        }
        pathSb.append("            $ref: \"#/definitions/").append(useElement).append("\"\n");
        return pathSb.toString();
    }

    /**
     * Registers this operation's relationship path in the shared {@link PutRelationPathSet} when the
     * path is a relationship endpoint. Kept separate from {@link #toString()} so that rendering is
     * free of side effects; call this once, after the operation has been emitted.
     */
    public void register() {
        if (path.endsWith("/" + RELATIONSHIP)) {
            PutRelationPathSet.add(useOpId, path);
        }
    }

    /**
     * @deprecated retained for backwards compatibility; use {@link #register()} instead. The return
     *             value was always the empty string and is never used by callers.
     */
    @Deprecated
    public String tagRelationshipPathMapEntry() {
        register();
        return "";
    }

}
