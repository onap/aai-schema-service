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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.schemaservice.nodeschema;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import jakarta.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
import org.onap.aai.schemaservice.config.ConfigTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * NodeIngestor - ingests A&AI OXM files per given config, serves DynamicJAXBContext per version
 */
@Component
public class NodeIngestor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeIngestor.class);

    private static final Pattern classNamePattern = Pattern.compile("\\.(v\\d+)\\.");
    private Map<SchemaVersion, DynamicJAXBContext> versionContextMap = new TreeMap<>();
    private Map<SchemaVersion, Set<String>> typesPerVersion = new TreeMap<>();
    private Map<SchemaVersion, Document> schemaPerVersion = new TreeMap<>();
    private ConfigTranslator translator;


    @Autowired
    /**
     * Instantiates the NodeIngestor bean.
     *
     * @param translator - ConfigTranslator autowired in by Spring framework which
     * contains the configuration information needed to ingest the desired files.
     */
    public NodeIngestor(ConfigTranslator translator) {
        this.translator = translator;
        Map<SchemaVersion, List<String>> filesToIngest = translator.getNodeFiles();

        try {
            for (Entry<SchemaVersion, List<String>> verFiles : filesToIngest.entrySet()) {
                SchemaVersion v = verFiles.getKey();
                List<String> files = verFiles.getValue();
                final DynamicJAXBContext ctx = ingest(files);
                versionContextMap.put(v, ctx);
                typesPerVersion.put(v, getAllNodeTypes(files));
                schemaPerVersion.put(v, createCombinedSchema(files, v));
            }
        } catch (JAXBException | ParserConfigurationException | SAXException | IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Ingests the given OXM files into DynamicJAXBContext
     *
     * @param files - List<String> of full filenames (ie including the path) to be ingested
     * @return DynamicJAXBContext including schema information from all given files
     * @throws FileNotFoundException if an OXM file can't be found
     * @throws JAXBException         if there's an error creating the DynamicJAXBContext
     */
    private DynamicJAXBContext ingest(List<String> files)
        throws FileNotFoundException, JAXBException {
        List<InputStream> streams = new ArrayList<>();

        for (String name : files) {
            streams.add(new FileInputStream(new File(name)));
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, streams);
        return DynamicJAXBContextFactory
            .createContextFromOXM(this.getClass().getClassLoader(), properties);
    }


    private Set<String> getAllNodeTypes(List<String> files)
        throws ParserConfigurationException, SAXException, IOException {
        Set<String> types = new HashSet<>();
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        docFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        docFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        docFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        docFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        docFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        ArrayList<Node> javaTypes = new ArrayList<>();
        for (String file : files) {
            InputStream inputStream = new FileInputStream(file);

            final Document doc = docBuilder.parse(inputStream);
            final NodeList list = doc.getElementsByTagName("java-type");


            for (int i = 0; i < list.getLength(); i++) {
                String type = list.item(i).getAttributes().getNamedItem("name").getNodeValue();
                javaTypes.add(list.item(i));
                types.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, type));
            }
        }

        return types;
    }

    private Document createCombinedSchema(List<String> files, SchemaVersion v)
        throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        docFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        docFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        docFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        docFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        docFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        DocumentBuilder masterDocBuilder = docFactory.newDocumentBuilder();
        Document combinedDoc = masterDocBuilder.parse(getShell(v));
        NodeList masterList = combinedDoc.getElementsByTagName("java-types");
        Node javaTypesContainer =
            masterList.getLength() == 0 ? combinedDoc.getDocumentElement() : masterList.item(0);

        Multimap<String, Node> nodeMultimap = ArrayListMultimap.create();
        LOGGER.debug("Started combining the schema from list of files {} for version {}", files, v);

        for (String file : files) {
            InputStream inputStream = new FileInputStream(file);

            final Document doc = docBuilder.parse(inputStream);
            final NodeList list = doc.getElementsByTagName("java-type");

            for (int i = 0; i < list.getLength(); i++) {
                Node curNode = list.item(i);
                String name = curNode.getAttributes().getNamedItem("name").getNodeValue();
                nodeMultimap.put(name, curNode);
            }
        }

        Map<String, Collection<Node>> map = nodeMultimap.asMap();
        createNode(combinedDoc, javaTypesContainer, map);

        LOGGER.debug("Successfully merged all schema files for version {}", v);

        return combinedDoc;
    }

    private void createNode(Document combinedDoc, Node javaTypesContainer,
                            Map<String, Collection<Node>> map) {

        for (Entry<String, Collection<Node>> entry : map.entrySet()) {

            List<Node> listOfNodes = (List<Node>) entry.getValue();
            LOGGER.trace("NodeType {} Occurrences {}", entry.getKey(), listOfNodes.size());
            Node copyOfFirstElement = null;
            Node javaAttributeElement = null;

            if (listOfNodes.size() > 1) {
                for (int index = 0; index < listOfNodes.size(); index++) {
                    if (index == 0) {
                        Node currentNode = listOfNodes.get(index);
                        copyOfFirstElement = combinedDoc.importNode(currentNode, true);
                        if (copyOfFirstElement.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) copyOfFirstElement;
                            NodeList javaAttributesList =
                                element.getElementsByTagName("java-attributes");
                            for (int javaAttributeIndex = 0;
                                 javaAttributeIndex < javaAttributesList.getLength();
                                 javaAttributeIndex++) {
                                javaAttributeElement = javaAttributesList.item(javaAttributeIndex);
                            }
                        }
                    } else {
                        Node currentNode = listOfNodes.get(index);
                        Node copyOfCurrentElement = combinedDoc.importNode(currentNode, true);
                        if (copyOfCurrentElement.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) copyOfCurrentElement;
                            NodeList javaAttributesList =
                                element.getElementsByTagName("java-attributes");
                            for (int javaAttributeIndex = 0;
                                 javaAttributeIndex < javaAttributesList.getLength();
                                 javaAttributeIndex++) {
                                Node jaElement = javaAttributesList.item(javaAttributeIndex);
                                NodeList xmlElementList = jaElement.getChildNodes();
                                for (int xmlElementIndex = 0;
                                     xmlElementIndex < xmlElementList.getLength();
                                     xmlElementIndex++) {
                                    if (javaAttributeElement != null) {
                                        Node curElem = xmlElementList.item(xmlElementIndex);
                                        if (curElem != null) {
                                            javaAttributeElement
                                                .appendChild(curElem.cloneNode(true));
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
                javaTypesContainer.appendChild(copyOfFirstElement);
            } else if (listOfNodes.size() == 1) {
                javaTypesContainer.appendChild(combinedDoc.importNode(listOfNodes.get(0), true));
            }
        }
    }

    /**
     * Gets the DynamicJAXBContext for the given version
     *
     * @param v
     * @return DynamicJAXBContext
     */
    public DynamicJAXBContext getContextForVersion(SchemaVersion v) {
        return versionContextMap.get(v);
    }

    /**
     * Determines if the given version contains the given node type
     *
     * @param nodeType - node type to check, must be in lower hyphen form (ie "type-name")
     * @param v        - schema version to check against
     * @return
     */
    public boolean hasNodeType(String nodeType, SchemaVersion v) {
        return typesPerVersion.get(v).contains(nodeType);
    }

    public Set<String> getObjectsInVersion(SchemaVersion v) {
        return typesPerVersion.get(v);
    }

    public Document getSchema(SchemaVersion v) {
        return schemaPerVersion.get(v);
    }

    private InputStream getShell(SchemaVersion v) {
        String source = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xml-bindings xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence/oxm\" package-name=\"inventory.aai.onap.org." +
            v.toString().toLowerCase() + "\" xml-mapping-metadata-complete=\"true\">\n" +
            "  <xml-schema element-form-default=\"QUALIFIED\">\n" +
            "    <xml-ns namespace-uri=\"http://org.onap.aai.inventory/" +
            v.toString().toLowerCase() + "\" />\n" +
            "  </xml-schema>\n" +
            "  <java-types>\n" +
            "  </java-types>\n" +
            "</xml-bindings>";
        return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
    }


    public SchemaVersion getVersionFromClassName(String classname) {
        Matcher m = classNamePattern.matcher(classname);
        String version = null;
        if (m.find()) {
            version = m.group(1);
            return new SchemaVersion(version);
        } else {
            return translator.getSchemaVersions().getDefaultVersion();
        }
    }
}

