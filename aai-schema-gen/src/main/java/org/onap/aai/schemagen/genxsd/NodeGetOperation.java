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

public class NodeGetOperation {

    private String useOpId;
    private String xmlRootElementName;
    private String tag;
    private String path;
    private String CRUDpath;
    private String pathParams;
    private String queryParams;
    private final NodeGenerationContext context;

    public NodeGetOperation(String useOpId, String xmlRootElementName, String tag, String path,
        String pathParams, NodeGenerationContext context) {
        super();
        this.useOpId = useOpId;
        this.xmlRootElementName = xmlRootElementName;
        this.tag = tag;
        this.CRUDpath = path;
        this.path = nodePath();
        this.pathParams = pathParams;
        this.context = context;

        Vector<String> containerProps = context.getContainerProps(xmlRootElementName);
        if (containerProps == null) {
            this.queryParams = "";
        } else {
            this.queryParams = String.join("", containerProps);
        }
    }

    String nodePath() {
        String path = null;
        int loc = CRUDpath.indexOf(xmlRootElementName);
        if (loc > 0) {
            path = "/nodes/" + CRUDpath.substring(loc);
        }
        return path;
    }

    @Override
    public String toString() {
        StringTokenizer st;
        st = new StringTokenizer(CRUDpath, "/");
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
        if (CRUDpath.endsWith("/relationship")) {
            return "";
        }
        if (OperationFilter.isRelationshipChildPath(CRUDpath)) { // filter paths with
                                                                 // relationship-list
            return "";
        }
        if (OperationFilter.isRelationshipListPath(CRUDpath)) {
            return "";
        }
        if (OperationFilter.isSearchPath(CRUDpath)) {
            return "";
        }
        if (CRUDpath.startsWith("/actions")) {
            return "";
        }
        if (CRUDpath.startsWith("/nodes")) {
            return "";
        }
        if (context.isAlreadyEmitted(xmlRootElementName)) {
            return "";
        }
        YamlWriter yaml = new YamlWriter();
        if (path.indexOf('{') == -1) {
            path += "?parameter=value[&parameter2=value2]";
        }
        yaml.key(1, path);
        yaml.key(2, "get");
        yaml.key(3, "tags");
        yaml.item(4, "Operations");
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
            yaml.blankLine();
            yaml.key(3, "parameters");
        }
        if (StringUtils.isNotEmpty(pathParams)) {
            yaml.raw(pathParams);
        }
        if (StringUtils.isNotEmpty(pathParams) && StringUtils.isNotEmpty(queryParams)) {
            yaml.blankLine();
        }
        if (StringUtils.isNotEmpty(queryParams)) {
            yaml.raw(queryParams);
        }
        return yaml.toString();
    }

    /**
     * Marks this object as having had its node-GET operation emitted, so that later occurrences are
     * suppressed. Kept separate from {@link #toString()} so that rendering is free of side effects;
     * call this once, after a non-empty operation has been emitted — which is exactly when the
     * original code reached the tail of {@code toString()}.
     */
    public void register() {
        context.markEmitted(xmlRootElementName);
    }
}
