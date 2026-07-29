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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.setup.SchemaConfigVersions;
import org.onap.aai.setup.SchemaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NodesYAMLfromOXM extends OxmFileProcessor {
    private static final Logger logger = LoggerFactory.getLogger("GenerateXsd.class");
    private static final String ROOT = "../aai-schema/src/main/resources";
    private static final String AUTO_GEN_ROOT = "aai-schema/src/main/resources";
    private static final String GENERATE_TYPE_YAML = "yaml";
    private static final String NORMAL_START_DIR = "aai-schema-gen";
    private static final String YAML_DIR = (((System.getProperty("user.dir") != null)
        && (!System.getProperty("user.dir").contains(NORMAL_START_DIR))) ? AUTO_GEN_ROOT : ROOT)
        + "/aai_swagger_yaml";
    private StringBuilder inventoryDefSb = null;
    private Map<String, String> operationDefinitions = new HashMap<>();

    private final String basePath;

    /**
     * Node-GET state for the version currently being generated. Replaced on every
     * {@link #process()} call, which is where the old code called
     * {@code NodeGetOperation.resetContainers()}.
     */
    private NodeGenerationContext nodeContext = new NodeGenerationContext();

    /**
     * The run-scoped state shared with {@link YAMLfromOXM}. This class emits no PUT operations of
     * its own, but it regenerates the relations files from the relationship paths that
     * {@link YAMLfromOXM} registered earlier in the run, so it must observe the same context.
     */
    private final GenerationContext context;

    public NodesYAMLfromOXM(String basePath, SchemaConfigVersions schemaConfigVersions,
        NodeIngestor ni, EdgeIngestor ei, GenerationContext context) {
        super(schemaConfigVersions, ni, ei);
        this.basePath = basePath;
        this.context = context;
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
            sb.append("\n    [Differences versus the previous schema version](" + "apidocs")
                .append(basePath).append("/aai_swagger_").append(v.toString()).append(".diff)");
        }
        sb.append(DOUBLE_LINE_SEPARATOR)
            .append("    This document is best viewed with Firefox or Chrome. ");
        sb.append(
            "Nodes can be found by opening the models link below and finding the node-type. ");
        sb.append("Edge definitions can be found with the node definitions.").append(LINE_SEPARATOR)
            .append("  version: \"").append(v.toString()).append("\"").append(LINE_SEPARATOR);
        sb.append("  title: Active and Available Inventory REST API").append(LINE_SEPARATOR);
        sb.append("  license:").append(LINE_SEPARATOR).append("    name: Apache 2.0")
            .append(LINE_SEPARATOR)
            .append("    url: http://www.apache.org/licenses/LICENSE-2.0.html")
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
        // a fresh context per version, replacing the former NodeGetOperation.resetContainers()
        nodeContext = new NodeGenerationContext();
        try {
            init();
        } catch (Exception e) {
            logger.error("Error initializing " + this.getClass());
            throw e;
        }
        pathSb.append(getDocumentHeader());
        YamlWriter definitions = new YamlWriter();
        Element elem;
        String javaTypeName;
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            elem = (Element) javaTypeNodes.item(i);
            javaTypeName = elem.getAttribute("name");
            boolean processInventory = false;
            if (!"Inventory".equals(javaTypeName)) {
                if (generatedJavaType.containsKey(javaTypeName)) {
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

            logger.debug("External: " + javaTypeElement.getAttribute("name") + "/"
                + getXmlRootElementName(javaTypeName));
            if (javaTypeName == null) {
                String msg = "Invalid OXM file: <java-type> has no name attribute in " + oxmFile;
                logger.error(msg);
                throw new SAXException(msg);
            }
            namespaceFilter.add(getXmlRootElementName(javaTypeName));
            processJavaTypeElementSwagger(javaTypeName, javaTypeElement.getElement(), pathSb,
                definitions, null, null, null, null);
        }
        sb.append(pathSb);
        // sb.append(getDocumentHeader());
        // sb.append(totalPathSbAccumulator);
        sb.append(appendOperations());
        sb.append(appendDefinitions());
        PutRelationPathSet prp = new PutRelationPathSet(v, context);
        prp.generateRelations(ei);
        return sb.toString();
    }

    public String appendDefinitions() {
        return appendDefinitions(null);
    }

    public String appendDefinitions(Set<String> namespaceFilter) {
        if (inventoryDefSb != null) {
            javaTypeDefinitions.put("inventory", inventoryDefSb.toString());
        }
        StringBuilder sb = new StringBuilder("definitions:\n");
        Map<String, String> sortedJavaTypeDefinitions = new TreeMap<>(javaTypeDefinitions);

        for (Map.Entry<String, String> entry : sortedJavaTypeDefinitions.entrySet()) {
            if (namespaceFilter != null && (!namespaceFilter.contains(entry.getKey()))) {
                continue;
            }
            logger.debug("Key: " + entry.getKey() + "Test: "
                + ("relationship-dict".equals(entry.getKey()) ? "true" : "false"));
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
        YamlWriter dictionary = new YamlWriter();
        dictionary.key(1, resource);
        dictionary.blockScalar(2, "description");
        dictionary.text(3, "dictionary of " + resource);
        dictionary.entry(2, "type", "object");
        dictionary.key(2, "properties");
        dictionary.key(3, resource);
        dictionary.entry(4, "type", "array");
        dictionary.key(4, "items");
        dictionary.entry(5, "$ref", "\"#/definitions/" + resource + "-dict\"");
        return dictionary.toString();
    }

    /**
     * Emits the node-GET operation and the schema definition for one java-type, recursing into the
     * types it references.
     *
     * <p>
     * The structure mirrors {@link YAMLfromOXM}: a {@link JavaTypeScope} holds what the recursion
     * accumulates and this method is the skeleton. What differs is what gets emitted - one GET per
     * node instead of the full CRUD set, no PATCH definitions, and path parameters that are not
     * inherited from the referencing type.
     */
    private void processJavaTypeElementSwagger(String javaTypeName, Element javaTypeElement,
        StringBuilder pathSb, YamlWriter definitions, String path, String tag, String opId,
        StringBuilder pathParams) {

        logger.debug("tag=" + tag);
        if (tag != null && !validTag(tag)) {
            logger.debug("tag=" + tag + "; javaTypeName=" + javaTypeName);
            return;
        }
        if ("AaiInternal".equals(javaTypeName)) {
            return;
        }
        NodeList parentNodes = javaTypeElement.getElementsByTagName("java-attributes");
        if (parentNodes.getLength() == 0) {
            logger.debug("no java-attributes for java-type " + javaTypeName);
            return;
        }

        JavaTypeScope scope = new JavaTypeScope(javaTypeName, javaTypeElement, path, tag, opId,
            pathParams, pathSb, definitions);
        if (appliedPaths.containsKey(scope.path)) {
            return;
        }
        StringTokenizer st = new StringTokenizer(scope.path, "/");
        logger.debug("path: " + scope.path + " st? " + st);
        if (st.countTokens() > 1) {
            logger.debug("appliedPaths: " + appliedPaths + " containsKey? "
                + appliedPaths.containsKey(scope.path));
            appliedPaths.put(scope.path, scope.xmlRootElementName);
        }

        scope.appendChildProperties((Element) parentNodes.item(0));
        scope.appendNodeGetOperation();

        if (generatedJavaType.containsKey(scope.xmlRootElementName)) {
            logger.debug("xmlRootElementName(1)=" + scope.xmlRootElementName);
            return;
        }
        scope.appendDefinition();
        scope.storeDefinition();
        if (scope.xmlRootElementName.equals("inventory")) {
            logger.trace("skip xmlRootElementName(2)=" + scope.xmlRootElementName);
            return;
        }
        generatedJavaType.put(scope.xmlRootElementName, null);
        logger.debug("xmlRootElementName(2)=" + scope.xmlRootElementName);
    }

    /**
     * The array-item name of a referenced java-type, i.e. the definition a referencing property
     * points its {@code $ref} at, or null when that type is not a collection or is not emitted at
     * all. See {@link YAMLfromOXM}'s counterpart; the only difference is the tag check.
     */
    private String getArrayItemName(String javaTypeName, Element javaTypeElement, String tag) {
        if (tag != null && !validTag(tag)) {
            logger.debug("tag=" + tag + "; javaTypeName=" + javaTypeName);
            return null;
        }
        if ("AaiInternal".equals(javaTypeName)) {
            return null;
        }
        return new XSDJavaType(javaTypeElement).getArrayType();
    }

    /**
     * The state of emitting one java-type: what the walk over its {@code xml-element} children
     * accumulates, plus the run-wide buffers that walk feeds. One instance per
     * {@link NodesYAMLfromOXM#processJavaTypeElementSwagger} invocation, so a referenced type gets
     * its own.
     */
    private final class JavaTypeScope {

        private final String xmlRootElementName;
        /** Inherited from the referencing type; both null at the top of the tree. */
        private final String tag;
        private final String opId;
        /**
         * What referenced types inherit as their tag and operation-id prefix. Both null for
         * {@code Inventory}, which contributes no path segment of its own; {@code useTag} is also
         * null once some ancestor has already set the tag.
         */
        private final String useTag;
        private final String useOpId;
        /** Extended by every child carrying {@code xml-key}, hence not final. */
        private String path;
        /**
         * Path parameters inherited from the referencing type. Unlike {@link YAMLfromOXM}, a
         * referenced type is generated with this type's own parameters only (cp8128), so child
         * nodes don't repeat the parameters of their parents.
         */
        private StringBuilder inheritedPathParams;
        private final StringBuilder pathSb;
        /** The run-wide definitions block, which every java-type appends its definition to. */
        private final YamlWriter definitions;

        private final String pathDescriptionProperty;
        private final String container;
        private final Vector<String> indexedProps;
        private final Vector<String> dslStartNodeProps;
        private final Vector<String> containerProps = new Vector<>();

        /** This type's own xml-key parameters. */
        private final StringBuilder parameters = new StringBuilder();
        private final DefinitionProperties definition = new DefinitionProperties();

        /** This type's definition body, before it is stored. */
        private final YamlWriter definitionsLocal = new YamlWriter(new StringBuilder(256));
        /** Only for {@code relationship}: the dictionary wrapper stored under the plain name. */
        private String dict;
        private boolean processingInventoryDef;
        /** The "Related Nodes" block, shared by the description emitter. */
        private String validEdges;

        private JavaTypeScope(String javaTypeName, Element javaTypeElement, String path, String tag,
            String opId, StringBuilder pathParams, StringBuilder pathSb, YamlWriter definitions) {
            this.xmlRootElementName = getXMLRootElementName(javaTypeElement);
            this.tag = tag;
            this.opId = opId;
            boolean isInventory = "Inventory".equals(javaTypeName);
            this.useOpId = isInventory ? null : (opId == null ? javaTypeName : opId + javaTypeName);
            this.useTag = (isInventory || tag != null) ? null : javaTypeName;
            this.path = "inventory".equals(xmlRootElementName) ? ""
                : (path == null) ? "/" + xmlRootElementName : path + "/" + xmlRootElementName;
            this.inheritedPathParams = pathParams;
            this.pathSb = pathSb;
            this.definitions = definitions;

            XSDJavaType javaType = new XSDJavaType(javaTypeElement);
            this.pathDescriptionProperty = javaType.getPathDescriptionProperty();
            this.container = javaType.getContainerProperty();
            this.indexedProps = javaType.getIndexedProps();
            this.dslStartNodeProps = javaType.getDslStartNodeProps();
            if (container != null) {
                logger.debug("javaTypeName " + javaTypeName + " container:" + container
                    + " indexedProps:" + indexedProps);
            }
        }

        /** The tag a referenced type inherits: ours if we have one, otherwise the one we set. */
        private String childTag() {
            return tag == null ? useTag : tag;
        }

        private void appendChildProperties(Element parentElement) {
            NodeList xmlElementNodes = parentElement.getElementsByTagName("xml-element");
            for (int i = 0; i < xmlElementNodes.getLength(); ++i) {
                XSDElement xmlElementElement = new XSDElement((Element) xmlElementNodes.item(i));
                // getElementsByTagName also finds grandchildren; those belong to a nested type
                if (!xmlElementElement.getParentNode().isSameNode(parentElement)) {
                    continue;
                }
                appendChildProperty(xmlElementElement);
            }
        }

        private void appendChildProperty(XSDElement xmlElementElement) {
            String name = xmlElementElement.getAttribute("name");
            String elementDescription = xmlElementElement.getPathDescriptionProperty();
            Vector<String> addTypeV = xmlElementElement.getAddTypes(v.toString());
            boolean isKey = "true".equals(xmlElementElement.getAttribute("xml-key"));
            if (isKey) {
                path += "/{" + name + "}";
            }
            logger.debug("path: " + path);
            logger.debug("xmlElementElement.getAttribute(required):"
                + xmlElementElement.getAttribute("required"));

            if ("true".equals(xmlElementElement.getAttribute("required"))) {
                appendRequired(name, addTypeV);
            }
            if (isKey) {
                parameters.append(xmlElementElement.getPathParamYAML(elementDescription));
            }
            if (indexedProps != null && indexedProps.contains(name)) {
                containerProps.add(xmlElementElement.getQueryParamYAML());
                nodeContext.addContainerProps(container, containerProps);
            }
            if (xmlElementElement.isStandardType()) {
                boolean isDslStartNode = dslStartNodeProps.contains(name);
                definition.properties.raw(xmlElementElement.getTypePropertyYAML(isDslStartNode));
                ++definition.propertyCount;
            }

            // cp8128 don't append the inherited pathParams so that child nodes don't contain the
            // parameters from parent
            StringBuilder newPathParams = new StringBuilder(parameters.toString());
            for (int k = 0; addTypeV != null && k < addTypeV.size(); ++k) {
                newPathParams = appendReferencedType(xmlElementElement, addTypeV.elementAt(k),
                    elementDescription, newPathParams);
            }
        }

        private void appendRequired(String name, Vector<String> addTypeV) {
            if (definition.requiredCount == 0) {
                definition.required.key(2, "required");
            }
            ++definition.requiredCount;
            if (addTypeV == null || addTypeV.isEmpty()) {
                definition.required.item(2, name);
            } else {
                for (int k = 0; k < addTypeV.size(); ++k) {
                    definition.required.item(2, getXmlRootElementName(addTypeV.elementAt(k)));
                }
            }
        }

        /**
         * Emits the property for one entry of a child's type list, and the referenced type's own
         * path and definition with it.
         *
         * @return the path parameters the next entry of the same list inherits
         */
        private StringBuilder appendReferencedType(XSDElement xmlElementElement, String addType,
            String elementDescription, StringBuilder newPathParams) {
            namespaceFilter.add(getXmlRootElementName(addType));

            if (opId == null || !opId.contains(addType)) {
                processJavaTypeElementSwagger(addType, getJavaTypeElementSwagger(addType), pathSb,
                    definitions, path, childTag(), useOpId, newPathParams);
            }
            // need item name of array
            String itemName =
                getArrayItemName(addType, getJavaTypeElementSwagger(addType), childTag());

            if (itemName != null) {
                if ("AaiInternal".equals(addType)) {
                    logger.debug("addType AaiInternal, skip properties");
                } else {
                    appendCollectionProperty(addType, itemName, elementDescription);
                }
                return newPathParams;
            }
            if ("java.util.ArrayList".equals(xmlElementElement.getAttribute("container-type"))) {
                // need properties for getXmlRootElementName(addType)
                namespaceFilter.add(getXmlRootElementName(addType));
                if (getXmlRootElementName(addType).equals("service-capabilities")) {
                    logger.info("arrays: " + getXmlRootElementName(addType));
                }
                // cp8128 - just use this type's own parameters, don't append the inherited ones
                newPathParams = new StringBuilder(parameters.toString());
                processJavaTypeElementSwagger(addType, getJavaTypeElementSwagger(addType), pathSb,
                    definitions, path, childTag(), useOpId, newPathParams);
                appendArrayProperty(addType, elementDescription);
            } else if (!nodeFilter.contains(getXmlRootElementName(addType))) {
                // Make sure certain types added to the filter don't appear
                definition.properties.key(3, getXmlRootElementName(addType));
                definition.properties.entry(4, "$ref",
                    "\"#/definitions/" + getXmlRootElementName(addType) + "\"");
            }
            if (StringUtils.isNotEmpty(elementDescription)) {
                definition.properties.entry(4, "description", elementDescription);
            }
            ++definition.propertyCount;
            return newPathParams;
        }

        /** A property that holds a collection of the referenced type, keyed by its item name. */
        private void appendCollectionProperty(String addType, String itemName,
            String elementDescription) {
            ++definition.propertyCount;
            definition.properties.key(3, getXmlRootElementName(addType));
            if ("RelationshipList".equals(addType)) {
                definition.properties.entry(4, "$ref", "\"#/definitions/" + itemName + "\"");
            } else {
                definition.properties.entry(4, "type", "object");
                definition.properties.key(4, "properties");
                definition.properties.key(5, itemName);
                definition.properties.entry(6, "type", "array");
                definition.properties.key(6, "items");
                definition.properties.entry(7, "$ref",
                    "\"#/definitions/" + ("".equals(itemName) ? "aai-internal" : itemName) + "\"");
            }
            if (StringUtils.isNotEmpty(elementDescription)) {
                definition.properties.entry(4, "description", elementDescription);
            }
        }

        /**
         * A property for a {@code java.util.ArrayList}-container child: an array of the referenced
         * type, or a plain {@code $ref} when that type is {@code relationship}.
         *
         * <p>
         * The description is emitted here and again by the caller, so such a property carries it
         * twice - long-standing output that the byte-identity constraint keeps in place.
         */
        private void appendArrayProperty(String addType, String elementDescription) {
            String useName = getXmlRootElementName(addType);
            definition.properties.key(3, useName);
            if ("relationship".equals(useName)) {
                definition.properties.entry(4, "$ref", "\"#/definitions/relationship\"");
            } else {
                definition.properties.entry(4, "type", "array");
                // the items key of an array property trails whitespace in the current documents, so
                // it is written as a line rather than as a key
                definition.properties.text(4, "items:" + " ".repeat(ITEMS_TRAILING_SPACES));
                definition.properties.entry(5, "$ref",
                    "\"#/definitions/" + getXmlRootElementName(addType) + "\"");
            }
            if (StringUtils.isNotEmpty(elementDescription)) {
                definition.properties.entry(4, "description", elementDescription);
            }
        }

        /**
         * Emits the node GET, keyed by node type rather than appended to {@code pathSb} - the
         * operations are sorted by {@link NodesYAMLfromOXM#appendOperations()} at the end of the
         * run.
         *
         * <p>
         * A node with no indexed properties and no container properties is queried without
         * parameters at all; otherwise the inherited path parameters apply.
         */
        private void appendNodeGetOperation() {
            if (parameters.length() > 0) {
                if (inheritedPathParams == null) {
                    inheritedPathParams = new StringBuilder();
                }
                inheritedPathParams.append(parameters);
            }
            boolean unparameterized =
                indexedProps != null && indexedProps.isEmpty() && containerProps.isEmpty();
            String params = unparameterized ? null
                : (inheritedPathParams == null ? "" : inheritedPathParams.toString());
            NodeGetOperation get =
                new NodeGetOperation(useOpId, xmlRootElementName, tag, path, params, nodeContext);
            String operation = get.toString();
            if (StringUtils.isNotEmpty(operation)) {
                operationDefinitions.put(xmlRootElementName, operation);
                // mirrors the original placement of the checklist update at the tail of toString()
                get.register();
            }
            logger.debug("opId vs useOpId:" + opId + " vs " + useOpId + " PathParams="
                + inheritedPathParams);
        }

        private void appendDefinition() {
            appendDefinitionHeader();
            validEdges = getRelatedNodesDescription(xmlRootElementName);
            appendDescription();
            appendRequiredAndProperties();
        }

        private void appendDefinitionHeader() {
            if (xmlRootElementName.equals("inventory")) {
                // inventory properties for each oxm to be concatenated
                processingInventoryDef = true;
                if (inventoryDefSb == null) {
                    inventoryDefSb = new StringBuilder();
                    definitions.key(1, xmlRootElementName);
                    definitionsLocal.key(1, xmlRootElementName);
                    definitionsLocal.key(2, "properties");
                }
            } else if (xmlRootElementName.equals("relationship")) {
                // a relationship is stored under the dictionary name; getDictionary() writes the
                // wrapper that points back at this definition
                definitions.key(1, "relationship-dict");
                definitionsLocal.key(1, "relationship-dict");
                dict = getDictionary(xmlRootElementName);
            } else {
                definitions.key(1, xmlRootElementName);
                definitionsLocal.key(1, xmlRootElementName);
            }
        }

        /**
         * Might have a description OR valid edges OR both OR neither: only open a
         * {@code description:} tag if there is at least one.
         */
        private void appendDescription() {
            if (StringUtils.isEmpty(pathDescriptionProperty) && StringUtils.isEmpty(validEdges)) {
                return;
            }
            definitions.blockScalar(2, "description");
            definitionsLocal.blockScalar(2, "description");
            if (pathDescriptionProperty != null) {
                definitions.text(3, pathDescriptionProperty);
                definitionsLocal.text(3, pathDescriptionProperty);
            }
            definitions.raw(validEdges);
            definitionsLocal.raw(validEdges);
        }

        private void appendRequiredAndProperties() {
            if (definition.requiredCount > 0) {
                definitions.raw(definition.required.toString());
                definitionsLocal.raw(definition.required.toString());
            }
            if (definition.propertyCount > 0) {
                definitions.key(2, "properties");
                definitions.raw(definition.properties.toString());
                if (!processingInventoryDef) {
                    definitionsLocal.key(2, "properties");
                }
                definitionsLocal.raw(definition.properties.toString());
            }
        }

        private void storeDefinition() {
            try {
                namespaceFilter.add(xmlRootElementName);
                if (xmlRootElementName.equals("inventory")) {
                    // will add to javaTypeDefinitions at end
                    inventoryDefSb.append(definitionsLocal);
                } else if (xmlRootElementName.equals("relationship")) {
                    javaTypeDefinitions.put(xmlRootElementName, dict);
                    javaTypeDefinitions.put(xmlRootElementName + "-dict",
                        definitionsLocal.toString());
                } else {
                    javaTypeDefinitions.put(xmlRootElementName, definitionsLocal.toString());
                }
            } catch (Exception e) {
                logger.trace("Exception during javaTypeDefinitions :", e);
            }
        }
    }

    private void writeYAMLfile(String outfileName, String fileContent) {
        outfileName = (StringUtils.isEmpty(outfileName)) ? "aai_swagger" : outfileName;
        outfileName = (outfileName.lastIndexOf(File.separator) == -1) ? YAML_DIR + File.separator
            + outfileName + "_" + v.toString() + "." + GENERATE_TYPE_YAML : outfileName;
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
        Path path = Path.of(outfileName);
        Charset charset = StandardCharsets.UTF_8;
        try (BufferedWriter bw = Files.newBufferedWriter(path, charset)) {
            bw.write(fileContent);
        } catch (IOException e) {
            logger.error("Exception writing output file " + outfileName, e);
        }
    }

    public boolean validTag(String tag) {
        if (tag != null) {
            // refactored to support top level paths from the schema file, set the ignore
            // parameter to false allows the logic to match all top level paths, including
            // Search and Actions, as hard-coded prior to refactoring
            return checkTopLevel(tag, false);
        }
        return false;
    }

    public String appendOperations() {
        // append definitions
        StringBuilder sb = new StringBuilder();
        Map<String, String> sortedOperationDefinitions =
            new TreeMap<String, String>(operationDefinitions);
        for (Map.Entry<String, String> entry : sortedOperationDefinitions.entrySet()) {
            sb.append(entry.getValue());
        }
        return sb.toString();
    }
}
