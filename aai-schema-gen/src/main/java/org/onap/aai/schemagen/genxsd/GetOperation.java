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
        YamlWriter yaml = new YamlWriter();
        yaml.key(1, path);
        yaml.key(2, "get");
        yaml.key(3, "tags");
        yaml.item(4, tag);
        yaml.entry(3, "summary", "returns " + xmlRootElementName);
        yaml.entry(3, "description", "returns " + xmlRootElementName);
        yaml.entry(3, "operationId", "get" + useOpId);
        yaml.key(3, "produces");
        yaml.item(4, "application/json");
        yaml.item(4, "application/xml");
        yaml.key(3, "responses");
        yaml.key(4, "\"200\"");
        yaml.entry(5, "description", "successful operation");
        yaml.key(5, "schema");
        // the $ref sits two levels below its schema key, as the current documents have it
        yaml.entry(7, "$ref", "\"#/definitions/" + xmlRootElementName + "\"");
        yaml.key(4, "\"default\"");
        yaml.fragment(5, GenerateXsd.getResponsesUrl());
        if (StringUtils.isNotEmpty(pathParams) || StringUtils.isNotEmpty(queryParams)) {
            yaml.key(3, "parameters");
        }
        if (StringUtils.isNotEmpty(pathParams)) {
            yaml.raw(pathParams);
        }
        if (StringUtils.isNotEmpty(queryParams)) {
            yaml.raw(queryParams);
        }
        return yaml.toString();
    }
}
