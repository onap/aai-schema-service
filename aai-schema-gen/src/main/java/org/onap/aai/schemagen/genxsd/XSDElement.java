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

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.onap.aai.setup.SchemaVersion;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

public class XSDElement implements Element {
    Element xmlElementElement;
    String maxOccurs;
    private static final int VALUE_NONE = 0;
    private static final int VALUE_DESCRIPTION = 1;
    private static final int VALUE_INDEXED_PROPS = 2;
    private static final int VALUE_CONTAINER = 3;
    private static final int VALUE_REQUIRES = 4;
    private static final int VALUE_DSLSTARTNODE = 5;

    public XSDElement(Element xmlElementElement, String maxOccurs) {
        super();
        this.xmlElementElement = xmlElementElement;
        this.maxOccurs = maxOccurs;
    }

    public XSDElement(Element xmlElementElement) {
        super();
        this.xmlElementElement = xmlElementElement;
        this.maxOccurs = null;
    }

    public String name() {
        return this.getAttribute("name");
    }

    public Vector<String> getAddTypes(String version) {
        String apiVersionFmt = "." + version + ".";
        NamedNodeMap attributes = this.getAttributes();
        Vector<String> addTypeV = new Vector<>(); // vector of 1
        String addType = null;

        for (int j = 0; j < attributes.getLength(); ++j) {
            Attr attr = (Attr) attributes.item(j);
            String attrName = attr.getNodeName();

            String attrValue = attr.getNodeValue();
            if ("type".equals(attrName)) {
                if (attrValue.contains(apiVersionFmt)) {
                    addType = attrValue.substring(attrValue.lastIndexOf('.') + 1);
                    addTypeV.add(addType);
                }

            }
        }
        return addTypeV;
    }

    public String getRequiresProperty() {
        String elementAlsoRequiresProperty = null;
        NodeList xmlPropNodes = this.getElementsByTagName("xml-properties");

        for (int i = 0; i < xmlPropNodes.getLength(); ++i) {
            Element xmlPropElement = (Element) xmlPropNodes.item(i);
            if (!xmlPropElement.getParentNode().getAttributes().getNamedItem("name").getNodeValue()
                .equals(this.xmlElementElement.getAttribute("name"))) {
                continue;
            }
            NodeList childNodes = xmlPropElement.getElementsByTagName("xml-property");

            for (int j = 0; j < childNodes.getLength(); ++j) {
                Element childElement = (Element) childNodes.item(j);
                // get name
                int useValue = VALUE_NONE;
                NamedNodeMap attributes = childElement.getAttributes();
                for (int k = 0; k < attributes.getLength(); ++k) {
                    Attr attr = (Attr) attributes.item(k);
                    String attrName = attr.getNodeName();
                    String attrValue = attr.getNodeValue();
                    if (attrName == null || attrValue == null) {
                        continue;
                    }
                    if (attrName.equals("name") && attrValue.equals("requires")) {
                        useValue = VALUE_REQUIRES;
                    }
                    if (useValue == VALUE_REQUIRES && attrName.equals("value")) {
                        elementAlsoRequiresProperty = attrValue;
                    }
                }
            }
        }
        return elementAlsoRequiresProperty;
    }

    public String getPathDescriptionProperty() {
        String pathDescriptionProperty = null;
        NodeList xmlPropNodes = this.getElementsByTagName("xml-properties");

        for (int i = 0; i < xmlPropNodes.getLength(); ++i) {
            Element xmlPropElement = (Element) xmlPropNodes.item(i);
            if (!xmlPropElement.getParentNode().getAttributes().getNamedItem("name").getNodeValue()
                .equals(this.xmlElementElement.getAttribute("name"))) {
                continue;
            }
            // This stopped working, replaced with above - should figure out why...
            // if ( !xmlPropElement.getParentNode().isSameNode(this.xmlElementElement))
            // continue;
            NodeList childNodes = xmlPropElement.getElementsByTagName("xml-property");

            for (int j = 0; j < childNodes.getLength(); ++j) {
                Element childElement = (Element) childNodes.item(j);
                // get name
                int useValue = VALUE_NONE;
                NamedNodeMap attributes = childElement.getAttributes();
                for (int k = 0; k < attributes.getLength(); ++k) {
                    Attr attr = (Attr) attributes.item(k);
                    String attrName = attr.getNodeName();
                    String attrValue = attr.getNodeValue();
                    if (attrName == null || attrValue == null) {
                        continue;
                    }
                    if (attrName.equals("name") && attrValue.equals("description")) {
                        useValue = VALUE_DESCRIPTION;
                    }
                    if (useValue == VALUE_DESCRIPTION && attrName.equals("value")) {
                        pathDescriptionProperty = attrValue;
                    }
                }
            }
        }
        if (pathDescriptionProperty != null) {
            // suppress non-printable characters in a description
            String replaceDescription = pathDescriptionProperty.replaceAll("[^\\p{ASCII}]", "");
            return replaceDescription;
        }
        return pathDescriptionProperty;
    }

    public Vector<String> getProps(int needValue) {
        Vector<String> props = new Vector<String>();
        NodeList xmlPropNodes = this.getElementsByTagName("xml-properties");

        for (int i = 0; i < xmlPropNodes.getLength(); ++i) {
            Element xmlPropElement = (Element) xmlPropNodes.item(i);
            if (!xmlPropElement.getParentNode().isSameNode(this.xmlElementElement)) {
                continue;
            }
            NodeList childNodes = xmlPropElement.getElementsByTagName("xml-property");
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Element childElement = (Element) childNodes.item(j);
                // get name
                int useValue = VALUE_NONE;
                NamedNodeMap attributes = childElement.getAttributes();
                for (int k = 0; k < attributes.getLength(); ++k) {
                    Attr attr = (Attr) attributes.item(k);
                    String attrName = attr.getNodeName();
                    String attrValue = attr.getNodeValue();
                    if (attrName == null || attrValue == null) {
                        continue;
                    }
                    if (needValue == VALUE_INDEXED_PROPS && attrValue.equals("indexedProps")) {
                        useValue = VALUE_INDEXED_PROPS;
                    } else if (needValue == VALUE_DSLSTARTNODE
                        && attrValue.equals("dslStartNodeProps")) {
                        useValue = VALUE_DSLSTARTNODE;
                    }
                    if (useValue != VALUE_NONE && attrName.equals("value")) {
                        props = getProps(attrValue);
                    }
                }
            }
        }
        return props;
    }

    private static Vector<String> getProps(String attrValue) {
        if (attrValue == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(attrValue, ",");
        if (st.countTokens() == 0) {
            return null;
        }
        Vector<String> result = new Vector<String>();
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }

    public Vector<String> getIndexedProps() {
        return getProps(VALUE_INDEXED_PROPS);
    }

    public Vector<String> getDslStartNodeProps() {
        return getProps(VALUE_DSLSTARTNODE);
    }

    public String getContainerProperty() {
        NodeList xmlPropNodes = this.getElementsByTagName("xml-properties");
        String container = null;
        for (int i = 0; i < xmlPropNodes.getLength(); ++i) {
            Element xmlPropElement = (Element) xmlPropNodes.item(i);
            if (!xmlPropElement.getParentNode().isSameNode(this.xmlElementElement)) {
                continue;
            }
            NodeList childNodes = xmlPropElement.getElementsByTagName("xml-property");
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Element childElement = (Element) childNodes.item(j);
                // get name
                int useValue = VALUE_NONE;
                NamedNodeMap attributes = childElement.getAttributes();
                for (int k = 0; k < attributes.getLength(); ++k) {
                    Attr attr = (Attr) attributes.item(k);
                    String attrName = attr.getNodeName();
                    String attrValue = attr.getNodeValue();
                    if (attrName == null || attrValue == null) {
                        continue;
                    }
                    if (useValue == VALUE_CONTAINER && attrName.equals("value")) {
                        container = attrValue;
                    }
                    if (attrValue.equals("container")) {
                        useValue = VALUE_CONTAINER;
                    }
                }
            }
        }
        return container;
    }

    public String getQueryParamYAML() {
        StringBuilder sbParameter = new StringBuilder();
        sbParameter.append("        - name: ").append(this.getAttribute("name")).append("\n");
        sbParameter.append(("          in: query\n"));
        if (this.getAttribute("description") != null
            && this.getAttribute("description").length() > 0) {
            sbParameter.append("          description: ").append(this.getAttribute("description"))
                .append("\n");
        } else {
            sbParameter.append(("          description: n/a\n"));
        }
        sbParameter.append(("          required: false\n"));
        if (("java.lang.String").equals(this.getAttribute("type"))) {
            sbParameter.append("          type: string\n");
        }
        if (("java.lang.Long").equals(this.getAttribute("type"))) {
            sbParameter.append("          type: integer\n");
            sbParameter.append("          format: int64\n");
        }
        if (("java.lang.Integer").equals(this.getAttribute("type"))) {
            sbParameter.append("          type: integer\n");
            sbParameter.append("          format: int32\n");
        }
        if (("java.lang.Float").equals(this.getAttribute("type"))) {
            sbParameter.append("          type: number\n");
            sbParameter.append("          format: float\n");
        }
        if (("java.lang.Boolean").equals(this.getAttribute("type"))) {
            sbParameter.append("          type: boolean\n");
        }
        return sbParameter.toString();
    }

    public String getPathParamYAML(String elementDescription) {
        return getPathParamYAML(elementDescription, null);
    }

    public String getPathParamYAML(String elementDescription, String overrideName) {
        // updated to allow caller to provide parameter name to use in API
        StringBuilder sbParameter = new StringBuilder();
        if (overrideName == null) {
            overrideName = this.getAttribute("name");
        }
        sbParameter.append("        - name: ").append(overrideName).append("\n");
        sbParameter.append(("          in: path\n"));
        if (elementDescription != null && elementDescription.length() > 0) {
            sbParameter.append("          description: ").append(elementDescription).append("\n");
        }
        sbParameter.append(("          required: true\n"));
        if (("java.lang.String").equals(this.getAttribute("type"))) {
            sbParameter.append("          type: string\n");
        }
        if (("java.lang.Long").equals(this.getAttribute("type"))) {
            sbParameter.append("          type: integer\n");
            sbParameter.append("          format: int64\n");
        }
        if (("java.lang.Integer").equals(this.getAttribute("type"))) {
            sbParameter.append("          type: integer\n");
            sbParameter.append("          format: int32\n");
        }
        if (("java.lang.Boolean").equals(this.getAttribute("type"))) {
            sbParameter.append("          type: boolean\n");
        }
        if (StringUtils.isNotBlank(this.getAttribute("name"))) {
            sbParameter.append("          example: " + "__")
                .append(this.getAttribute("name").toUpperCase()).append("__").append("\n");
        }
        return sbParameter.toString();
    }

    public String getHTMLElement(SchemaVersion v, boolean useAnnotation, HTMLfromOXM driver) {
        StringBuilder sbElement = new StringBuilder();
        String elementName = this.getAttribute("name");
        String elementType = this.getAttribute("type");
        String elementContainerType = this.getAttribute("container-type");
        String elementIsRequired = this.getAttribute("required");
        String addType = elementType.contains("." + v.toString() + ".")
            ? elementType.substring(elementType.lastIndexOf('.') + 1)
            : null;

        if (addType != null) {
            sbElement.append("        <xs:element ref=\"tns:")
                .append(driver.getXmlRootElementName(addType)).append("\"");
        } else {
            sbElement.append("        <xs:element name=\"").append(elementName).append("\"");
        }
        if (elementType.equals("java.lang.String")) {
            sbElement.append(" type=\"xs:string\"");
        }
        if (elementType.equals("java.lang.Long")) {
            sbElement.append(" type=\"xs:unsignedInt\"");
        }
        if (elementType.equals("java.lang.Integer")) {
            sbElement.append(" type=\"xs:int\"");
        }
        if (elementType.equals("java.lang.Float")) {
            sbElement.append(" type=\"xs:float\"");
        }
        if (elementType.equals("java.lang.Double")) {
            sbElement.append(" type=\"xs:double\"");
        }
        if (elementType.equals("java.lang.Boolean")) {
            sbElement.append(" type=\"xs:boolean\"");
        }
        if (addType != null || elementType.startsWith("java.lang.")) {
            sbElement.append(" minOccurs=\"0\"");
        }
        if (elementContainerType != null && elementContainerType.equals("java.util.ArrayList")) {
            sbElement.append(" maxOccurs=\"").append(maxOccurs).append("\"");
        }
        if (useAnnotation) {
            String annotation = new XSDElement(xmlElementElement, maxOccurs)
                .getHTMLAnnotation("field", "          ");
            sbElement.append(
                StringUtils.isNotEmpty(annotation) ? ">" + OxmFileProcessor.LINE_SEPARATOR : "");
            sbElement.append(annotation);
            sbElement.append(StringUtils.isNotEmpty(annotation)
                ? "        </xs:element>" + OxmFileProcessor.LINE_SEPARATOR
                : "/>" + OxmFileProcessor.LINE_SEPARATOR);
        } else {
            sbElement.append("/>").append(OxmFileProcessor.LINE_SEPARATOR);
        }
        return this.getHTMLElementWrapper(sbElement.toString(), v, useAnnotation);
        // return sbElement.toString();
    }

    public String getHTMLElementWrapper(String unwrappedElement, SchemaVersion v,
        boolean useAnnotation) {

        NodeList childNodes = this.getElementsByTagName("xml-element-wrapper");

        String xmlElementWrapper = null;
        if (childNodes.getLength() > 0) {
            Element childElement = (Element) childNodes.item(0);
            // get name
            xmlElementWrapper = childElement == null ? null : childElement.getAttribute("name");
        }
        if (xmlElementWrapper == null) {
            return unwrappedElement;
        }

        StringBuilder sbElement = new StringBuilder();
        sbElement.append("        <xs:element name=\"").append(xmlElementWrapper).append("\"");
        String elementType = xmlElementElement.getAttribute("type");
        String elementIsRequired = this.getAttribute("required");
        String addType = elementType.contains("." + v.toString() + ".")
            ? elementType.substring(elementType.lastIndexOf('.') + 1)
            : null;

        if (elementIsRequired == null || !elementIsRequired.equals("true") || addType != null) {
            sbElement.append(" minOccurs=\"0\"");
        }
        sbElement.append(">").append(OxmFileProcessor.LINE_SEPARATOR);
        sbElement.append("          <xs:complexType>").append(OxmFileProcessor.LINE_SEPARATOR);
        if (useAnnotation) {
            XSDElement javaTypeElement = new XSDElement((Element) this.getParentNode(), maxOccurs);
            sbElement.append(javaTypeElement.getHTMLAnnotation("class", "            "));
        }
        sbElement.append("            <xs:sequence>").append(OxmFileProcessor.LINE_SEPARATOR);
        sbElement.append("      ");
        sbElement.append(unwrappedElement);
        sbElement.append("            </xs:sequence>").append(OxmFileProcessor.LINE_SEPARATOR);
        sbElement.append("          </xs:complexType>").append(OxmFileProcessor.LINE_SEPARATOR);
        sbElement.append("        </xs:element>").append(OxmFileProcessor.LINE_SEPARATOR);
        return sbElement.toString();
    }

    public String getHTMLAnnotation(String target, String indentation) {
        StringBuilder sb = new StringBuilder();
        List<String> metadata = new ArrayList<>();
        if ("true".equals(this.getAttribute("xml-key"))) {
            metadata.add("isKey=true");
        }

        NodeList xmlPropTags = this.getElementsByTagName("xml-properties");
        Element xmlPropElement = null;
        for (int i = 0; i < xmlPropTags.getLength(); ++i) {
            xmlPropElement = (Element) xmlPropTags.item(i);
            if (xmlPropElement.getParentNode().getAttributes().getNamedItem("name").getNodeValue()
                .equals(this.xmlElementElement.getAttribute("name"))) {
                break;
            }
        }
        if (xmlPropElement != null) {
            NodeList xmlProperties = xmlPropElement.getElementsByTagName("xml-property");
            for (int i = 0; i < xmlProperties.getLength(); i++) {
                Element item = (Element) xmlProperties.item(i);
                String name = item.getAttribute("name");
                String value = item.getAttribute("value");
                if (name.equals("abstract")) {
                    name = "isAbstract";
                } else if (name.equals("extends")) {
                    name = "extendsFrom";
                }
                metadata.add(name + "=\"" + value.replaceAll("&", "&amp;") + "\"");
            }
        }
        if (metadata.size() == 0) {
            return "";
        }
        sb.append(indentation).append("<xs:annotation>").append(OxmFileProcessor.LINE_SEPARATOR);
        sb.append(indentation).append("  <xs:appinfo>").append(OxmFileProcessor.LINE_SEPARATOR)
            .append(indentation).append("    <annox:annotate target=\"").append(target)
            .append("\">@org.onap.aai.annotations.Metadata(").append(Joiner.on(",").join(metadata))
            .append(")</annox:annotate>").append(OxmFileProcessor.LINE_SEPARATOR)
            .append(indentation).append("  </xs:appinfo>").append(OxmFileProcessor.LINE_SEPARATOR);
        sb.append(indentation).append("</xs:annotation>").append(OxmFileProcessor.LINE_SEPARATOR);
        return sb.toString();
    }

    public String getTypePropertyYAML(boolean isDslStartNode) {
        StringBuilder sbProperties = new StringBuilder();
        sbProperties.append("      ").append(this.getAttribute("name")).append(":\n");
        sbProperties.append("        type: ");

        if (("java.lang.String").equals(this.getAttribute("type"))) {
            sbProperties.append("string\n");
        } else if (("java.lang.Long").equals(this.getAttribute("type"))) {
            sbProperties.append("integer\n");
            sbProperties.append("        format: int64\n");
        } else if (("java.lang.Integer").equals(this.getAttribute("type"))) {
            sbProperties.append("integer\n");
            sbProperties.append("        format: int32\n");
        } else if (("java.lang.Boolean").equals(this.getAttribute("type"))) {
            sbProperties.append("boolean\n");
        }
        String attrDescription = this.getPathDescriptionProperty();
        if (attrDescription != null && attrDescription.length() > 0) {
            if (!isDslStartNode) {
                sbProperties.append("        description: ").append(attrDescription).append("\n");
            } else {
                sbProperties.append("        description: |\n");
                sbProperties.append("          ").append(attrDescription).append("\n");
                sbProperties.append(
                    "          *This property can be used as a filter to find the start node for a dsl query\n");
            }
        } else {
            if (isDslStartNode) {
                sbProperties.append("        description: |\n");
                sbProperties.append("          \n");
                sbProperties.append(
                    "          *This property can be used as a filter to find the start node for a dsl query\n");
            }
        }
        String elementAlsoRequiresProperty = this.getRequiresProperty();
        if (StringUtils.isNotEmpty(elementAlsoRequiresProperty)) {
            sbProperties.append("        also requires: ").append(elementAlsoRequiresProperty)
                .append("\n");
        }
        return sbProperties.toString();
    }

    public boolean isStandardType() {
        switch (this.getAttribute("type")) {
            case "java.lang.String":
            case "java.lang.Long":
            case "java.lang.Integer":
            case "java.lang.Boolean":
                return true;
        }
        return false;
    }

    @Override
    public String getNodeName() {
        return xmlElementElement.getNodeName();
    }

    @Override
    public String getNodeValue() throws DOMException {
        return xmlElementElement.getNodeValue();
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException {
        xmlElementElement.setNodeValue(nodeValue);
    }

    @Override
    public short getNodeType() {
        return xmlElementElement.getNodeType();
    }

    @Override
    public Node getParentNode() {
        return xmlElementElement.getParentNode();
    }

    @Override
    public NodeList getChildNodes() {
        return xmlElementElement.getChildNodes();
    }

    @Override
    public Node getFirstChild() {
        return xmlElementElement.getFirstChild();
    }

    @Override
    public Node getLastChild() {
        return xmlElementElement.getLastChild();
    }

    @Override
    public Node getPreviousSibling() {
        return xmlElementElement.getPreviousSibling();
    }

    @Override
    public Node getNextSibling() {
        return xmlElementElement.getNextSibling();
    }

    @Override
    public NamedNodeMap getAttributes() {
        return xmlElementElement.getAttributes();
    }

    @Override
    public Document getOwnerDocument() {
        return xmlElementElement.getOwnerDocument();
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return xmlElementElement.insertBefore(newChild, refChild);
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return xmlElementElement.replaceChild(newChild, oldChild);
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException {
        return xmlElementElement.removeChild(oldChild);
    }

    @Override
    public Node appendChild(Node newChild) throws DOMException {
        return xmlElementElement.appendChild(newChild);
    }

    @Override
    public boolean hasChildNodes() {
        return xmlElementElement.hasChildNodes();
    }

    @Override
    public Node cloneNode(boolean deep) {
        return xmlElementElement.cloneNode(deep);
    }

    @Override
    public void normalize() {
        xmlElementElement.normalize();
    }

    @Override
    public boolean isSupported(String feature, String version) {
        return xmlElementElement.isSupported(feature, version);
    }

    @Override
    public String getNamespaceURI() {
        return xmlElementElement.getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        return xmlElementElement.getPrefix();
    }

    @Override
    public void setPrefix(String prefix) throws DOMException {
        xmlElementElement.setPrefix(prefix);
    }

    @Override
    public String getLocalName() {

        return xmlElementElement.getLocalName();
    }

    @Override
    public boolean hasAttributes() {
        return xmlElementElement.hasAttributes();
    }

    @Override
    public String getBaseURI() {
        return xmlElementElement.getBaseURI();
    }

    @Override
    public short compareDocumentPosition(Node other) throws DOMException {
        return xmlElementElement.compareDocumentPosition(other);
    }

    @Override
    public String getTextContent() throws DOMException {
        return xmlElementElement.getTextContent();
    }

    @Override
    public void setTextContent(String textContent) throws DOMException {
        xmlElementElement.setTextContent(textContent);
    }

    @Override
    public boolean isSameNode(Node other) {
        return xmlElementElement.isSameNode(other);
    }

    @Override
    public String lookupPrefix(String namespaceURI) {
        return xmlElementElement.lookupPrefix(namespaceURI);
    }

    @Override
    public boolean isDefaultNamespace(String namespaceURI) {
        return xmlElementElement.isDefaultNamespace(namespaceURI);
    }

    @Override
    public String lookupNamespaceURI(String prefix) {
        return xmlElementElement.lookupNamespaceURI(prefix);
    }

    @Override
    public boolean isEqualNode(Node arg) {
        return xmlElementElement.isEqualNode(arg);
    }

    @Override
    public Object getFeature(String feature, String version) {
        return xmlElementElement.getFeature(feature, version);
    }

    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return xmlElementElement.setUserData(key, data, handler);
    }

    @Override
    public Object getUserData(String key) {
        return xmlElementElement.getUserData(key);
    }

    @Override
    public String getTagName() {
        return xmlElementElement.getTagName();
    }

    @Override
    public String getAttribute(String name) {
        return xmlElementElement.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, String value) throws DOMException {
        xmlElementElement.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) throws DOMException {
        xmlElementElement.removeAttribute(name);
    }

    @Override
    public Attr getAttributeNode(String name) {
        return xmlElementElement.getAttributeNode(name);
    }

    @Override
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        return xmlElementElement.setAttributeNode(newAttr);
    }

    @Override
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        return xmlElementElement.removeAttributeNode(oldAttr);
    }

    @Override
    public NodeList getElementsByTagName(String name) {
        return xmlElementElement.getElementsByTagName(name);
    }

    @Override
    public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
        return xmlElementElement.getAttributeNS(namespaceURI, localName);
    }

    @Override
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value)
        throws DOMException {
        xmlElementElement.setAttributeNS(namespaceURI, qualifiedName, value);
    }

    @Override
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        xmlElementElement.removeAttributeNS(namespaceURI, localName);
    }

    @Override
    public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
        return xmlElementElement.getAttributeNodeNS(namespaceURI, localName);
    }

    @Override
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        return xmlElementElement.setAttributeNodeNS(newAttr);
    }

    @Override
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
        throws DOMException {
        return xmlElementElement.getElementsByTagNameNS(namespaceURI, localName);
    }

    @Override
    public boolean hasAttribute(String name) {
        return xmlElementElement.hasAttribute(name);
    }

    @Override
    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
        return xmlElementElement.hasAttributeNS(namespaceURI, localName);
    }

    @Override
    public TypeInfo getSchemaTypeInfo() {
        return xmlElementElement.getSchemaTypeInfo();
    }

    @Override
    public void setIdAttribute(String name, boolean isId) throws DOMException {
        xmlElementElement.setIdAttribute(name, isId);

    }

    @Override
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId)
        throws DOMException {
        xmlElementElement.setIdAttributeNS(namespaceURI, localName, isId);
    }

    @Override
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        xmlElementElement.setIdAttributeNode(idAttr, isId);
    }

}
