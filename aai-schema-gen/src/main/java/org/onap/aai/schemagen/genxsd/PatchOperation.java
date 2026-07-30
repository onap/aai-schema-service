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
import org.onap.aai.schemagen.yaml.YamlBlock;
import org.onap.aai.schemagen.yaml.YamlFragment;
import org.onap.aai.schemagen.yaml.YamlMapping;
import org.onap.aai.schemagen.yaml.YamlRaw;
import org.onap.aai.schemagen.yaml.YamlSequence;
import org.onap.aai.schemagen.yaml.YamlSerializer;
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

        // the guards above have already returned for a relationship path, so unlike the PUT this
        // emitter has only the one shape: it never opens a path key of its own
        String relationshipExamples = "[See Examples](apidocs" + basePath + "/relations/"
            + version.toString() + "/" + useOpId + ".json)";
        YamlMapping operation = new YamlMapping().entry("tags", new YamlSequence().item(tag))
            .entry("summary", "update an existing " + xmlRootElementName)
            .entry("description", description()).entry("operationId", "Update" + useOpId)
            .entry("consumes", new YamlSequence().item("application/json"))
            .entry("produces", new YamlSequence().item("application/json"))
            .entry("responses",
                new YamlMapping().entry("\"default\"",
                    new YamlFragment(GenerateXsd.getResponsesUrl())))
            .entry("parameters", new YamlSequence().item(new YamlRaw(pathParams))
                .item(bodyParameter(relationshipExamples)));
        // the path key was already emitted by the GET, so this starts at the operation level
        return YamlSerializer.serialize(new YamlMapping().entry("patch", operation), 2);
    }

    /** The markdown note on how PATCH differs from PUT, as a literal block. */
    private YamlBlock description() {
        return new YamlBlock().line("Update an existing " + xmlRootElementName).line("#").line(
            "Note:  Endpoints that are not devoted to object relationships support both PUT and PATCH operations.")
            .line("The PUT operation will entirely replace an existing object.")
            .line(
                "The PATCH operation sends a \"description of changes\" for an existing object.  The entire set of changes must be applied.  An error result means no change occurs.")
            .line("#").line("Other differences between PUT and PATCH are:").line("#")
            .line(
                "- For PATCH, you can send any of the values shown in sample REQUEST body.  There are no required values.")
            .line(
                "- For PATCH, resource-id which is a required REQUEST body element for PUT, must not be sent.")
            .line(
                "- PATCH cannot be used to update relationship elements; there are dedicated PUT operations for this.");
    }

    private YamlMapping bodyParameter(String relationshipExamples) {
        return new YamlMapping().entry("name", "body").entry("in", "body")
            .entry("description",
                xmlRootElementName + " object that needs to be updated." + relationshipExamples)
            .entry("required", "true").entry("schema", new YamlMapping().entry("$ref",
                "\"#/definitions/" + prefixForPatch + xmlRootElementName + "\""));
    }
}
