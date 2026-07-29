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

import java.util.StringTokenizer;

import org.onap.aai.schemagen.GenerateXsd;
import org.onap.aai.setup.SchemaVersion;

public class PatchOperation {
    private String useOpId;
    private String xmlRootElementName;
    private String tag;
    private String path;
    private String pathParams;
    private String prefixForPatch;
    private SchemaVersion version;
    private String basePath;

    public PatchOperation(String useOpId, String xmlRootElementName, String tag, String path,
        String pathParams, SchemaVersion v, String basePath) {
        super();
        this.useOpId = useOpId;
        this.xmlRootElementName = xmlRootElementName;
        this.tag = tag;
        this.path = path;
        this.pathParams = pathParams;
        this.prefixForPatch = "";
        this.version = v;
        this.basePath = basePath;
    }

    public void setPrefixForPatchRef(String prefixForPatchRef) {
        this.prefixForPatch = prefixForPatchRef;
    }

    public String toString() {
        StringTokenizer st;
        st = new StringTokenizer(path, "/");
        // a valid tag is necessary
        if (OperationFilter.hasNoTag(tag)) {
            return "";
        }
        if (OperationFilter.isRelationshipChildPath(path)) { // filter paths with relationship-list
            return "";
        }
        if (OperationFilter.isRelationshipListPath(path)) {
            return "";
        }
        if (OperationFilter.isSearchPath(path)) {
            return "";
        }
        // No Patch operation paths end with "relationship"

        if (path.endsWith("/relationship")) {
            return "";
        }
        if (!path.endsWith("}")) {
            return "";
        }

        YamlWriter yaml = new YamlWriter();
        String relationshipExamples = "";
        // unreachable given the guard above, but kept so this emitter stays symmetric with the PUT
        if (path.endsWith("/relationship")) {
            yaml.key(1, path);
        }
        yaml.key(2, "patch");
        yaml.key(3, "tags");
        yaml.item(4, tag);

        if (path.endsWith("/relationship")) {
            yaml.entry(3, "summary", "see node definition for valid relationships");
        } else {
            relationshipExamples = "[See Examples](apidocs" + basePath + "/relations/"
                + version.toString() + "/" + useOpId + ".json)";
            yaml.entry(3, "summary", "update an existing " + xmlRootElementName);
            yaml.blockScalar(3, "description");
            yaml.text(4, "Update an existing " + xmlRootElementName);
            yaml.text(4, "#");
            yaml.text(4,
                "Note:  Endpoints that are not devoted to object relationships support both PUT and PATCH operations.");
            yaml.text(4, "The PUT operation will entirely replace an existing object.");
            yaml.text(4,
                "The PATCH operation sends a \"description of changes\" for an existing object.  The entire set of changes must be applied.  An error result means no change occurs.");
            yaml.text(4, "#");
            yaml.text(4, "Other differences between PUT and PATCH are:");
            yaml.text(4, "#");
            yaml.text(4,
                "- For PATCH, you can send any of the values shown in sample REQUEST body.  There are no required values.");
            yaml.text(4,
                "- For PATCH, resource-id which is a required REQUEST body element for PUT, must not be sent.");
            yaml.text(4,
                "- PATCH cannot be used to update relationship elements; there are dedicated PUT operations for this.");
        }
        yaml.entry(3, "operationId", "Update" + useOpId);
        yaml.key(3, "consumes");
        yaml.item(4, "application/json");
        yaml.key(3, "produces");
        yaml.item(4, "application/json");
        yaml.key(3, "responses");
        yaml.key(4, "\"default\"");
        yaml.fragment(5, GenerateXsd.getResponsesUrl());
        yaml.key(3, "parameters");
        yaml.raw(pathParams); // for nesting
        yaml.item(4, "name: body");
        yaml.entry(5, "in", "body");
        yaml.entry(5, "description",
            xmlRootElementName + " object that needs to be updated." + relationshipExamples);
        yaml.entry(5, "required", "true");
        yaml.key(5, "schema");
        yaml.entry(6, "$ref", "\"#/definitions/" + prefixForPatch + xmlRootElementName + "\"");
        return yaml.toString();
    }
}
