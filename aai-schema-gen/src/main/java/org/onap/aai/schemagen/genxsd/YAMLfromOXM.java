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

import com.google.common.collect.Multimap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.EdgeRule;
import org.onap.aai.edges.EdgeRuleQuery;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.setup.SchemaConfigVersions;
import org.onap.aai.setup.SchemaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class YAMLfromOXM extends OxmFileProcessor {
    private static final Logger logger = LoggerFactory.getLogger("YAMLfromOXM.class");
    // private static StringBuffer totalPathSbAccumulator = new StringBuffer();
    private static final String root = "../aai-schema/src/main/resources";
    private static final String autoGenRoot = "aai-schema/src/main/resources";
    private static final String generateTypeYAML = "yaml";
    private static final String normalStartDir = "aai-schema-gen";
    private static final String yaml_dir = (((System.getProperty("user.dir") != null)
        && (!System.getProperty("user.dir").contains(normalStartDir))) ? autoGenRoot : root)
        + "/aai_swagger_yaml";
    private final String patchDefinePrefix = "zzzz-patch-";
    private StringBuilder inventoryDefSb = null;

    private String basePath;

    public YAMLfromOXM(String basePath, SchemaConfigVersions schemaConfigVersions, NodeIngestor ni,
        EdgeIngestor ei) {
        super(schemaConfigVersions, ni, ei);
        this.basePath = basePath;
    }

    public void setOxmVersion(File oxmFile, SchemaVersion v) {
        super.setOxmVersion(oxmFile, v);
    }

    public void setXmlVersion(String xml, SchemaVersion v) {
        super.setXmlVersion(xml, v);
    }

    public void setVersion(SchemaVersion v) {
        super.setVersion(v);
    }

    @Override
    public String getDocumentHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(LINE_SEPARATOR).append(
            "# ============LICENSE_START=======================================================")
            .append(LINE_SEPARATOR).append("# org.onap.aai").append(LINE_SEPARATOR)
            .append(
                "# ================================================================================")
            .append(LINE_SEPARATOR)
            .append("# Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.")
            .append(LINE_SEPARATOR)
            .append(
                "# ================================================================================")
            .append(LINE_SEPARATOR)
            .append(
                "# Licensed under the Creative Commons License, Attribution 4.0 Intl. (the \"License\");")
            .append(LINE_SEPARATOR)
            .append("# you may not use this file except in compliance with the License.")
            .append(LINE_SEPARATOR).append("# You may obtain a copy of the License at")
            .append(LINE_SEPARATOR).append("# <p>").append(LINE_SEPARATOR)
            .append("# https://creativecommons.org/licenses/by/4.0/").append(LINE_SEPARATOR)
            .append("# <p>").append(LINE_SEPARATOR)
            .append("# Unless required by applicable law or agreed to in writing, software")
            .append(LINE_SEPARATOR)
            .append("# distributed under the License is distributed on an \"AS IS\" BASIS,")
            .append(LINE_SEPARATOR)
            .append("# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.")
            .append(LINE_SEPARATOR)
            .append("# See the License for the specific language governing permissions and")
            .append(LINE_SEPARATOR).append("# limitations under the License.")
            .append(LINE_SEPARATOR)
            .append(
                "# ============LICENSE_END=========================================================")
            .append(LINE_SEPARATOR).append("#").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        sb.append("swagger: \"2.0\"\ninfo:").append(LINE_SEPARATOR).append("  ");
        sb.append("description: |");
        if (versionSupportsSwaggerDiff(v.toString())) {
            sb.append("\n    [Differences versus the previous schema version](" + "apidocs"
                + basePath + "/aai_swagger_" + v.toString() + ".diff)");
        }
        sb.append(DOUBLE_LINE_SEPARATOR)
            .append("    This document is best viewed with Firefox or Chrome. ");
        sb.append(
            "Nodes can be found by opening the models link below and finding the node-type. ");
        sb.append("Edge definitions can be found with the node definitions.").append(LINE_SEPARATOR)
            .append("  version: \"").append(v.toString()).append("\"").append(LINE_SEPARATOR);
        sb.append("  title: Active and Available Inventory REST API").append(LINE_SEPARATOR);
        sb.append("  license:").append(LINE_SEPARATOR)
            .append(
                "    name: Apache 2.0\n    url: http://www.apache.org/licenses/LICENSE-2.0.html")
            .append(LINE_SEPARATOR);
        sb.append("host: localhost").append(LINE_SEPARATOR).append("basePath: ").append(basePath)
            .append("/").append(v.toString()).append(LINE_SEPARATOR);
        sb.append("schemes:").append(LINE_SEPARATOR).append("  - https\npaths:")
            .append(LINE_SEPARATOR);
        return sb.toString();
    }

    protected void init() throws ParserConfigurationException, SAXException, IOException,
        FileNotFoundException, EdgeRuleNotFoundException {
        super.init();
    }

    @Override
    public String process() throws ParserConfigurationException, SAXException, IOException,
        FileNotFoundException, EdgeRuleNotFoundException {
        StringBuilder sb = new StringBuilder();
        StringBuilder pathSb = new StringBuilder();
        try {
            init();
        } catch (Exception e) {
            logger.error("Error initializing " + this.getClass(), e);
            throw e;
        }
        pathSb.append(getDocumentHeader());
        StringBuilder definitionsSb = new StringBuilder();
        Element elem;
        String javaTypeName;
        combinedJavaTypes = new HashMap<>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            elem = (Element) javaTypeNodes.item(i);
            javaTypeName = elem.getAttribute("name");
            boolean processInventory = false;
            if (!"Inventory".equals(javaTypeName)) {
                if (generatedJavaType.containsKey(getXmlRootElementName(javaTypeName))) {
                    continue;
                }
                // will combine all matching java-types
                elem = getJavaTypeElementSwagger(javaTypeName);
            } else {
                processInventory = true;
            }

            XSDElement javaTypeElement = new XSDElement(elem);

            if (processInventory) {
                getTopLevelPaths(javaTypeElement);
            }

            if (javaTypeName == null) {
                String msg = "Invalid OXM file: <java-type> has no name attribute in " + oxmFile;
                logger.error(msg);
                throw new SAXException(msg);
            }
            namespaceFilter.add(getXmlRootElementName(javaTypeName));
            processJavaTypeElementSwagger(javaTypeName, javaTypeElement, pathSb, definitionsSb,
                null, null, null, null, null, null);
        }
        sb.append(pathSb);

        sb.append(appendDefinitions());
        PutRelationPathSet prp = new PutRelationPathSet(v);
        prp.generateRelations(ei);
        return sb.toString();
    }

    public String appendDefinitions() {
        return appendDefinitions(null);
    }

    public String appendDefinitions(Set<String> namespaceFilter) {
        // append definitions
        if (inventoryDefSb != null) {
            javaTypeDefinitions.put("inventory", inventoryDefSb.toString());
        }
        StringBuilder sb = new StringBuilder("definitions:\n");
        Map<String, String> sortedJavaTypeDefinitions =
            new TreeMap<String, String>(javaTypeDefinitions);
        for (Map.Entry<String, String> entry : sortedJavaTypeDefinitions.entrySet()) {
            // logger.info("Key: "+entry.getKey()+"Value: "+ entry.getValue());
            if (namespaceFilter != null && entry.getKey().matches("service-capabilities")) {
                for (String tally : namespaceFilter) {
                    logger.debug("Marker: " + tally);
                }
            }
            if (namespaceFilter != null && (!namespaceFilter.contains(entry.getKey()))) {
                continue;
            }
            logger.debug(
                "Key: " + entry.getKey() + "Test: " + ("relationship-dict".equals(entry.getKey())));
            if (entry.getKey().matches("relationship-dict")) {
                String jb = entry.getValue();
                logger.debug("Value: " + jb);
                int ndx = jb.indexOf("related-to-property:");
                if (ndx > 0) {
                    jb = jb.substring(0, ndx);
                    jb = StringUtils.stripEnd(jb, " ");
                }
                logger.debug("Value-after: " + jb);
                sb.append(jb);
                continue;
            }
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

    private String getDictionary(String resource) {
        StringBuilder dictSb = new StringBuilder();
        dictSb.append("  ").append(resource).append(":\n");
        dictSb.append("    description: |\n");
        dictSb.append("      dictionary of ").append(resource).append("\n");
        dictSb.append("    type: object\n");
        dictSb.append("    properties:\n");
        dictSb.append("      ").append(resource).append(":\n");
        dictSb.append("        type: array\n");
        dictSb.append("        items:\n");
        dictSb.append("          $ref: \"#/definitions/").append(resource).append("-dict\"\n");
        return dictSb.toString();
    }

    private String processJavaTypeElementSwagger(String javaTypeName, Element javaTypeElement,
        StringBuilder pathSb, StringBuilder definitionsSb, String path, String tag, String opId,
        String getItemName, StringBuilder pathParams, String validEdges) {

        String xmlRootElementName = getXMLRootElementName(javaTypeElement);
        StringBuilder definitionsLocalSb = new StringBuilder(256);
        StringBuilder definitionsLocalPatchSb = new StringBuilder(256);

        String useTag = null;
        String useOpId = null;
        logger.debug("tag=" + tag);
        if (tag != null) {
            // set ignore to true to skip Actions and Search
            boolean topCheck = checkTopLevel(tag, true);
            if (!topCheck) {
                return null;
            }
        }

        if (!"Inventory".equals(javaTypeName)) {
            if ("AaiInternal".equals(javaTypeName)) {
                return null;
            }
            if (opId == null) {
                useOpId = javaTypeName;
            } else {
                useOpId = opId + javaTypeName;
            }
            if (tag == null) {
                useTag = javaTypeName;
            }
        }
        path = "inventory".equals(xmlRootElementName) ? ""
            : (path == null) ? "/" + xmlRootElementName : path + "/" + xmlRootElementName;
        XSDJavaType javaType = new XSDJavaType(javaTypeElement);
        if (getItemName != null) {
            if ("array".equals(getItemName)) {
                return javaType.getArrayType();
            } else {
                return javaType.getItemName();
            }
        }

        NodeList parentNodes = javaTypeElement.getElementsByTagName("java-attributes");
        if (parentNodes.getLength() == 0) {
            logger.debug("no java-attributes for java-type " + javaTypeName);
            return "";
        }

        String pathDescriptionProperty = javaType.getPathDescriptionProperty();
        String container = javaType.getContainerProperty();
        Vector<String> indexedProps = javaType.getIndexedProps();
        Vector<String> dslStartNodeProps = javaType.getDslStartNodeProps();
        Vector<String> containerProps = new Vector<String>();
        if (container != null) {
            logger.debug("javaTypeName " + javaTypeName + " container:" + container
                + " indexedProps:" + indexedProps);
        }

        Element parentElement = (Element) parentNodes.item(0);
        NodeList xmlElementNodes = parentElement.getElementsByTagName("xml-element");

        StringBuilder sbParameters = new StringBuilder();
        StringBuilder sbPathParameters = new StringBuilder(); // separate naming path parameters
                                                              // from name of parameter in the
                                                              // schema
        StringBuilder sbRequired = new StringBuilder();

        int requiredCnt = 0;
        int propertyCnt = 0;
        StringBuilder sbProperties = new StringBuilder();
        int patchPropertyCnt = 0; // manage payload properties separately for patch
        StringBuilder sbPropertiesPatch = new StringBuilder();

        if (appliedPaths.containsKey(path)) {
            return null;
        }

        StringTokenizer st = new StringTokenizer(path, "/");
        logger.debug("path: " + path + " st? " + st.toString());
        if (st.countTokens() > 1 && getItemName == null) {
            logger.debug("appliedPaths: " + appliedPaths + " containsKey? "
                + appliedPaths.containsKey(path));
            appliedPaths.put(path, xmlRootElementName);
        }

        Vector<String> addTypeV = null;
        String modifiedName;
        String replaceDescription;
        for (int i = 0; i < xmlElementNodes.getLength(); ++i) {
            XSDElement xmlElementElement = new XSDElement((Element) xmlElementNodes.item(i));
            if (!xmlElementElement.getParentNode().isSameNode(parentElement)) {
                continue;
            }
            String elementDescription = xmlElementElement.getPathDescriptionProperty();
            if (getItemName == null) {
                addTypeV = xmlElementElement.getAddTypes(v.toString());
            }
            // use alternate name for parameter if already in the path string
            modifiedName = "/{" + xmlElementElement.getAttribute("name") + "}";
            if (path.contains(modifiedName)) {
                modifiedName = path.substring(path.lastIndexOf('/') + 1) + "."
                    + xmlElementElement.getAttribute("name");
            } else {
                modifiedName = xmlElementElement.getAttribute("name");
            }
            if ("true".equals(xmlElementElement.getAttribute("xml-key"))) {
                path += "/{" + modifiedName + "}";
            }
            logger.debug("path: " + path);
            logger.debug("xmlElementElement.getAttribute(required):"
                + xmlElementElement.getAttribute("required"));

            if ("true".equals(xmlElementElement.getAttribute("xml-key"))) {
                sbParameters.append(xmlElementElement.getPathParamYAML(elementDescription));
                sbPathParameters
                    .append(xmlElementElement.getPathParamYAML(elementDescription, modifiedName));
            }
            if ("true".equals(xmlElementElement.getAttribute("required"))) {
                if (requiredCnt == 0) {
                    sbRequired.append("    required:\n");
                }
                ++requiredCnt;
                if (addTypeV == null || addTypeV.isEmpty()) {
                    sbRequired.append("    - ").append(xmlElementElement.getAttribute("name"))
                        .append("\n");
                } else {
                    for (int k = 0; k < addTypeV.size(); ++k) {
                        sbRequired.append("    - ")
                            .append(getXmlRootElementName(addTypeV.elementAt(k))).append(":\n");
                    }
                }
            }
            if (indexedProps != null
                && indexedProps.contains(xmlElementElement.getAttribute("name"))) {
                containerProps.add(xmlElementElement.getQueryParamYAML());
                GetOperation.addContainerProps(container, containerProps);
            }
            if (xmlElementElement.isStandardType()) {
                boolean isDslStartNode =
                    dslStartNodeProps.contains(xmlElementElement.getAttribute("name"));
                sbProperties.append(xmlElementElement.getTypePropertyYAML(isDslStartNode));
                if (!"resource-version".equals(xmlElementElement.getAttribute("name"))) {
                    sbPropertiesPatch.append(xmlElementElement.getTypePropertyYAML(isDslStartNode));
                    ++patchPropertyCnt;
                }
                ++propertyCnt;
            }

            StringBuilder newPathParams = new StringBuilder(
                (pathParams == null ? "" : pathParams.toString()) + sbPathParameters.toString());
            String useName;
            for (int k = 0; addTypeV != null && k < addTypeV.size(); ++k) {
                String addType = addTypeV.elementAt(k);
                namespaceFilter.add(getXmlRootElementName(addType));
                logger.debug("addType: " + addType);

                if (opId == null || !opId.contains(addType)) {
                    processJavaTypeElementSwagger(addType, getJavaTypeElementSwagger(addType),
                        pathSb, definitionsSb, path, tag == null ? useTag : tag, useOpId, null,
                        newPathParams, validEdges);
                }
                // need item name of array
                String itemName = processJavaTypeElementSwagger(addType,
                    getJavaTypeElementSwagger(addType), pathSb, definitionsSb, path,
                    tag == null ? useTag : tag, useOpId, "array", null, null);

                if (itemName != null) {
                    if (addType.equals("AaiInternal")) {
                        logger.debug("addType AaiInternal, skip properties");

                    } else if (getItemName == null) {
                        ++propertyCnt;
                        sbProperties.append("      ").append(getXmlRootElementName(addType))
                            .append(":\n");
                        if ("RelationshipList".equals(addType)) {
                            sbProperties.append("        type: object\n");
                            sbProperties.append("        $ref: \"#/definitions/").append(itemName)
                                .append("\"\n");
                            sbPropertiesPatch.append("      ")
                                .append(getXmlRootElementName(addType)).append(":\n");
                            sbPropertiesPatch.append("        type: object\n");
                            sbPropertiesPatch.append("        $ref: \"#/definitions/")
                                .append(itemName).append("\"\n");
                            ++patchPropertyCnt;
                        } else {
                            if ("relationship".equals(itemName)) {
                                System.out.println(
                                    v + "-relationship added as array for getItemName null");
                            }
                            sbProperties.append("        type: array\n        items:\n");
                            sbProperties.append("          $ref: \"#/definitions/")
                                .append("".equals(itemName) ? "inventory-item-data" : itemName)
                                .append("\"\n");
                        }
                        if (StringUtils.isNotEmpty(elementDescription)) {
                            sbProperties.append("        description: ").append(elementDescription)
                                .append("\n");
                        }
                    }
                } else {
                    if (("java.util.ArrayList")
                        .equals(xmlElementElement.getAttribute("container-type"))) {
                        // need properties for getXmlRootElementName(addType)
                        namespaceFilter.add(getXmlRootElementName(addType));
                        newPathParams = new StringBuilder(
                            (pathParams == null ? "" : pathParams.toString()) + sbParameters);
                        processJavaTypeElementSwagger(addType, getJavaTypeElementSwagger(addType),
                            pathSb, definitionsSb, path, tag == null ? useTag : tag, useOpId, null,
                            newPathParams, validEdges);
                        useName = getXmlRootElementName(addType);
                        sbProperties.append("      ").append(useName).append(":\n");
                        if ("relationship".equals(useName)) {
                            sbProperties.append("        type: object\n");
                            sbProperties.append("        $ref: \"#/definitions/relationship\"\n");
                            sbPropertiesPatch.append("        type: object\n");
                            sbPropertiesPatch
                                .append("        $ref: \"#/definitions/relationship\"\n");
                            ++patchPropertyCnt;
                        } else {
                            sbProperties.append("        type: array\n        items:          \n");
                            sbProperties.append("          $ref: \"#/definitions/"
                                + getXmlRootElementName(addType) + "\"\n");
                            if (StringUtils.isNotEmpty(elementDescription)) {
                                sbProperties.append("        description: ")
                                    .append(elementDescription).append("\n");
                            }
                        }

                    } else {
                        // Make sure certain types added to the filter don't appear
                        if (!nodeFilter.contains(getXmlRootElementName(addType))) {
                            sbProperties.append("      ").append(getXmlRootElementName(addType))
                                .append(":\n");
                            sbProperties.append("        type: object\n");
                            sbProperties.append("        $ref: \"#/definitions/")
                                .append(getXmlRootElementName(addType)).append("\"\n");
                        }
                    }
                    if (StringUtils.isNotEmpty(elementDescription)) {
                        sbProperties.append("        description: ").append(elementDescription)
                            .append("\n");
                    }
                    ++propertyCnt;
                }
            }
        }

        if (sbParameters.toString().length() > 0) {
            if (pathParams == null) {
                pathParams = new StringBuilder();
            }
            pathParams.append(sbPathParameters);
        }
        GetOperation get = new GetOperation(useOpId, xmlRootElementName, tag, path,
            pathParams == null ? "" : pathParams.toString());
        pathSb.append(get);
        logger.debug("opId vs useOpId:" + opId + " vs " + useOpId + " PathParams=" + pathParams);
        // add PUT
        PutOperation put = new PutOperation(useOpId, xmlRootElementName, tag, path,
            pathParams == null ? "" : pathParams.toString(), this.v, this.basePath);
        pathSb.append(put);
        // add PATCH
        PatchOperation patch = new PatchOperation(useOpId, xmlRootElementName, tag, path,
            pathParams == null ? "" : pathParams.toString(), this.v, this.basePath);
        patch.setPrefixForPatchRef(patchDefinePrefix);
        pathSb.append(patch);
        // add DELETE
        DeleteOperation del = new DeleteOperation(useOpId, xmlRootElementName, tag, path,
            pathParams == null ? "" : pathParams.toString());
        pathSb.append(del);
        if (generatedJavaType.containsKey(xmlRootElementName)) {
            logger.debug("xmlRootElementName(1)=" + xmlRootElementName);
            return null;
        }

        boolean processingInventoryDef = false;
        String dict = null;
        if (xmlRootElementName.equals("inventory")) {
            // inventory properties for each oxm to be concatenated
            processingInventoryDef = true;
            if (inventoryDefSb == null) {
                inventoryDefSb = new StringBuilder();
                definitionsSb.append("  ").append(xmlRootElementName).append(":\n");
                definitionsLocalSb.append("  ").append(xmlRootElementName).append(":\n");
                definitionsLocalSb.append("    properties:\n");
            }
        } else if (xmlRootElementName.equals("relationship")) {
            definitionsSb.append("  " + "relationship-dict" + ":\n");
            definitionsLocalSb.append("  " + "relationship-dict" + ":\n");
            dict = getDictionary(xmlRootElementName);
        } else {
            definitionsSb.append("  ").append(xmlRootElementName).append(":\n");
            definitionsLocalSb.append("  ").append(xmlRootElementName).append(":\n");
        }
        // Collection<EdgeDescription> edges = edgeRuleSet.getEdgeRules(xmlRootElementName );
        DeleteFootnoteSet footnotes = new DeleteFootnoteSet(xmlRootElementName);
        StringBuilder sbEdge = new StringBuilder();
        LinkedHashSet<String> preventDelete = new LinkedHashSet<String>();
        String prevent = null;
        String nodeCaption = "      ###### Related Nodes\n";
        try {
            EdgeRuleQuery q =
                new EdgeRuleQuery.Builder(xmlRootElementName).version(v).fromOnly().build();
            Multimap<String, EdgeRule> results = ei.getRules(q);
            SortedSet<String> ss = new TreeSet<String>(results.keySet());
            sbEdge.append(nodeCaption);
            nodeCaption = "";
            for (String key : ss) {
                results.get(key).stream()
                    .filter((i) -> (i.getFrom().equals(xmlRootElementName) && (!i.isPrivateEdge())))
                    .forEach((i) -> {
                        logger.info(new String(new StringBuilder("      - TO ").append(i.getTo())
                            .append(i.getDirection().toString()).append(i.getContains())));
                    });
                results.get(key).stream()
                    .filter((i) -> (i.getFrom().equals(xmlRootElementName) && (!i.isPrivateEdge())))
                    .forEach((i) -> {
                        sbEdge.append("      - TO ").append(i.getTo());
                        EdgeDescription ed = new EdgeDescription(i);
                        String footnote = ed.getAlsoDeleteFootnote(xmlRootElementName);
                        sbEdge.append(ed.getRelationshipDescription("TO", xmlRootElementName))
                            .append(footnote).append("\n");
                        if (StringUtils.isNotEmpty(footnote)) {
                            footnotes.add(footnote);
                        }
                    });
                results.get(key).stream()
                    .filter((i) -> (i.getFrom().equals(xmlRootElementName)
                        && (!i.isPrivateEdge() && i.getPreventDelete().equals("OUT"))))
                    .forEach((i) -> {
                        preventDelete.add(i.getTo().toUpperCase());
                    });
            }
        } catch (Exception e) {
            logger.debug("xmlRootElementName: " + xmlRootElementName + " from edge exception\n", e);
        }
        try {
            EdgeRuleQuery q1 =
                new EdgeRuleQuery.Builder(xmlRootElementName).version(v).toOnly().build();
            Multimap<String, EdgeRule> results = ei.getRules(q1);
            SortedSet<String> ss = new TreeSet<String>(results.keySet());
            sbEdge.append(nodeCaption);
            for (String key : ss) {
                results.get(key).stream()
                    .filter((i) -> (i.getTo().equals(xmlRootElementName) && (!i.isPrivateEdge())))
                    .forEach((i) -> {
                        sbEdge.append("      - FROM ").append(i.getFrom());
                        EdgeDescription ed = new EdgeDescription(i);
                        String footnote = ed.getAlsoDeleteFootnote(xmlRootElementName);
                        sbEdge.append(ed.getRelationshipDescription("FROM", xmlRootElementName))
                            .append(footnote).append("\n");
                        if (StringUtils.isNotEmpty(footnote)) {
                            footnotes.add(footnote);
                        }
                    });
                results.get(key).stream()
                    .filter((i) -> (i.getTo().equals(xmlRootElementName) && (!i.isPrivateEdge())))
                    .forEach((i) -> {
                        logger
                            .info(new String(new StringBuilder("      - FROM ").append(i.getFrom())
                                .append(i.getDirection().toString()).append(i.getContains())));
                    });
                results.get(key).stream()
                    .filter((i) -> (i.getTo().equals(xmlRootElementName)
                        && (!i.isPrivateEdge() && i.getPreventDelete().equals("IN"))))
                    .forEach((i) -> {
                        preventDelete.add(i.getFrom().toUpperCase());
                    });
            }
        } catch (Exception e) {
            logger.debug("xmlRootElementName: " + xmlRootElementName + " to edge exception\n", e);
        }
        if (preventDelete.size() > 0) {
            prevent = xmlRootElementName.toUpperCase() + " cannot be deleted if related to "
                + String.join(",", preventDelete);
            logger.debug(prevent);
        }

        if (StringUtils.isNotEmpty(prevent)) {
            footnotes.add(prevent);
        }
        if (footnotes.footnotes.size() > 0) {
            sbEdge.append(footnotes.toString());
        }
        validEdges = sbEdge.toString();

        // Handle description property. Might have a description OR valid edges OR both OR neither.
        // Only put a description: tag if there is at least one.
        if (StringUtils.isNotEmpty(pathDescriptionProperty) || StringUtils.isNotEmpty(validEdges)) {
            definitionsSb.append("    description: |\n");
            definitionsLocalSb.append("    description: |\n");

            if (pathDescriptionProperty != null) {
                definitionsSb.append("      ").append(pathDescriptionProperty).append("\n");
                definitionsLocalSb.append("      ").append(pathDescriptionProperty).append("\n");
            }
            if (StringUtils.isNotEmpty(validEdges)) {
                definitionsSb.append(validEdges);
                definitionsLocalSb.append(validEdges);
            }
        }
        if (patchPropertyCnt > 0) {
            definitionsLocalPatchSb.append("  " + patchDefinePrefix).append(xmlRootElementName)
                .append(":\n");
            if (StringUtils.isNotEmpty(pathDescriptionProperty)
                || StringUtils.isNotEmpty(validEdges)) {
                definitionsLocalPatchSb.append("    description: |\n");
            }
            if (pathDescriptionProperty != null) {
                definitionsLocalPatchSb.append("      ").append(pathDescriptionProperty)
                    .append("\n");
            }
            if (StringUtils.isNotEmpty(validEdges)) {
                definitionsLocalPatchSb.append(validEdges);
            }
            definitionsLocalPatchSb.append("    properties:\n");
        }

        if (requiredCnt > 0) {
            definitionsSb.append(sbRequired);
            definitionsLocalSb.append(sbRequired);
        }

        if (propertyCnt > 0) {
            definitionsSb.append("    properties:\n");
            definitionsSb.append(sbProperties);
            if (!processingInventoryDef) {
                definitionsLocalSb.append("    properties:\n");
            }
            definitionsLocalSb.append(sbProperties);
            definitionsLocalPatchSb.append(sbPropertiesPatch);
        }
        try {
            namespaceFilter.add(xmlRootElementName);
            if (xmlRootElementName.equals("inventory")) {
                // will add to javaTypeDefinitions at end
                inventoryDefSb.append(definitionsLocalSb.toString());
            } else if (xmlRootElementName.equals("relationship")) {
                javaTypeDefinitions.put(xmlRootElementName, dict);
                javaTypeDefinitions.put(xmlRootElementName + "-dict",
                    definitionsLocalSb.toString());
            } else {
                javaTypeDefinitions.put(xmlRootElementName, definitionsLocalSb.toString());
                if (!"relationship-list".equals(xmlRootElementName)) {
                    javaTypeDefinitions.put(patchDefinePrefix + xmlRootElementName,
                        definitionsLocalPatchSb.toString());
                }
            }
        } catch (Exception e) {
            logger.error("Exception adding in javaTypeDefinitions", e);
        }
        if (xmlRootElementName.equals("inventory")) {
            logger.trace("skip xmlRootElementName(2)=" + xmlRootElementName);
            return null;
        }
        generatedJavaType.put(xmlRootElementName, null);
        /*
         * if( validTag(javaTypeName) && javaTypeName == useTag && tag == null) {
         * String nameSpaceResult =
         * getDocumentHeader()+pathSb.toString()+appendDefinitions(namespaceFilter);
         * writeYAMLfile(javaTypeName, nameSpaceResult);
         * totalPathSbAccumulator.append(pathSb);
         * pathSb.delete(0, pathSb.length());
         * namespaceFilter.clear();
         * }
         */
        logger.trace("xmlRootElementName(2)=" + xmlRootElementName);
        return null;
    }

    private void writeYAMLfile(String outfileName, String fileContent) {
        outfileName = (StringUtils.isEmpty(outfileName)) ? "aai_swagger" : outfileName;
        outfileName = (outfileName.lastIndexOf(File.separator) == -1)
            ? yaml_dir + File.separator + outfileName + "_" + v.toString() + "." + generateTypeYAML
            : outfileName;
        File outfile = new File(outfileName);
        File parentDir = outfile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try {
            if (!outfile.createNewFile()) {
                logger.error("File {} already exist", outfileName);
            }
        } catch (IOException e) {
            logger.error("Exception creating output file " + outfileName, e);
        }
        try {
            Charset charset = StandardCharsets.UTF_8;
            Path path = Paths.get(outfileName);
            try (BufferedWriter bw = Files.newBufferedWriter(path, charset)) {
                bw.write(fileContent);
            }
        } catch (IOException e) {
            logger.error("Exception writing output file " + outfileName, e);
        }
    }

    public boolean validTag(String tag) {
        if (tag != null) {
            // set ignore to true to skip Actions and Search
            return checkTopLevel(tag, true);
        }
        return false;
    }

}
