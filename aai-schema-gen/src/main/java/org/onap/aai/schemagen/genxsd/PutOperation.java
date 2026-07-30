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
import org.onap.aai.schemagen.yaml.YamlBlock;
import org.onap.aai.schemagen.yaml.YamlFragment;
import org.onap.aai.schemagen.yaml.YamlMapping;
import org.onap.aai.schemagen.yaml.YamlRaw;
import org.onap.aai.schemagen.yaml.YamlSequence;
import org.onap.aai.schemagen.yaml.YamlSerializer;
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
        boolean isRelationshipPath = path.endsWith("/" + RELATIONSHIP);
        YamlMapping operation = new YamlMapping().entry("tags", new YamlSequence().item(tag));
        if (isRelationshipPath) {
            operation.entry("summary", "see node definition for valid relationships");
        } else {
            operation.entry("summary", "create or update an existing " + xmlRootElementName).entry(
                "description",
                new YamlBlock().line("Create or update an existing " + xmlRootElementName + ".")
                    .line("#").line(
                        "Note! This PUT method has a corresponding PATCH method that can be used to update just a few of the fields of an existing object, rather than a full object replacement.  An example can be found in the [PATCH section] below"));
        }
        operation.entry("operationId", "createOrUpdate" + useOpId)
            .entry("consumes", new YamlSequence().item("application/json").item("application/xml"))
            .entry("produces", new YamlSequence().item("application/json").item("application/xml"))
            .entry("responses",
                new YamlMapping().entry("\"default\"",
                    new YamlFragment(GenerateXsd.getResponsesUrl())))
            .entry("parameters",
                new YamlSequence().item(new YamlRaw(pathParams)).item(bodyParameter()));
        if (isRelationshipPath) {
            // a relationship endpoint is not reached by any other operation, so it opens its own
            // path; otherwise the path key was already emitted by the GET
            YamlMapping pathItem =
                new YamlMapping().entry(path, new YamlMapping().entry("put", operation));
            return YamlSerializer.serialize(pathItem, 1);
        }
        return YamlSerializer.serialize(new YamlMapping().entry("put", operation), 2);
    }

    private YamlMapping bodyParameter() {
        String relationshipExamples = "[Valid relationship examples shown here](apidocs" + basePath
            + "/relations/" + version.toString() + "/"
            + useOpId.replace("RelationshipListRelationship", "") + ".json)";
        String useElement = xmlRootElementName;
        if (xmlRootElementName.equals(RELATIONSHIP)) {
            useElement += "-dict";
        }
        return new YamlMapping().entry("name", "body").entry("in", "body")
            .entry("description",
                xmlRootElementName + " object that needs to be created or updated. "
                    + relationshipExamples)
            .entry("required", "true").entry("schema",
                new YamlMapping().entry("$ref", "\"#/definitions/" + useElement + "\""));
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
