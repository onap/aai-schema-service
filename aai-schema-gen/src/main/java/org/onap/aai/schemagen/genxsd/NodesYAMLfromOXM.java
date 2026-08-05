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

import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

/**
 * Builds the nodes swagger document for one schema version: one {@code /nodes/...} GET per node
 * type, reachable by node type rather than by its place in the inventory tree, alongside the same
 * definitions the CRUD document carries.
 */
public class NodesYAMLfromOXM extends OxmFileProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NodesYAMLfromOXM.class);

    /**
     * The endpoints, keyed by node type rather than by path: they are published in node-type order
     * rather than in the order the walk over the OXM reaches them.
     */
    private Map<String, Path> operations = new TreeMap<>();
    /** The path each node type's endpoint is published under, which is not its CRUD path. */
    private Map<String, String> pathKeys = new LinkedHashMap<>();

    private final String basePath;

    /**
     * Node-GET state for the version currently being generated. Replaced on every
     * {@link #process()}
     * call, which is where the old code called {@code NodeGetOperation.resetContainers()}.
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

    protected void init() throws ParserConfigurationException, SAXException, IOException,
        FileNotFoundException, EdgeRuleNotFoundException {
        super.init();
    }

    @Override
    public String process() throws ParserConfigurationException, SAXException, IOException,
        FileNotFoundException, EdgeRuleNotFoundException {
        // a fresh context per version, replacing the former NodeGetOperation.resetContainers()
        nodeContext = new NodeGenerationContext();
        try {
            init();
        } catch (Exception e) {
            logger.error("Error initializing " + this.getClass());
            throw e;
        }
        operations = new TreeMap<>();
        pathKeys = new LinkedHashMap<>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            Element elem = (Element) javaTypeNodes.item(i);
            String javaTypeName = elem.getAttribute("name");
            boolean processInventory = "Inventory".equals(javaTypeName);
            if (!processInventory) {
                if (generatedJavaType.containsKey(javaTypeName)) {
                    continue;
                }
                // will combine all matching java-types
                elem = getJavaTypeElementSwagger(javaTypeName);
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
            processJavaTypeElementSwagger(javaTypeName, javaTypeElement.getElement(), null, null,
                null, null);
        }

        Swagger document = newDocument(basePath);
        document.setPaths(paths());
        document.setDefinitions(definitions());
        new PutRelationPathSet(v, context).generateRelations(ei);
        return LICENSE_PREFIX + SwaggerWriter.toYaml(document);
    }

    /** The endpoints in the order they are published: by node type, not by path. */
    public Map<String, Path> paths() {
        Map<String, Path> paths = new LinkedHashMap<>();
        operations.forEach((nodeType, endpoint) -> paths.put(pathKeys.get(nodeType), endpoint));
        return paths;
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
        String path, String tag, String opId, List<Parameter> pathParams) {

        logger.debug("tag=" + tag);
        if (tag != null && !validTag(tag)) {
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
            new JavaTypeScope(javaTypeName, javaTypeElement, path, tag, opId, pathParams);
        if (appliedPaths.containsKey(scope.path)) {
            return;
        }
        StringTokenizer st = new StringTokenizer(scope.path, "/");
        if (st.countTokens() > 1) {
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
        if ("inventory".equals(scope.xmlRootElementName)) {
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
            return null;
        }
        if ("AaiInternal".equals(javaTypeName)) {
            return null;
        }
        return new XSDJavaType(javaTypeElement).getArrayType();
    }

    /**
     * The state of emitting one java-type: what the walk over its {@code xml-element} children
     * accumulates. One instance per {@link NodesYAMLfromOXM#processJavaTypeElementSwagger}
     * invocation, so a referenced type gets its own.
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
        private List<Parameter> inheritedPathParams;

        private final String pathDescriptionProperty;
        private final String container;
        private final Vector<String> indexedProps;
        private final Vector<String> dslStartNodeProps;
        private final List<Parameter> containerProps = new ArrayList<>();

        /** This type's own xml-key parameters. */
        private final List<Parameter> parameters = new ArrayList<>();

        /** This type's definition, before it is stored. */
        private final ModelImpl definition = new ModelImpl();
        /** In OXM declaration order, which is not the order the property names sort in. */
        private final List<String> required = new ArrayList<>();
        /** Only for {@code relationship}: the dictionary wrapper stored under the plain name. */
        private ModelImpl dict;
        /** The "Related Nodes" block, read by the description emitter. */
        private String validEdges;

        private JavaTypeScope(String javaTypeName, Element javaTypeElement, String path, String tag,
            String opId, List<Parameter> pathParams) {
            this.xmlRootElementName = getXMLRootElementName(javaTypeElement);
            this.tag = tag;
            this.opId = opId;
            boolean isInventory = "Inventory".equals(javaTypeName);
            this.useOpId = isInventory ? null : (opId == null ? javaTypeName : opId + javaTypeName);
            this.useTag = (isInventory || tag != null) ? null : javaTypeName;
            this.path = "inventory".equals(xmlRootElementName) ? ""
                : (path == null) ? "/" + xmlRootElementName : path + "/" + xmlRootElementName;
            this.inheritedPathParams = pathParams;

            XSDJavaType javaType = new XSDJavaType(javaTypeElement);
            this.pathDescriptionProperty = javaType.getPathDescriptionProperty();
            this.container = javaType.getContainerProperty();
            this.indexedProps = javaType.getIndexedProps();
            this.dslStartNodeProps = javaType.getDslStartNodeProps();
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

            if ("true".equals(xmlElementElement.getAttribute("required"))) {
                appendRequired(name, addTypeV);
            }
            if (isKey) {
                parameters.add(xmlElementElement.getPathParameter(elementDescription));
            }
            if (indexedProps != null && indexedProps.contains(name)) {
                containerProps.add(xmlElementElement.getQueryParameter());
                nodeContext.addContainerProps(container, containerProps);
            }
            if (xmlElementElement.isStandardType()) {
                boolean isDslStartNode = dslStartNodeProps.contains(name);
                definition.addProperty(name, xmlElementElement.getTypeProperty(isDslStartNode));
            }

            // cp8128 don't append the inherited pathParams so that child nodes don't contain the
            // parameters from parent
            List<Parameter> newPathParams = List.copyOf(parameters);
            for (int k = 0; addTypeV != null && k < addTypeV.size(); ++k) {
                newPathParams = appendReferencedType(xmlElementElement, addTypeV.elementAt(k),
                    elementDescription, newPathParams);
            }
        }

        private void appendRequired(String name, Vector<String> addTypeV) {
            if (addTypeV == null || addTypeV.isEmpty()) {
                required.add(name);
            } else {
                for (int k = 0; k < addTypeV.size(); ++k) {
                    required.add(getXmlRootElementName(addTypeV.elementAt(k)));
                }
            }
        }

        /**
         * Emits the property for one entry of a child's type list, and the referenced type's own
         * path and definition with it.
         *
         * @return the path parameters the next entry of the same list inherits
         */
        private List<Parameter> appendReferencedType(XSDElement xmlElementElement, String addType,
            String elementDescription, List<Parameter> newPathParams) {
            namespaceFilter.add(getXmlRootElementName(addType));

            if (opId == null || !opId.contains(addType)) {
                processJavaTypeElementSwagger(addType, getJavaTypeElementSwagger(addType), path,
                    childTag(), useOpId, newPathParams);
            }
            // need item name of array
            String itemName =
                getArrayItemName(addType, getJavaTypeElementSwagger(addType), childTag());

            if (itemName != null) {
                if ("AaiInternal".equals(addType)) {
                    logger.debug("addType AaiInternal, skip properties");
                } else {
                    describe(appendCollectionProperty(addType, itemName), elementDescription);
                }
                return newPathParams;
            }
            Property property = null;
            if ("java.util.ArrayList".equals(xmlElementElement.getAttribute("container-type"))) {
                // need properties for getXmlRootElementName(addType)
                namespaceFilter.add(getXmlRootElementName(addType));
                // cp8128 - just use this type's own parameters, don't append the inherited ones
                newPathParams = List.copyOf(parameters);
                processJavaTypeElementSwagger(addType, getJavaTypeElementSwagger(addType), path,
                    childTag(), useOpId, newPathParams);
                property = appendArrayProperty(addType);
            } else if (!nodeFilter.contains(getXmlRootElementName(addType))) {
                // Make sure certain types added to the filter don't appear
                property = putProperty(getXmlRootElementName(addType),
                    new RefProperty(getXmlRootElementName(addType)));
            }
            describe(property, elementDescription);
            return newPathParams;
        }

        /** A property that holds a collection of the referenced type, keyed by its item name. */
        private Property appendCollectionProperty(String addType, String itemName) {
            String useName = getXmlRootElementName(addType);
            if ("RelationshipList".equals(addType)) {
                return putProperty(useName, new RefProperty(itemName));
            }
            ObjectProperty wrapper = new ObjectProperty();
            Map<String, Property> members = new LinkedHashMap<>();
            members.put(itemName, new ArrayProperty(
                new RefProperty("".equals(itemName) ? "aai-internal" : itemName)));
            // setProperties, not property(): the latter would sort the members
            wrapper.setProperties(members);
            return putProperty(useName, wrapper);
        }

        /**
         * A property for a {@code java.util.ArrayList}-container child: an array of the referenced
         * type, or a plain {@code $ref} when that type is {@code relationship}.
         */
        private Property appendArrayProperty(String addType) {
            String useName = getXmlRootElementName(addType);
            if ("relationship".equals(useName)) {
                return putProperty(useName, new RefProperty(useName));
            }
            return putProperty(useName, new ArrayProperty(new RefProperty(useName)));
        }

        private Property putProperty(String name, Property property) {
            definition.addProperty(name, property);
            return property;
        }

        /**
         * A child's description belongs to the property that stands for it. Not every child yields
         * a
         * property - a type in the node filter is referenced by nothing - and then there is nothing
         * to describe.
         */
        private void describe(Property property, String elementDescription) {
            if (property != null && StringUtils.isNotEmpty(elementDescription)) {
                property.setDescription(elementDescription);
            }
        }

        /**
         * Emits the node GET, keyed by node type rather than by path.
         *
         * <p>
         * A node with no indexed properties and no container properties is queried without
         * parameters at all; otherwise the inherited path parameters apply.
         */
        private void appendNodeGetOperation() {
            if (!parameters.isEmpty()) {
                inheritedPathParams = inheritedPathParams == null ? new ArrayList<>()
                    : new ArrayList<>(inheritedPathParams);
                inheritedPathParams.addAll(parameters);
            }
            boolean unparameterized =
                indexedProps != null && indexedProps.isEmpty() && containerProps.isEmpty();
            List<Parameter> params =
                unparameterized || inheritedPathParams == null ? List.of() : inheritedPathParams;

            NodeGetOperation get =
                new NodeGetOperation(useOpId, xmlRootElementName, tag, path, params, nodeContext);
            Operation operation = get.build();
            if (operation != null) {
                Path endpoint = new Path();
                endpoint.setGet(operation);
                operations.put(xmlRootElementName, endpoint);
                pathKeys.put(xmlRootElementName, get.getPath());
                // mirrors the original placement of the checklist update at the tail of toString()
                get.register();
            }
        }

        private void appendDefinition() {
            if ("relationship".equals(xmlRootElementName)) {
                // a relationship is stored under the dictionary name; dictionaryOf() builds the
                // wrapper that points back at this definition
                dict = dictionaryOf(xmlRootElementName);
            }
            validEdges = getRelatedNodesDescription(xmlRootElementName);
            String description = description();
            if (description != null) {
                definition.setDescription(description);
            }
            if (!required.isEmpty()) {
                definition.setRequired(required);
            }
        }

        /**
         * Might have a description OR valid edges OR both OR neither. The trailing newline is what
         * makes the writer render the result as a literal block rather than as one line.
         */
        private String description() {
            if (StringUtils.isEmpty(pathDescriptionProperty) && StringUtils.isEmpty(validEdges)) {
                return null;
            }
            StringBuilder description = new StringBuilder();
            if (StringUtils.isNotEmpty(pathDescriptionProperty)) {
                description.append(pathDescriptionProperty).append("\n");
            }
            return description.append(validEdges).toString();
        }

        private void storeDefinition() {
            namespaceFilter.add(xmlRootElementName);
            if ("inventory".equals(xmlRootElementName)) {
                mergeInventoryDefinition(definition);
            } else if ("relationship".equals(xmlRootElementName)) {
                javaTypeDefinitions.put(xmlRootElementName, dict);
                javaTypeDefinitions.put(xmlRootElementName + "-dict",
                    publishedRelationshipDict(definition));
            } else {
                javaTypeDefinitions.put(xmlRootElementName, definition);
            }
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
}
