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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
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
 * Builds the CRUD swagger document for one schema version from that version's OXM: an endpoint per
 * addressable object with its GET, PUT, PATCH and DELETE, and a definition per java-type.
 */
public class YAMLfromOXM extends OxmFileProcessor {

    private static final Logger logger = LoggerFactory.getLogger(YAMLfromOXM.class);

    /**
     * The names of the PATCH flavour of a definition are the base names under a prefix, and the
     * prefix sorts last so that they follow the definitions they are derived from.
     */
    private static final String PATCH_DEFINITION_PREFIX = "zzzz-patch-";

    private final String basePath;

    /**
     * State shared with the operation emitters. Deliberately spans every version generated in a
     * run; see {@link GenerationContext} for why narrowing its lifetime would change the output.
     */
    private final GenerationContext context;

    /** The endpoints, in the order the walk over the OXM reaches them. */
    private Map<String, Path> paths = new LinkedHashMap<>();

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

    protected void init() throws ParserConfigurationException, SAXException, IOException,
        FileNotFoundException, EdgeRuleNotFoundException {
        super.init();
    }

    @Override
    public String process() throws ParserConfigurationException, SAXException, IOException,
        FileNotFoundException, EdgeRuleNotFoundException {
        try {
            init();
        } catch (Exception e) {
            logger.error("Error initializing " + this.getClass(), e);
            throw e;
        }
        paths = new LinkedHashMap<>();
        combinedJavaTypes = new HashMap<>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            Element elem = (Element) javaTypeNodes.item(i);
            String javaTypeName = elem.getAttribute("name");
            boolean processInventory = "Inventory".equals(javaTypeName);
            if (!processInventory) {
                if (generatedJavaType.containsKey(getXmlRootElementName(javaTypeName))) {
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
        document.setPaths(paths);
        document.setDefinitions(definitions());
        new PutRelationPathSet(v, context).generateRelations(ei);
        return LICENSE_PREFIX + SwaggerWriter.toYaml(document);
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
        String path, String tag, String opId, List<Parameter> pathParams) {

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
            new JavaTypeScope(javaTypeName, javaTypeElement, path, tag, opId, pathParams);
        if (appliedPaths.containsKey(scope.path)) {
            return;
        }
        StringTokenizer st = new StringTokenizer(scope.path, "/");
        if (st.countTokens() > 1) {
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
        if ("inventory".equals(scope.xmlRootElementName)) {
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
     * accumulates. One instance per {@link YAMLfromOXM#processJavaTypeElementSwagger} invocation,
     * so
     * a referenced type gets its own.
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
        private List<Parameter> inheritedPathParams;

        private final String pathDescriptionProperty;
        private final String container;
        private final Vector<String> indexedProps;
        private final Vector<String> dslStartNodeProps;
        private final List<Parameter> containerProps = new ArrayList<>();

        /** This type's own xml-key parameters, named after the schema property. */
        private final List<Parameter> parameters = new ArrayList<>();
        /**
         * The same parameters named for their place in the path: a child whose name already appears
         * in the path is qualified with the segment above it.
         */
        private final List<Parameter> pathParameters = new ArrayList<>();

        /** This type's definition, and its PATCH flavour, before they are stored. */
        private final ModelImpl definition = new ModelImpl();
        private final ModelImpl patchDefinition = new ModelImpl();
        /** In OXM declaration order, which is not the order the property names sort in. */
        private final List<String> required = new ArrayList<>();
        /** Only for {@code relationship}: the dictionary wrapper stored under the plain name. */
        private ModelImpl dict;
        /** The "Related Nodes" block; read by both description emitters. */
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
            // use alternate name for parameter if already in the path string
            String modifiedName = path.contains("/{" + name + "}")
                ? path.substring(path.lastIndexOf('/') + 1) + "." + name
                : name;
            if (isKey) {
                path += "/{" + modifiedName + "}";
            }

            if (isKey) {
                parameters.add(xmlElementElement.getPathParameter(elementDescription));
                pathParameters
                    .add(xmlElementElement.getPathParameter(elementDescription, modifiedName));
            }
            if ("true".equals(xmlElementElement.getAttribute("required"))) {
                appendRequired(name, addTypeV);
            }
            if (indexedProps != null && indexedProps.contains(name)) {
                containerProps.add(xmlElementElement.getQueryParameter());
                context.addContainerProps(container, containerProps);
            }
            if (xmlElementElement.isStandardType()) {
                appendStandardTypeProperty(xmlElementElement, name);
            }

            List<Parameter> newPathParams = inherited(pathParameters);
            for (int k = 0; addTypeV != null && k < addTypeV.size(); ++k) {
                newPathParams = appendReferencedType(xmlElementElement, addTypeV.elementAt(k),
                    elementDescription, newPathParams);
            }
        }

        /** The parameters inherited from the referencing type, extended by some of our own. */
        private List<Parameter> inherited(List<Parameter> own) {
            List<Parameter> combined = new ArrayList<>();
            if (inheritedPathParams != null) {
                combined.addAll(inheritedPathParams);
            }
            combined.addAll(own);
            return combined;
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

        private void appendStandardTypeProperty(XSDElement xmlElementElement, String name) {
            boolean isDslStartNode = dslStartNodeProps.contains(name);
            definition.addProperty(name, xmlElementElement.getTypeProperty(isDslStartNode));
            // resource-version is server-owned, so it is not part of a PATCH payload
            if (!"resource-version".equals(name)) {
                patchDefinition.addProperty(name,
                    xmlElementElement.getTypeProperty(isDslStartNode));
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
                newPathParams = inherited(parameters);
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
                patchDefinition.addProperty(useName, new RefProperty(itemName));
                return putProperty(useName, new RefProperty(itemName));
            }
            ObjectProperty wrapper = new ObjectProperty();
            Map<String, Property> members = new LinkedHashMap<>();
            members.put(itemName, new ArrayProperty(
                new RefProperty("".equals(itemName) ? "inventory-item-data" : itemName)));
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

        private void appendOperations() {
            if (!parameters.isEmpty()) {
                if (inheritedPathParams == null) {
                    inheritedPathParams = new ArrayList<>();
                }
                inheritedPathParams.addAll(pathParameters);
            }
            List<Parameter> params = inheritedPathParams == null ? List.of() : inheritedPathParams;

            Path endpoint = new Path();
            endpoint.setGet(
                new GetOperation(useOpId, xmlRootElementName, tag, path, params, context).build());

            PutOperation put =
                new PutOperation(useOpId, xmlRootElementName, tag, path, params, v, basePath);
            Operation putOperation = put.build();
            if (putOperation != null) {
                endpoint.setPut(putOperation);
                put.register(context);
            }

            PatchOperation patch =
                new PatchOperation(useOpId, xmlRootElementName, tag, path, params, v, basePath);
            patch.setPrefixForPatchRef(PATCH_DEFINITION_PREFIX);
            endpoint.setPatch(patch.build());

            DeleteOperation delete =
                new DeleteOperation(useOpId, xmlRootElementName, tag, path, params);
            Operation deleteOperation = delete.build();
            if (deleteOperation != null) {
                endpoint.setDelete(deleteOperation);
                delete.register(context);
            }

            // an endpoint none of the four operations apply to is not an endpoint
            if (!endpoint.isEmpty()) {
                paths.put(path, endpoint);
            }
        }

        private void appendDefinition() {
            if ("relationship".equals(xmlRootElementName)) {
                // a relationship is stored under the dictionary name; dictionaryOf() builds the
                // wrapper that points back at this definition
                definition.setType(ModelImpl.OBJECT);
                dict = dictionaryOf(xmlRootElementName);
            }
            validEdges = getRelatedNodesDescription(xmlRootElementName);
            String description = description();
            if (description != null) {
                definition.setDescription(description);
                patchDefinition.setDescription(description);
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
                // relationship-list is replaced whole, so it has no PATCH form to reference
                if (!"relationship-list".equals(xmlRootElementName)
                    && patchDefinition.getProperties() != null) {
                    javaTypeDefinitions.put(PATCH_DEFINITION_PREFIX + xmlRootElementName,
                        patchDefinition);
                }
            }
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
