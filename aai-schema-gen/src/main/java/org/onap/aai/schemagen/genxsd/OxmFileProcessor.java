/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.schemagen.genxsd;

import com.google.common.collect.Multimap;

import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class OxmFileProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OxmFileProcessor.class);

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String DOUBLE_LINE_SEPARATOR =
        System.getProperty("line.separator") + System.getProperty("line.separator");

    /**
     * The one part of a swagger document that is not part of the document model: YAML has no place
     * for a licence notice, so it is carried as a comment block ahead of the serialized document.
     */
    protected static final String LICENSE_PREFIX = """
        #
        # ============LICENSE_START=======================================================
        # org.onap.aai
        # ================================================================================
        # Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
        # ================================================================================
        # Licensed under the Creative Commons License, Attribution 4.0 Intl. (the "License");
        # you may not use this file except in compliance with the License.
        # You may obtain a copy of the License at
        # <p>
        # https://creativecommons.org/licenses/by/4.0/
        # <p>
        # Unless required by applicable law or agreed to in writing, software
        # distributed under the License is distributed on an "AS IS" BASIS,
        # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        # See the License for the specific language governing permissions and
        # limitations under the License.
        # ============LICENSE_END=========================================================
        #

        """;

    protected static int annotationsStartVersion = 9; // minimum version to support annotations in
    // xsd
    protected static int annotationsMinVersion = 6; // lower versions support annotations in xsd
    protected static int swaggerSupportStartsVersion = 1; // minimum version to support swagger
    // documentation
    protected static int swaggerDiffStartVersion = 1; // minimum version to support difference
    protected static int swaggerMinBasepath = 6; // minimum version to support difference
    static List<String> nodeFilter = createNodeFilter();
    protected Set<String> namespaceFilter;
    protected File oxmFile;
    protected String xml;
    protected SchemaVersion v;
    protected Document doc = null;
    protected String apiVersion = null;
    protected SchemaConfigVersions schemaConfigVersions;
    protected Map<String, Integer> combinedJavaTypes;
    protected String apiVersionFmt = null;
    protected List<String> topLevelPaths = new ArrayList<String>();
    protected HashMap<String, String> generatedJavaType = new HashMap<String, String>();
    protected HashMap<String, String> appliedPaths = new HashMap<String, String>();
    protected NodeList javaTypeNodes = null;
    protected Map<String, Model> javaTypeDefinitions = createJavaTypeDefinitions();
    EdgeIngestor ei;
    NodeIngestor ni;

    public OxmFileProcessor(SchemaConfigVersions schemaConfigVersions, NodeIngestor ni,
        EdgeIngestor ei) {
        this.schemaConfigVersions = schemaConfigVersions;
        this.ni = ni;
        this.ei = ei;
    }

    private static List<String> createNodeFilter() {
        return Arrays.asList("search", "actions", "aai-internal", "nodes");
    }

    /**
     * The definitions that are not derived from the OXM. {@code nodes} has no java-type of its own
     * -
     * it is the container the {@code /nodes} endpoints hang off - so its definition is stated here.
     */
    private Map<String, Model> createJavaTypeDefinitions() {
        ModelImpl nodes = new ModelImpl();
        nodes.addProperty("inventory-item-data",
            new ArrayProperty(new RefProperty("inventory-item-data")));
        Map<String, Model> javaTypeDefinitions = new HashMap<>();
        javaTypeDefinitions.put("nodes", nodes);
        return javaTypeDefinitions;
    }

    /**
     * The document every generated swagger file starts from: everything above {@code paths:}, which
     * is the same for both documents of a version.
     */
    protected Swagger newDocument(String basePath) {
        Swagger document = new Swagger();
        document.setInfo(documentInfo(basePath));
        document.setHost("localhost");
        document.setBasePath(basePath + "/" + v.toString());
        document.setSchemes(List.of(Scheme.HTTPS));
        return document;
    }

    private Info documentInfo(String basePath) {
        StringBuilder description = new StringBuilder();
        if (versionSupportsSwaggerDiff(v.toString())) {
            description.append("[Differences versus the previous schema version](apidocs")
                .append(basePath).append("/aai_swagger_").append(v.toString()).append(".diff)\n\n");
        }
        description.append(
            "This document is best viewed with Firefox or Chrome. Nodes can be found by opening the models link below and finding the node-type. Edge definitions can be found with the node definitions.\n");

        License license = new License();
        license.setName("Apache 2.0");
        license.setUrl("http://www.apache.org/licenses/LICENSE-2.0.html");

        Info info = new Info();
        info.setDescription(description.toString());
        info.setVersion(v.toString());
        info.setTitle("Active and Available Inventory REST API");
        info.setLicense(license);
        return info;
    }

    /** The document's definitions, sorted by name. */
    public Map<String, Model> definitions() {
        return definitions(null);
    }

    /**
     * The document's definitions, sorted by name, restricted to the java-types of one OXM
     * namespace.
     */
    public Map<String, Model> definitions(Set<String> namespaceFilter) {
        Map<String, Model> definitions = new TreeMap<>();
        for (Map.Entry<String, Model> entry : javaTypeDefinitions.entrySet()) {
            if (namespaceFilter != null && !namespaceFilter.contains(entry.getKey())) {
                continue;
            }
            definitions.put(entry.getKey(), entry.getValue());
        }
        return definitions;
    }

    /** The wrapper definition a {@code relationship} is published under: a dictionary of them. */
    protected ModelImpl dictionaryOf(String resource) {
        ModelImpl dictionary = new ModelImpl();
        dictionary.setType(ModelImpl.OBJECT);
        dictionary.setDescription("dictionary of " + resource + "\n");
        dictionary.addProperty(resource, new ArrayProperty(new RefProperty(resource + "-dict")));
        return dictionary;
    }

    /**
     * The published form of the {@code relationship} definition: the properties from
     * {@code related-to-property} onwards are dropped.
     *
     * <p>
     * Those describe a relationship as it is stored rather than as it is exchanged, so the
     * documents
     * have never published them. The cut is by property name - the OXM declares them last in every
     * version - rather than by position.
     */
    protected ModelImpl publishedRelationshipDict(ModelImpl relationship) {
        Map<String, Property> properties = relationship.getProperties();
        if (properties == null) {
            return relationship;
        }
        boolean drop = false;
        for (String name : List.copyOf(properties.keySet())) {
            drop |= "related-to-property".equals(name);
            if (drop) {
                properties.remove(name);
            }
        }
        return relationship;
    }

    /**
     * {@code inventory} is the one definition several java-types contribute to, so it accumulates
     * across the encounters rather than being replaced by the last of them.
     */
    protected void mergeInventoryDefinition(ModelImpl contribution) {
        ModelImpl inventory = (ModelImpl) javaTypeDefinitions.get("inventory");
        if (inventory == null) {
            javaTypeDefinitions.put("inventory", contribution);
            return;
        }
        if (contribution.getProperties() != null) {
            contribution.getProperties().forEach(inventory::addProperty);
        }
    }

    public void setOxmVersion(File oxmFile, SchemaVersion v) {
        this.oxmFile = oxmFile;
        this.v = v;
    }

    public void setXmlVersion(String xml, SchemaVersion v) {
        this.xml = xml;
        this.v = v;
    }

    public void setVersion(SchemaVersion v) {
        this.oxmFile = null;
        this.v = v;
    }

    public void setNodeIngestor(NodeIngestor ni) {
        this.ni = ni;
    }

    public void setEdgeIngestor(EdgeIngestor ei) {
        this.ei = ei;
    }

    public SchemaConfigVersions getSchemaConfigVersions() {
        return schemaConfigVersions;
    }

    public void setSchemaConfigVersions(SchemaConfigVersions schemaConfigVersions) {
        this.schemaConfigVersions = schemaConfigVersions;
    }

    protected void getTopLevelPaths(XSDElement elem) {
        NodeList parentNodes;
        Element parentElement;
        NodeList xmlElementNodes;

        parentNodes = elem.getElementsByTagName("java-attributes");
        if (parentNodes.getLength() == 0) {
            return;
        }
        parentElement = (Element) parentNodes.item(0);
        xmlElementNodes = parentElement.getElementsByTagName("xml-element");
        if (xmlElementNodes.getLength() <= 0) {
            return;
        }

        XSDElement xmlElementElement;

        for (int i = 0; i < xmlElementNodes.getLength(); ++i) {
            xmlElementElement = new XSDElement((Element) xmlElementNodes.item(i));
            if (!xmlElementElement.getParentNode().isSameNode(parentElement)) {
                continue;
            }
            String topLevel = xmlElementElement.getAttribute("type");
            topLevel = topLevel.substring(topLevel.lastIndexOf('.') + 1);
            if (!topLevelPaths.contains(topLevel)) {
                if ("Nodes".equals(topLevel) || "AaiInternal".equals(topLevel)) {
                    continue;
                }
                topLevelPaths.add(topLevel);
            }
        }
    }

    protected boolean checkTopLevel(String topLevel, boolean ignoreActionsSearch) {
        // when ignoreActionsSearch is set to true, with a topLevel that matches one of the values
        // to ignore, the logic will handle those values, as if they are not at the top level.
        // this was done when refactoring checks that may or may not include these top levels.
        // Using this API allows new top levels to be added to the schema file and
        // included in the generated yaml without changing this generation logic.
        if (ignoreActionsSearch) {
            if ("Actions".equals(topLevel) || "Search".equals(topLevel)) {
                return false;
            }
        }
        return topLevelPaths.contains(topLevel);
    }

    protected void init()
        throws ParserConfigurationException, SAXException, IOException, EdgeRuleNotFoundException {
        if (this.xml != null || this.oxmFile != null) {
            createDocument();
        }
        if (this.doc == null) {
            this.doc = ni.getSchema(v);
        }
        namespaceFilter = new HashSet<>();

        NodeList bindingsNodes = doc.getElementsByTagName("xml-bindings");
        Element bindingElement;
        NodeList javaTypesNodes;
        Element javaTypesElement;

        if (bindingsNodes == null || bindingsNodes.getLength() == 0) {
            throw new SAXException("OXM file error: missing <binding-nodes> in " + oxmFile);
        }

        bindingElement = (Element) bindingsNodes.item(0);
        javaTypesNodes = bindingElement.getElementsByTagName("java-types");
        if (javaTypesNodes.getLength() < 1) {
            throw new SAXException(
                "OXM file error: missing <binding-nodes><java-types> in " + oxmFile);
        }
        javaTypesElement = (Element) javaTypesNodes.item(0);

        javaTypeNodes = javaTypesElement.getElementsByTagName("java-type");
        if (javaTypeNodes.getLength() < 1) {
            throw new SAXException(
                "OXM file error: missing <binding-nodes><java-types><java-type> in " + oxmFile);
        }
    }

    private void createDocument() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        if (xml == null) {
            doc = dBuilder.parse(oxmFile);
        } else {
            InputSource isInput = new InputSource(new StringReader(xml));
            doc = dBuilder.parse(isInput);
        }
    }

    public abstract String process() throws ParserConfigurationException, SAXException, IOException,
        FileNotFoundException, EdgeRuleNotFoundException;

    public String getXMLRootElementName(Element javaTypeElement) {
        String xmlRootElementName = null;
        NamedNodeMap attributes;

        NodeList valNodes = javaTypeElement.getElementsByTagName("xml-root-element");
        Element valElement = (Element) valNodes.item(0);
        attributes = valElement.getAttributes();
        for (int i = 0; i < attributes.getLength(); ++i) {
            Attr attr = (Attr) attributes.item(i);
            String attrName = attr.getNodeName();

            String attrValue = attr.getNodeValue();
            if ("name".equals(attrName)) {
                xmlRootElementName = attrValue;
            }
        }
        return xmlRootElementName;
    }

    public String getXmlRootElementName(String javaTypeName) {
        String attrName, attrValue;
        Attr attr;
        Element javaTypeElement;
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            javaTypeElement = (Element) javaTypeNodes.item(i);
            NamedNodeMap attributes = javaTypeElement.getAttributes();
            for (int j = 0; j < attributes.getLength(); ++j) {
                attr = (Attr) attributes.item(j);
                attrName = attr.getNodeName();
                attrValue = attr.getNodeValue();
                if ("name".equals(attrName) && attrValue.equals(javaTypeName)) {
                    NodeList valNodes = javaTypeElement.getElementsByTagName("xml-root-element");
                    Element valElement = (Element) valNodes.item(0);
                    attributes = valElement.getAttributes();
                    for (int k = 0; k < attributes.getLength(); ++k) {
                        attr = (Attr) attributes.item(k);
                        attrName = attr.getNodeName();

                        attrValue = attr.getNodeValue();
                        if ("name".equals(attrName)) {
                            return (attrValue);
                        }
                    }
                }
            }
        }
        return null;
    }

    public Map<String, Integer> getCombinedJavaTypes() {
        return combinedJavaTypes;
    }

    public void setCombinedJavaTypes(Map<String, Integer> combinedJavaTypes) {
        this.combinedJavaTypes = combinedJavaTypes;
    }

    public Element getJavaTypeElementSwagger(String javaTypeName) {

        String attrName, attrValue;
        Attr attr;
        Element javaTypeElement;

        List<Element> combineElementList = new ArrayList<Element>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            javaTypeElement = (Element) javaTypeNodes.item(i);
            NamedNodeMap attributes = javaTypeElement.getAttributes();
            for (int j = 0; j < attributes.getLength(); ++j) {
                attr = (Attr) attributes.item(j);
                attrName = attr.getNodeName();
                attrValue = attr.getNodeValue();
                if ("name".equals(attrName) && attrValue.equals(javaTypeName)) {
                    combineElementList.add(javaTypeElement);
                }
            }
        }
        if (combineElementList.size() == 0) {
            return (Element) null;
        } else if (combineElementList.size() > 1) {
            return combineElements(javaTypeName, combineElementList);
        }
        return combineElementList.get(0);
    }

    public boolean versionSupportsSwaggerDiff(String version) {
        int ver = Integer.parseInt(version.substring(1));
        return ver >= HTMLfromOXM.swaggerDiffStartVersion;
    }

    public boolean versionSupportsBasePathProperty(String version) {
        int ver = Integer.parseInt(version.substring(1));
        return ver <= HTMLfromOXM.swaggerMinBasepath;
    }

    protected void updateParentXmlElements(Element parentElement, NodeList moreXmlElementNodes) {
        Element xmlElement;
        NodeList childNodes;
        Node childNode;

        Node refChild = null;
        // find childNode with attributes and no children, insert children before that node
        childNodes = parentElement.getChildNodes();
        if (childNodes == null || childNodes.getLength() == 0) {
            // should not happen since the base parent was chosen if it had children
            return;
        }

        for (int i = 0; i < childNodes.getLength(); ++i) {
            refChild = childNodes.item(i);
            if (refChild.hasAttributes() && !refChild.hasChildNodes()) {
                break;
            }

        }

        for (int i = 0; i < moreXmlElementNodes.getLength(); ++i) {
            xmlElement = (Element) moreXmlElementNodes.item(i);
            childNode = xmlElement.cloneNode(true);
            parentElement.insertBefore(childNode, refChild);
        }
    }

    protected Node getXmlPropertiesNode(Element javaTypeElement) {
        NodeList nl = javaTypeElement.getChildNodes();
        Node child;
        for (int i = 0; i < nl.getLength(); ++i) {
            child = nl.item(i);
            if ("xml-properties".equals(child.getNodeName())) {
                return child;
            }
        }
        return null;
    }

    protected Node merge(NodeList nl, Node mergeNode) {
        NamedNodeMap nnm = mergeNode.getAttributes();
        Node childNode;
        NamedNodeMap childNnm;

        String mergeName = nnm.getNamedItem("name").getNodeValue();
        String mergeValue = nnm.getNamedItem("value").getNodeValue();
        String childName;
        String childValue;
        for (int j = 0; j < nl.getLength(); ++j) {
            childNode = nl.item(j);
            if ("xml-property".equals(childNode.getNodeName())) {
                childNnm = childNode.getAttributes();
                childName = childNnm.getNamedItem("name").getNodeValue();
                childValue = childNnm.getNamedItem("value").getNodeValue();
                if (childName.equals(mergeName)) {
                    // attribute exists
                    // keep, replace or update
                    if (childValue.contains(mergeValue)) {
                        return null;
                    }
                    if (mergeValue.contains(childValue)) {
                        childNnm.getNamedItem("value").setTextContent(mergeValue);
                        return null;
                    }
                    childNnm.getNamedItem("value").setTextContent(mergeValue + "," + childValue);
                    return null;
                }
            }
        }
        childNode = mergeNode.cloneNode(true);
        return childNode;
    }

    protected void mergeXmlProperties(Node useChildProperties, NodeList propertiesToMerge) {
        NodeList nl = useChildProperties.getChildNodes();
        Node childNode;
        Node newNode;
        for (int i = 0; i < propertiesToMerge.getLength(); ++i) {
            childNode = propertiesToMerge.item(i);
            if ("xml-property".equals(childNode.getNodeName())) {
                newNode = merge(nl, childNode);
                if (newNode != null) {
                    useChildProperties.appendChild(newNode);
                }
            }

        }
    }

    protected void combineXmlProperties(int useElement, List<Element> combineElementList) {
        // add or update xml-properties to the referenced element from the combined list
        Element javaTypeElement = combineElementList.get(useElement);
        Node useChildProperties = getXmlPropertiesNode(javaTypeElement);
        int cloneChild = -1;
        Node childProperties;
        if (useChildProperties == null) {
            // find xml-properties to clone
            for (int i = 0; i < combineElementList.size(); ++i) {
                if (i == useElement) {
                    continue;
                }
                childProperties = getXmlPropertiesNode(combineElementList.get(i));
                if (childProperties != null) {
                    useChildProperties = childProperties.cloneNode(true);
                    javaTypeElement.appendChild(useChildProperties);
                    cloneChild = i;
                }
            }
        }
        NodeList cnl;
        // find other xml-properties
        for (int i = 0; i < combineElementList.size(); ++i) {
            if (i == useElement || (cloneChild >= 0 && i <= cloneChild)) {
                continue;
            }
            childProperties = getXmlPropertiesNode(combineElementList.get(i));
            if (childProperties == null) {
                continue;
            }
            cnl = childProperties.getChildNodes();
            mergeXmlProperties(useChildProperties, cnl);
        }

    }

    protected Element combineElements(String javaTypeName, List<Element> combineElementList) {
        Element javaTypeElement;
        NodeList parentNodes;
        Element parentElement = null;
        NodeList xmlElementNodes;

        int useElement = -1;
        if (combinedJavaTypes.containsKey(javaTypeName)) {
            return combineElementList.get(combinedJavaTypes.get(javaTypeName));
        }
        for (int i = 0; i < combineElementList.size(); ++i) {
            javaTypeElement = combineElementList.get(i);
            parentNodes = javaTypeElement.getElementsByTagName("java-attributes");
            if (parentNodes.getLength() == 0) {
                continue;
            }
            parentElement = (Element) parentNodes.item(0);
            xmlElementNodes = parentElement.getElementsByTagName("xml-element");
            if (xmlElementNodes.getLength() <= 0) {
                continue;
            }
            useElement = i;
            break;
        }
        boolean doCombineElements = true;
        if (useElement < 0) {
            useElement = 0;
            doCombineElements = false;
        } else if (useElement == combineElementList.size() - 1) {
            doCombineElements = false;
        }
        if (doCombineElements) {
            // get xml-element from other javaTypeElements
            Element otherParentElement = null;
            for (int i = 0; i < combineElementList.size(); ++i) {
                if (i == useElement) {
                    continue;
                }
                javaTypeElement = combineElementList.get(i);
                parentNodes = javaTypeElement.getElementsByTagName("java-attributes");
                if (parentNodes.getLength() == 0) {
                    continue;
                }
                otherParentElement = (Element) parentNodes.item(0);
                xmlElementNodes = otherParentElement.getElementsByTagName("xml-element");
                if (xmlElementNodes.getLength() <= 0) {
                    continue;
                }
                // xml-element that are not present
                updateParentXmlElements(parentElement, xmlElementNodes);

            }
        }
        // need to combine xml-properties
        combineXmlProperties(useElement, combineElementList);
        combinedJavaTypes.put(javaTypeName, useElement);
        return combineElementList.get(useElement);
    }

    /**
     * The "Related Nodes" markdown block for a node type: one bullet per edge rule, the
     * also-delete footnotes those bullets reference, and a closing cannot-be-deleted note.
     *
     * <p>
     * Outgoing edges are listed before incoming ones, and within each direction the rules are
     * visited in sorted key order, so the block is stable across runs. The caption is emitted by
     * whichever direction produces the first bullet; a node with no edges at all yields an empty
     * string, which is why callers test the result before opening a {@code description:} tag.
     *
     * <p>
     * An edge query that fails is logged and skipped rather than propagated - a pre-existing
     * behaviour this extraction deliberately preserves, since changing it would alter the generated
     * documents whenever a rule set is incomplete.
     */
    protected String getRelatedNodesDescription(String xmlRootElementName) {
        DeleteFootnoteSet footnotes = new DeleteFootnoteSet(xmlRootElementName);
        StringBuilder sbEdge = new StringBuilder();
        LinkedHashSet<String> preventDelete = new LinkedHashSet<>();
        String nodeCaption = "###### Related Nodes\n";
        try {
            EdgeRuleQuery q =
                new EdgeRuleQuery.Builder(xmlRootElementName).version(v).fromOnly().build();
            Multimap<String, EdgeRule> results = ei.getRules(q);
            SortedSet<String> ss = new TreeSet<>(results.keySet());
            sbEdge.append(nodeCaption);
            nodeCaption = "";
            for (String key : ss) {
                results.get(key).stream()
                    .filter(i -> i.getFrom().equals(xmlRootElementName) && !i.isPrivateEdge())
                    .forEach(
                        i -> appendEdge(sbEdge, footnotes, i, "TO", i.getTo(), xmlRootElementName));
                results.get(key).stream()
                    .filter(i -> i.getFrom().equals(xmlRootElementName) && !i.isPrivateEdge()
                        && i.getPreventDelete().equals("OUT"))
                    .forEach(i -> preventDelete.add(i.getTo().toUpperCase()));
            }
        } catch (Exception e) {
            logger.debug("xmlRootElementName: " + xmlRootElementName + " from edge exception\n", e);
        }
        try {
            EdgeRuleQuery q1 =
                new EdgeRuleQuery.Builder(xmlRootElementName).version(v).toOnly().build();
            Multimap<String, EdgeRule> results = ei.getRules(q1);
            SortedSet<String> ss = new TreeSet<>(results.keySet());
            sbEdge.append(nodeCaption);
            for (String key : ss) {
                results.get(key).stream()
                    .filter(i -> i.getTo().equals(xmlRootElementName) && !i.isPrivateEdge())
                    .forEach(i -> appendEdge(sbEdge, footnotes, i, "FROM", i.getFrom(),
                        xmlRootElementName));
                results.get(key).stream()
                    .filter(i -> i.getTo().equals(xmlRootElementName) && !i.isPrivateEdge()
                        && i.getPreventDelete().equals("IN"))
                    .forEach(i -> preventDelete.add(i.getFrom().toUpperCase()));
            }
        } catch (Exception e) {
            logger.debug("xmlRootElementName: " + xmlRootElementName + " to edge exception\n", e);
        }
        if (!preventDelete.isEmpty()) {
            String prevent = xmlRootElementName.toUpperCase() + " cannot be deleted if related to "
                + String.join(",", preventDelete);
            logger.debug(prevent);
            footnotes.add(prevent);
        }
        if (!footnotes.footnotes.isEmpty()) {
            sbEdge.append(footnotes.toString());
        }
        return sbEdge.toString();
    }

    /**
     * Appends one {@code - TO}/{@code - FROM} bullet and collects the also-delete footnote it
     * refers to, if any.
     */
    private void appendEdge(StringBuilder sbEdge, DeleteFootnoteSet footnotes, EdgeRule rule,
        String direction, String otherNode, String xmlRootElementName) {
        logger.info("      - " + direction + " " + otherNode + rule.getDirection().toString()
            + rule.getContains());
        sbEdge.append("- ").append(direction).append(" ").append(otherNode);
        EdgeDescription ed = new EdgeDescription(rule);
        String footnote = ed.getAlsoDeleteFootnote(xmlRootElementName);
        sbEdge.append(ed.getRelationshipDescription(direction, xmlRootElementName)).append(footnote)
            .append("\n");
        if (StringUtils.isNotEmpty(footnote)) {
            footnotes.add(footnote);
        }
    }
}
