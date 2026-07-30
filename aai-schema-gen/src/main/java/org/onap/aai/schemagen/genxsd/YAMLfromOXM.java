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
import org.onap.aai.schemagen.yaml.YamlBlock;
import org.onap.aai.schemagen.yaml.YamlMapping;
import org.onap.aai.schemagen.yaml.YamlSerializer;
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

    /**
     * State shared with the operation emitters. Deliberately spans every version generated in a
     * run; see {@link GenerationContext} for why narrowing its lifetime would change the output.
     */
    private final GenerationContext context;

    public YAMLfromOXM(String basePath, SchemaConfigVersions schemaConfigVersions, NodeIngestor ni,
        EdgeIngestor ei, GenerationContext context) {
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
            processJavaTypeElementSwagger(javaTypeName, javaTypeElement.getElement(), pathSb, null,
                null, null, null);
        }
        sb.append(pathSb);

        sb.append(appendDefinitions());
        PutRelationPathSet prp = new PutRelationPathSet(v, context);
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

    /**
     * The wrapper definition stored under a resource's plain name. Its single property is an array
     * of the {@code -dict} definition, which holds the actual schema.
     */
    private String getDictionary(String resource) {
        YamlMapping collection = new YamlMapping().entry("type", "array").entry("items",
            new YamlMapping().entry("$ref", "\"#/definitions/" + resource + "-dict\""));
        YamlMapping dictionary = new YamlMapping().entry(resource,
            new YamlMapping().entry("type", "object")
                .entry("description", new YamlBlock().line("dictionary of " + resource))
                .entry("properties", new YamlMapping().entry(resource, collection)));
        // one entry of the document's definitions block, which sits one level in
        return YamlSerializer.serialize(dictionary, 1);
    }

    /**
     * Emits the paths and the schema definition for one java-type, recursing into the types it
     * references.
     *
     * <p>
     * Everything the recursion accumulates lives in a {@link JavaTypeScope}; this method is the
     * skeleton - guard, register the path as taken, walk the children, emit operations, emit the
     * definition - and each step is a method on that scope.
     */
    private void processJavaTypeElementSwagger(String javaTypeName, Element javaTypeElement,
        StringBuilder pathSb, String path, String tag, String opId, StringBuilder pathParams) {

        logger.debug("tag=" + tag);
        // ignoreActionsSearch=true: Actions and Search are handled as if not top level
        if (tag != null && !checkTopLevel(tag, true)) {
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

        JavaTypeScope scope =
            new JavaTypeScope(javaTypeName, javaTypeElement, path, tag, opId, pathParams, pathSb);
        if (appliedPaths.containsKey(scope.path)) {
            return;
        }
        StringTokenizer st = new StringTokenizer(scope.path, "/");
        logger.debug("path: " + scope.path + " st? " + st.toString());
        if (st.countTokens() > 1) {
            logger.debug("appliedPaths: " + appliedPaths + " containsKey? "
                + appliedPaths.containsKey(scope.path));
            appliedPaths.put(scope.path, scope.xmlRootElementName);
        }

        scope.appendChildProperties((Element) parentNodes.item(0));
        scope.appendOperations();

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
        logger.trace("xmlRootElementName(2)=" + scope.xmlRootElementName);
    }

    /**
     * The array-item name of a referenced java-type, i.e. the definition a referencing property
     * points its {@code $ref} at. A type that is not a collection has none, and the caller then
     * emits the reference inline instead.
     *
     * <p>
     * Null also stands for "this type is not emitted at all", for the same two reasons that stop
     * {@link #processJavaTypeElementSwagger}: a tag that is not a top-level path, and
     * {@code AaiInternal}.
     */
    private String getArrayItemName(String javaTypeName, Element javaTypeElement, String tag) {
        if (tag != null && !checkTopLevel(tag, true)) {
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
     * {@link YAMLfromOXM#processJavaTypeElementSwagger} invocation, so a referenced type gets its
     * own.
     *
     * <p>
     * Non-static on purpose: the emission steps reach the generator's schema lookups and its
     * run-wide accumulators directly, which is what lets each of them take almost no arguments.
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
         * Path parameters inherited from the referencing type. The operations are emitted with
         * these plus this type's own, which is why the walk appends into it rather than replacing
         * it.
         */
        private StringBuilder inheritedPathParams;
        private final StringBuilder pathSb;

        private final String pathDescriptionProperty;
        private final String container;
        private final Vector<String> indexedProps;
        private final Vector<String> dslStartNodeProps;
        private final Vector<String> containerProps = new Vector<>();

        /** This type's own xml-key parameters, named after the schema property. */
        private final StringBuilder parameters = new StringBuilder();
        /**
         * The same parameters named for their place in the path: a child whose name already appears
         * in the path is qualified with the segment above it.
         */
        private final StringBuilder pathParameters = new StringBuilder();
        private final DefinitionProperties definition = new DefinitionProperties();

        /** This type's definition body, and its PATCH flavour, before they are stored. */
        private final YamlMapping definitionBody = new YamlMapping();
        private final YamlMapping patchDefinitionBody = new YamlMapping();
        /** Only for {@code relationship}: the dictionary wrapper stored under the plain name. */
        private String dict;
        /**
         * The name this definition is stored under, which is the schema name except for
         * {@code relationship}, whose body is the dictionary entry.
         */
        private String definitionName;
        /** The "Related Nodes" block; read by three of the description emitters. */
        private String validEdges;

        private JavaTypeScope(String javaTypeName, Element javaTypeElement, String path, String tag,
            String opId, StringBuilder pathParams, StringBuilder pathSb) {
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
            // use alternate name for parameter if already in the path string
            String modifiedName = path.contains("/{" + name + "}")
                ? path.substring(path.lastIndexOf('/') + 1) + "." + name
                : name;
            if (isKey) {
                path += "/{" + modifiedName + "}";
            }
            logger.debug("path: " + path);
            logger.debug("xmlElementElement.getAttribute(required):"
                + xmlElementElement.getAttribute("required"));

            if (isKey) {
                parameters.append(xmlElementElement.getPathParamYAML(elementDescription));
                pathParameters
                    .append(xmlElementElement.getPathParamYAML(elementDescription, modifiedName));
            }
            if ("true".equals(xmlElementElement.getAttribute("required"))) {
                appendRequired(name, addTypeV);
            }
            if (indexedProps != null && indexedProps.contains(name)) {
                containerProps.add(xmlElementElement.getQueryParamYAML());
                context.addContainerProps(container, containerProps);
            }
            if (xmlElementElement.isStandardType()) {
                appendStandardTypeProperty(xmlElementElement, name);
            }

            StringBuilder newPathParams = new StringBuilder(
                (inheritedPathParams == null ? "" : inheritedPathParams.toString())
                    + pathParameters.toString());
            for (int k = 0; addTypeV != null && k < addTypeV.size(); ++k) {
                newPathParams = appendReferencedType(xmlElementElement, addTypeV.elementAt(k),
                    elementDescription, newPathParams);
            }
        }

        private void appendRequired(String name, Vector<String> addTypeV) {
            if (addTypeV == null || addTypeV.isEmpty()) {
                definition.required.item(name);
            } else {
                for (int k = 0; k < addTypeV.size(); ++k) {
                    definition.required.item(getXmlRootElementName(addTypeV.elementAt(k)));
                }
            }
        }

        private void appendStandardTypeProperty(XSDElement xmlElementElement, String name) {
            boolean isDslStartNode = dslStartNodeProps.contains(name);
            xmlElementElement.addTypeProperty(definition.properties, isDslStartNode);
            // resource-version is server-owned, so it is not part of a PATCH payload
            if (!"resource-version".equals(name)) {
                xmlElementElement.addTypeProperty(definition.patchProperties, isDslStartNode);
            }
        }

        /**
         * Emits the property for one entry of a child's type list, and the referenced type's own
         * paths and definition with it.
         *
         * @return the path parameters the next entry of the same list inherits. The
         *         ArrayList-container branch replaces them, and the original code let that
         *         replacement carry over to the entries after it.
         */
        private StringBuilder appendReferencedType(XSDElement xmlElementElement, String addType,
            String elementDescription, StringBuilder newPathParams) {
            namespaceFilter.add(getXmlRootElementName(addType));
            logger.debug("addType: " + addType);

            if (opId == null || !opId.contains(addType)) {
                processJavaTypeElementSwagger(addType, getJavaTypeElementSwagger(addType), pathSb,
                    path, childTag(), useOpId, newPathParams);
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
                newPathParams = new StringBuilder(
                    (inheritedPathParams == null ? "" : inheritedPathParams.toString())
                        + parameters);
                processJavaTypeElementSwagger(addType, getJavaTypeElementSwagger(addType), pathSb,
                    path, childTag(), useOpId, newPathParams);
                appendArrayProperty(addType, elementDescription);
            } else if (!nodeFilter.contains(getXmlRootElementName(addType))) {
                // Make sure certain types added to the filter don't appear
                appendReferenceProperty(getXmlRootElementName(addType), elementDescription);
            }
            return newPathParams;
        }

        /** A property that is just a reference to the definition of the type it holds. */
        private void appendReferenceProperty(String useName, String elementDescription) {
            YamlMapping property =
                new YamlMapping().entry("$ref", "\"#/definitions/" + useName + "\"");
            definition.properties.entry(useName, property);
            describe(property, elementDescription);
        }

        /** A property that holds a collection of the referenced type, keyed by its item name. */
        private void appendCollectionProperty(String addType, String itemName,
            String elementDescription) {
            String useName = getXmlRootElementName(addType);
            YamlMapping property = new YamlMapping();
            definition.properties.entry(useName, property);
            if ("RelationshipList".equals(addType)) {
                property.entry("$ref", "\"#/definitions/" + itemName + "\"");
                // the only property a PATCH payload carries besides its own scalars
                definition.patchProperties.entry(useName,
                    new YamlMapping().entry("$ref", "\"#/definitions/" + itemName + "\""));
            } else {
                if ("relationship".equals(itemName)) {
                    System.out.println(v + "-relationship added as array for getItemName null");
                }
                String definitionName = "".equals(itemName) ? "inventory-item-data" : itemName;
                YamlMapping collection = new YamlMapping().entry("type", "array").entry("items",
                    new YamlMapping().entry("$ref", "\"#/definitions/" + definitionName + "\""));
                property.entry("type", "object").entry("properties",
                    new YamlMapping().entry(itemName, collection));
            }
            describe(property, elementDescription);
        }

        /**
         * A property for a {@code java.util.ArrayList}-container child: an array of the referenced
         * type, or a plain {@code $ref} when that type is {@code relationship}.
         */
        private void appendArrayProperty(String addType, String elementDescription) {
            String useName = getXmlRootElementName(addType);
            YamlMapping property = new YamlMapping();
            definition.properties.entry(useName, property);
            if ("relationship".equals(useName)) {
                property.entry("$ref", "\"#/definitions/relationship\"");
                return;
            }
            YamlMapping items =
                new YamlMapping().entry("$ref", "\"#/definitions/" + useName + "\"");
            property.entry("type", "array").entryWithPaddedKey("items", items, ITEMS_KEY_PADDING);
            describe(property, elementDescription);
        }

        /**
         * Adds a property's description, if it has one.
         *
         * <p>
         * Only the properties that carry a description reach this; the ones that cannot - a
         * reference
         * to a type in the node filter, an array of a referenced type - have none in any version's
         * OXM, which is why the original code could append the description outside the branch that
         * opened the property and still produce a well-formed document.
         */
        private void describe(YamlMapping property, String elementDescription) {
            if (StringUtils.isNotEmpty(elementDescription)) {
                property.entry("description", elementDescription);
            }
        }

        private void appendOperations() {
            if (parameters.length() > 0) {
                if (inheritedPathParams == null) {
                    inheritedPathParams = new StringBuilder();
                }
                inheritedPathParams.append(pathParameters);
            }
            String params = inheritedPathParams == null ? "" : inheritedPathParams.toString();
            pathSb
                .append(new GetOperation(useOpId, xmlRootElementName, tag, path, params, context));
            logger.debug("opId vs useOpId:" + opId + " vs " + useOpId + " PathParams="
                + inheritedPathParams);
            // add PUT
            PutOperation put =
                new PutOperation(useOpId, xmlRootElementName, tag, path, params, v, basePath);
            String putStr = put.toString();
            pathSb.append(putStr);
            // register the relationship path only when the operation was actually emitted,
            // mirroring
            // the original placement of the registration at the tail of PutOperation.toString()
            if (!putStr.isEmpty()) {
                put.register(context);
            }
            // add PATCH
            PatchOperation patch =
                new PatchOperation(useOpId, xmlRootElementName, tag, path, params, v, basePath);
            patch.setPrefixForPatchRef(patchDefinePrefix);
            pathSb.append(patch);
            // add DELETE
            DeleteOperation del =
                new DeleteOperation(useOpId, xmlRootElementName, tag, path, params);
            String delStr = del.toString();
            pathSb.append(delStr);
            // register the delete path only when the operation was actually emitted, mirroring the
            // original placement of the registration at the tail of DeleteOperation.toString()
            if (!delStr.isEmpty()) {
                del.register(context);
            }
        }

        private void appendDefinition() {
            appendDefinitionHeader();
            validEdges = getRelatedNodesDescription(xmlRootElementName);
            appendDescription();
            appendRequiredAndProperties();
        }

        /** Decides the name this definition is stored under, and what precedes its description. */
        private void appendDefinitionHeader() {
            if (xmlRootElementName.equals("inventory")) {
                // inventory properties are accumulated separately and added by appendDefinitions()
                definitionName = xmlRootElementName;
                if (inventoryDefSb == null) {
                    inventoryDefSb = new StringBuilder();
                }
            } else if (xmlRootElementName.equals("relationship")) {
                // a relationship is stored under the dictionary name; getDictionary() builds the
                // wrapper that points back at this definition
                definitionName = "relationship-dict";
                definitionBody.entry("type", "object");
                dict = getDictionary(xmlRootElementName);
            } else {
                definitionName = xmlRootElementName;
            }
        }

        /**
         * Might have a description OR valid edges OR both OR neither: only open a
         * {@code description:} tag if there is at least one.
         *
         * <p>
         * The same block goes into the PATCH flavour of the definition, which describes the same
         * node.
         */
        private void appendDescription() {
            if (!hasDescription()) {
                return;
            }
            YamlBlock description = new YamlBlock();
            if (pathDescriptionProperty != null) {
                description.line(pathDescriptionProperty);
            }
            if (StringUtils.isNotEmpty(validEdges)) {
                description.trailingRaw(validEdges);
            }
            definitionBody.entry("description", description);
            if (hasPatchDefinition()) {
                patchDefinitionBody.entry("description", description);
            }
        }

        private boolean hasDescription() {
            return StringUtils.isNotEmpty(pathDescriptionProperty)
                || StringUtils.isNotEmpty(validEdges);
        }

        /** A node with nothing patchable gets no {@code zzzz-patch-} definition at all. */
        private boolean hasPatchDefinition() {
            return !definition.patchProperties.isEmpty();
        }

        private void appendRequiredAndProperties() {
            if (!definition.required.isEmpty()) {
                // the "-" indicators of a required list are aligned with its key, not indented
                // past it
                definitionBody.entryAtShiftedDepth("required", definition.required, -1);
            }
            if (!definition.properties.isEmpty()) {
                definitionBody.entry("properties", definition.properties);
            }
            if (hasPatchDefinition()) {
                patchDefinitionBody.entry("properties", definition.patchProperties);
            }
        }

        private void storeDefinition() {
            try {
                namespaceFilter.add(xmlRootElementName);
                String body = serializeDefinition(definitionName, definitionBody);
                if (xmlRootElementName.equals("inventory")) {
                    // will add to javaTypeDefinitions at end
                    inventoryDefSb.append(body);
                } else if (xmlRootElementName.equals("relationship")) {
                    javaTypeDefinitions.put(xmlRootElementName, dict);
                    javaTypeDefinitions.put(xmlRootElementName + "-dict", body);
                } else {
                    javaTypeDefinitions.put(xmlRootElementName, body);
                    if (!"relationship-list".equals(xmlRootElementName)) {
                        javaTypeDefinitions.put(patchDefinePrefix + xmlRootElementName,
                            serializeDefinition(patchDefinePrefix + xmlRootElementName,
                                patchDefinitionBody));
                    }
                }
            } catch (Exception e) {
                logger.error("Exception adding in javaTypeDefinitions", e);
            }
        }

        /** One entry of the document's definitions block, which sits one level in. */
        private String serializeDefinition(String name, YamlMapping body) {
            if (body.isEmpty()) {
                return "";
            }
            return YamlSerializer.serialize(new YamlMapping().entry(name, body), 1);
        }
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
            Path path = Path.of(outfileName);
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
