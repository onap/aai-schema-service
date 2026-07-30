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
import org.onap.aai.schemagen.yaml.YamlFragment;
import org.onap.aai.schemagen.yaml.YamlMapping;
import org.onap.aai.schemagen.yaml.YamlRaw;
import org.onap.aai.schemagen.yaml.YamlSequence;
import org.onap.aai.schemagen.yaml.YamlSerializer;

public class DeleteOperation {
    private String useOpId;
    private String xmlRootElementName;
    private String tag;
    private String path;
    private String pathParams;

    public DeleteOperation(String useOpId, String xmlRootElementName, String tag, String path,
        String pathParams) {
        super();
        this.useOpId = useOpId;
        this.xmlRootElementName = xmlRootElementName;
        this.tag = tag;
        this.path = path;
        this.pathParams = pathParams;
    }

    @Override
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
        // All Delete operation paths end with "relationship"
        // or there is a parameter at the end of the path
        // and there is a parameter in the path

        if (!path.endsWith("/relationship") && !path.endsWith("}")) {
            return "";
        }
        YamlSequence parameters = new YamlSequence().item(new YamlRaw(pathParams));
        if (!path.endsWith("/relationship")) {
            parameters.item(new YamlMapping().entry("name", "resource-version").entry("in", "query")
                .entry("description", "resource-version for concurrency").entry("required", "true")
                .entry("type", "string"));
        }
        YamlMapping operation = new YamlMapping().entry("tags", new YamlSequence().item(tag))
            .entry("summary", "delete an existing " + xmlRootElementName)
            .entry("description", "delete an existing " + xmlRootElementName)
            .entry("operationId", "delete" + useOpId)
            .entry("consumes", new YamlSequence().item("application/json").item("application/xml"))
            .entry("produces", new YamlSequence().item("application/json").item("application/xml"))
            .entry("responses",
                new YamlMapping().entry("\"default\"",
                    new YamlFragment(GenerateXsd.getResponsesUrl())))
            .entry("parameters", parameters);
        // the path key was already emitted by the GET, so this starts at the operation level
        return YamlSerializer.serialize(new YamlMapping().entry("delete", operation), 2);
    }

    /**
     * Registers this operation's path in the run's {@link GenerationContext} (unless it is a
     * relationship endpoint). Kept separate from {@link #toString()} so that rendering is free of
     * side effects; call this once, after the operation has been emitted.
     */
    public void register(GenerationContext context) {
        if (!path.endsWith("/relationship")) {
            context.addDeletePath(path, xmlRootElementName);
        }
    }

    /**
     * @deprecated retained for backwards compatibility; use {@link #register(GenerationContext)}
     *             instead. The return value is never used by callers.
     */
    @Deprecated
    public String objectPathMapEntry(GenerationContext context) {
        register(context);
        return (xmlRootElementName + ":" + path);
    }
}
