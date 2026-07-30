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
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.onap.aai.schemagen.GenerateXsd;
import org.onap.aai.schemagen.yaml.YamlFragment;
import org.onap.aai.schemagen.yaml.YamlMapping;
import org.onap.aai.schemagen.yaml.YamlRaw;
import org.onap.aai.schemagen.yaml.YamlSequence;
import org.onap.aai.schemagen.yaml.YamlSerializer;

public class GetOperation {

    private String useOpId;
    private String xmlRootElementName;
    private String tag;
    private String path;
    private String pathParams;
    private String queryParams;

    public GetOperation(String useOpId, String xmlRootElementName, String tag, String path,
        String pathParams, GenerationContext context) {
        super();
        this.useOpId = useOpId;
        this.xmlRootElementName = xmlRootElementName;
        this.tag = tag;
        this.path = path;
        this.pathParams = pathParams;

        Vector<String> containerProps = context.getContainerProps(xmlRootElementName);
        if (containerProps == null) {
            this.queryParams = "";
        } else {
            this.queryParams = String.join("", containerProps);
        }
    }

    @Override
    public String toString() {
        StringTokenizer st;
        st = new StringTokenizer(path, "/");
        // Path has to be longer than one element
        /*
         * if ( st.countTokens() <= 1) {
         * return "";
         * }
         */
        // a valid tag is necessary
        if (OperationFilter.hasNoTag(tag)) {
            return "";
        }
        if (path.endsWith("/relationship")) {
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
        YamlMapping operation = new YamlMapping().entry("tags", new YamlSequence().item(tag))
            .entry("summary", "returns " + xmlRootElementName)
            .entry("description", "returns " + xmlRootElementName)
            .entry("operationId", "get" + useOpId)
            .entry("produces", new YamlSequence().item("application/json").item("application/xml"))
            .entry("responses", responses());
        YamlSequence parameters = parameters();
        if (!parameters.isEmpty()) {
            operation.entry("parameters", parameters);
        }
        YamlMapping pathItem =
            new YamlMapping().entry(path, new YamlMapping().entry("get", operation));
        // the path item is spliced into the document's paths block, one level in
        return YamlSerializer.serialize(pathItem, 1);
    }

    private YamlMapping responses() {
        YamlMapping successSchema =
            new YamlMapping().entry("$ref", "\"#/definitions/" + xmlRootElementName + "\"");
        return new YamlMapping()
            .entry("\"200\"", new YamlMapping().entry("description", "successful operation")
                // the $ref sits a level deeper than nesting alone would put it, as the
                // current documents have it
                .entryAtShiftedDepth("schema", successSchema, 1))
            .entry("\"default\"", new YamlFragment(GenerateXsd.getResponsesUrl()));
    }

    /**
     * The operation's parameters, inherited from the path and from the container's properties. Both
     * arrive pre-rendered and already indented, so they pass through as raw items; an empty
     * sequence
     * means the {@code parameters:} key is left out altogether.
     */
    private YamlSequence parameters() {
        YamlSequence parameters = new YamlSequence();
        if (StringUtils.isNotEmpty(pathParams)) {
            parameters.item(new YamlRaw(pathParams));
        }
        if (StringUtils.isNotEmpty(queryParams)) {
            parameters.item(new YamlRaw(queryParams));
        }
        return parameters;
    }
}
