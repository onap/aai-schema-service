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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.onap.aai.schemagen.yaml.YamlBlock;
import org.onap.aai.schemagen.yaml.YamlMapping;
import org.onap.aai.schemagen.yaml.YamlSequence;
import org.onap.aai.schemagen.yaml.YamlSerializer;
import org.onap.aai.setup.SchemaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An OXM {@code <java-type>} or {@code <xml-element>} enriched with the readers and emitters the
 * generators need.
 *
 * <p>
 * This <em>wraps</em> a DOM element rather than implementing {@link Element}. Callers only ever ask
 * it for a handful of attributes and child lists, so the ~40 pass-through methods the is-a relation
 * demanded carried no weight; {@link #getElement()} hands out the wrapped element for the few
 * places
 * that genuinely need a DOM node.
 */
public class XSDElement {
    private static final Logger logger = LoggerFactory.getLogger(XSDElement.class);

    Element xmlElementElement;
    String maxOccurs;
    private static final int VALUE_NONE = 0;
    private static final int VALUE_DESCRIPTION = 1;
    private static final int VALUE_INDEXED_PROPS = 2;
    private static final int VALUE_CONTAINER = 3;
    private static final int VALUE_REQUIRES = 4;
    private static final int VALUE_DSLSTARTNODE = 5;

    /** OXM {@code <xml-property>} name for the minimum length of a string property. */
    private static final String FACET_MIN_LENGTH = "minLength";
    /** OXM {@code <xml-property>} name for the maximum length of a string property. */
    private static final String FACET_MAX_LENGTH = "maxLength";
    /** OXM {@code <xml-property>} name for the regular expression a string must match. */
    private static final String FACET_PATTERN = "pattern";
    /** OXM {@code <xml-property>} name for the comma separated list of permitted values. */
    private static final String FACET_ALLOWED_VALUES = "allowedValues";
    /** OXM {@code <xml-property>} name for the inclusive lower bound of a numeric property. */
    private static final String FACET_MINIMUM = "minimum";
    /** OXM {@code <xml-property>} name for the inclusive upper bound of a numeric property. */
    private static final String FACET_MAXIMUM = "maximum";

    /**
     * All constraint facet property names, in the order they are emitted. Length before pattern
     * before enumeration before range keeps both generated artefacts stable and readable.
     */
    private static final List<String> FACET_NAMES =
        Collections.unmodifiableList(Arrays.asList(FACET_MIN_LENGTH, FACET_MAX_LENGTH,
            FACET_PATTERN, FACET_ALLOWED_VALUES, FACET_MINIMUM, FACET_MAXIMUM));

    /** Facets that XML Schema allows on {@code xs:string}. */
    private static final Set<String> STRING_FACETS =
        Set.of(FACET_MIN_LENGTH, FACET_MAX_LENGTH, FACET_PATTERN, FACET_ALLOWED_VALUES);

    /** Facets that XML Schema allows on the numeric types this generator emits. */
    private static final Set<String> NUMERIC_FACETS =
        Set.of(FACET_MINIMUM, FACET_MAXIMUM, FACET_ALLOWED_VALUES);

    /**
     * The XML Schema facet element each OXM property name maps to. {@code allowedValues} is absent
     * because it expands to one {@code xs:enumeration} per value rather than a single facet.
     */
    private static final Map<String, String> XSD_FACET_NAMES =
        Map.of(FACET_MIN_LENGTH, "xs:minLength", FACET_MAX_LENGTH, "xs:maxLength", FACET_PATTERN,
            "xs:pattern", FACET_MINIMUM, "xs:minInclusive", FACET_MAXIMUM, "xs:maxInclusive");

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

    /**
     * The value of one constraint facet {@code <xml-property>} declared on this element, or
     * {@code null} when the element does not declare it.
     *
     * <p>
     * Only {@code <xml-properties>} belonging to this element are considered - the DOM
     * {@code getElementsByTagName} lookup is recursive, so a nested element's properties would
     * otherwise leak in, which is the same parent check the neighbouring readers make.
     */
    private String getFacet(String facetName) {
        NodeList xmlPropNodes = this.getElementsByTagName("xml-properties");
        String facetValue = null;
        for (int i = 0; i < xmlPropNodes.getLength(); ++i) {
            Element xmlPropElement = (Element) xmlPropNodes.item(i);
            if (!xmlPropElement.getParentNode().isSameNode(this.xmlElementElement)) {
                continue;
            }
            NodeList childNodes = xmlPropElement.getElementsByTagName("xml-property");
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Element childElement = (Element) childNodes.item(j);
                if (facetName.equals(childElement.getAttribute("name"))) {
                    String value = childElement.getAttribute("value");
                    if (StringUtils.isNotEmpty(value)) {
                        facetValue = value;
                    }
                }
            }
        }
        return facetValue;
    }

    /**
     * The facets XML Schema (and swagger) allow for this element's type. A facet that is not legal
     * for the type is silently skipped rather than emitted, so a mistake in the OXM cannot produce
     * an invalid XSD or swagger document. {@code xs:boolean} and node-type references take no
     * facets.
     */
    private Set<String> legalFacets() {
        switch (this.getAttribute("type")) {
            case "java.lang.String":
                return STRING_FACETS;
            case "java.lang.Long":
            case "java.lang.Integer":
            case "java.lang.Float":
            case "java.lang.Double":
                return NUMERIC_FACETS;
            default:
                return Collections.emptySet();
        }
    }

    /**
     * The constraint facets this element declares that are also legal for its type, in emission
     * order. Empty when the element declares none, declares only facets that do not apply to its
     * type, or is not of a standard type at all.
     */
    private List<String> declaredFacets() {
        Set<String> legalFacets = legalFacets();
        List<String> declared = new ArrayList<>();
        for (String facetName : FACET_NAMES) {
            if (legalFacets.contains(facetName) && getFacet(facetName) != null) {
                declared.add(facetName);
            }
        }
        return declared;
    }

    /**
     * Splits an {@code allowedValues} property into its individual values, trimming each and
     * dropping empties, so {@code "a, b ,, c"} yields {@code [a, b, c]}.
     */
    private static List<String> splitAllowedValues(String allowedValues) {
        List<String> values = new ArrayList<>();
        for (String value : allowedValues.split(",")) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                values.add(trimmed);
            }
        }
        return values;
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
        return serializeParameter(queryParam());
    }

    public String getPathParamYAML(String elementDescription) {
        return getPathParamYAML(elementDescription, null);
    }

    public String getPathParamYAML(String elementDescription, String overrideName) {
        return serializeParameter(pathParam(elementDescription, overrideName));
    }

    /** This element as a query parameter, which is never required. */
    public YamlMapping queryParam() {
        return parameter("query", this.getAttribute("name"), this.getAttribute("description"),
            false);
    }

    /**
     * This element as a path parameter, which always is required.
     *
     * @param overrideName the name to use in the API, or {@code null} for the schema property's own
     *        name - a child whose name already appears in the path is qualified by its caller
     */
    public YamlMapping pathParam(String elementDescription, String overrideName) {
        return parameter("path", overrideName == null ? this.getAttribute("name") : overrideName,
            elementDescription, true);
    }

    /**
     * One entry of an operation's {@code parameters:} sequence. A path parameter is required and a
     * query parameter is not, which is the only structural difference between the two.
     */
    private YamlMapping parameter(String in, String name, String description, boolean required) {
        YamlMapping parameter = new YamlMapping().entry("name", name).entry("in", in);
        if (description != null && description.length() > 0) {
            parameter.entry("description", description);
        }
        parameter.entry("required", Boolean.toString(required));
        addParameterType(parameter);
        return parameter;
    }

    /**
     * A single parameter rendered as the fragment its callers still concatenate: one item of an
     * operation's parameters sequence, which sits four levels in.
     */
    private static String serializeParameter(YamlMapping parameter) {
        return YamlSerializer.serialize(new YamlSequence().item(parameter), 4);
    }

    /**
     * The swagger {@code type} and (optional) {@code format} for a Java primitive-wrapper type.
     * Single source of truth for the mapping that was previously spelled out in
     * {@link #getQueryParamYAML()}, {@link #getPathParamYAML(String, String)} and
     * {@link #getTypePropertyYAML(boolean)}.
     */
    private record SwaggerType(String type, String format) {
    }

    /**
     * Maps a Java type name to its swagger type/format, or {@code null} for a non-standard type
     * (mirroring {@link #isStandardType()} — callers emit nothing for a non-standard type).
     */
    private static SwaggerType swaggerTypeFor(String javaType) {
        switch (javaType) {
            case "java.lang.String":
                return new SwaggerType("string", null);
            case "java.lang.Long":
                return new SwaggerType("integer", "int64");
            case "java.lang.Integer":
                return new SwaggerType("integer", "int32");
            case "java.lang.Float":
                return new SwaggerType("number", "float");
            case "java.lang.Double":
                return new SwaggerType("number", "double");
            case "java.lang.Boolean":
                return new SwaggerType("boolean", null);
            default:
                return null;
        }
    }

    /**
     * Adds the swagger {@code type}/{@code format} entries for a path or query parameter. Shared by
     * {@link #getQueryParamYAML()} and {@link #getPathParamYAML(String, String)}, which previously
     * duplicated this mapping verbatim. Non-standard types add nothing, exactly as before.
     */
    private void addParameterType(YamlMapping parameter) {
        SwaggerType swaggerType = swaggerTypeFor(this.getAttribute("type"));
        if (swaggerType == null) {
            return;
        }
        parameter.entry("type", swaggerType.type());
        if (swaggerType.format() != null) {
            parameter.entry("format", swaggerType.format());
        }
    }

    /**
     * The XML Schema built-in type this generator uses for a Java primitive-wrapper type, or
     * {@code null} for anything else (a node-type reference or a container).
     */
    private static String xsdTypeFor(String javaType) {
        switch (javaType) {
            case "java.lang.String":
                return "xs:string";
            case "java.lang.Long":
                return "xs:unsignedInt";
            case "java.lang.Integer":
                return "xs:int";
            case "java.lang.Float":
                return "xs:float";
            case "java.lang.Double":
                return "xs:double";
            case "java.lang.Boolean":
                return "xs:boolean";
            default:
                return null;
        }
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

        String xsdType = xsdTypeFor(elementType);
        List<String> facets = declaredFacets();
        if (addType != null && !facets.isEmpty()) {
            // a reference carries the referenced node type's own definition, so there is nothing
            // here to restrict
            logger.warn(
                "ignoring constraint facets {} on element {}: they do not apply to a reference to node type {}",
                facets, elementName, addType);
            facets = Collections.emptyList();
        }
        // an xs:element cannot carry both a type attribute and an inline xs:simpleType, so a
        // restricted element states its base type inside the restriction instead
        boolean restrictType = xsdType != null && addType == null && !facets.isEmpty();

        if (addType != null) {
            sbElement.append("        <xs:element ref=\"tns:")
                .append(driver.getXmlRootElementName(addType)).append("\"");
        } else {
            sbElement.append("        <xs:element name=\"").append(elementName).append("\"");
        }
        if (xsdType != null && !restrictType) {
            sbElement.append(" type=\"").append(xsdType).append("\"");
        }
        if (addType != null || elementType.startsWith("java.lang.")) {
            sbElement.append(" minOccurs=\"0\"");
        }
        if (elementContainerType != null && elementContainerType.equals("java.util.ArrayList")) {
            sbElement.append(" maxOccurs=\"").append(maxOccurs).append("\"");
        }
        String annotation = useAnnotation
            ? new XSDElement(xmlElementElement, maxOccurs).getHTMLAnnotation("field", "          ")
            : "";
        if (restrictType) {
            // xs:annotation has to come first, the content model of xs:element is ordered
            sbElement.append(">").append(OxmFileProcessor.LINE_SEPARATOR).append(annotation);
            appendSimpleTypeRestriction(sbElement, xsdType, facets, "          ");
            sbElement.append("        </xs:element>").append(OxmFileProcessor.LINE_SEPARATOR);
        } else if (StringUtils.isNotEmpty(annotation)) {
            sbElement.append(">").append(OxmFileProcessor.LINE_SEPARATOR).append(annotation)
                .append("        </xs:element>").append(OxmFileProcessor.LINE_SEPARATOR);
        } else {
            sbElement.append("/>").append(OxmFileProcessor.LINE_SEPARATOR);
        }
        return this.getHTMLElementWrapper(sbElement.toString(), v, useAnnotation);
        // return sbElement.toString();
    }

    /**
     * Appends the inline {@code xs:simpleType} that restricts {@code xsdType} by the given facets.
     * Only facets legal for the base type reach here (see {@link #declaredFacets()}), so every
     * facet
     * emitted is one a validating parser accepts.
     */
    private void appendSimpleTypeRestriction(StringBuilder sbElement, String xsdType,
        List<String> facets, String indentation) {
        sbElement.append(indentation).append("<xs:simpleType>")
            .append(OxmFileProcessor.LINE_SEPARATOR);
        sbElement.append(indentation).append("  <xs:restriction base=\"").append(xsdType)
            .append("\">").append(OxmFileProcessor.LINE_SEPARATOR);
        for (String facetName : facets) {
            String facetValue = getFacet(facetName);
            if (FACET_ALLOWED_VALUES.equals(facetName)) {
                for (String value : splitAllowedValues(facetValue)) {
                    appendFacet(sbElement, indentation, "xs:enumeration", value);
                }
            } else {
                appendFacet(sbElement, indentation, XSD_FACET_NAMES.get(facetName), facetValue);
            }
        }
        sbElement.append(indentation).append("  </xs:restriction>")
            .append(OxmFileProcessor.LINE_SEPARATOR);
        sbElement.append(indentation).append("</xs:simpleType>")
            .append(OxmFileProcessor.LINE_SEPARATOR);
    }

    private void appendFacet(StringBuilder sbElement, String indentation, String xsdFacetName,
        String facetValue) {
        sbElement.append(indentation).append("    <").append(xsdFacetName).append(" value=\"")
            .append(escapeXmlAttribute(facetValue)).append("\"/>")
            .append(OxmFileProcessor.LINE_SEPARATOR);
    }

    /**
     * Escapes a value for use inside a double quoted XML attribute. A facet value is author
     * supplied
     * - a pattern in particular routinely contains characters that would otherwise end the
     * attribute or start a markup construct.
     */
    private static String escapeXmlAttribute(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"",
            "&quot;");
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
        YamlMapping properties = new YamlMapping();
        addTypeProperty(properties, isDslStartNode);
        // one entry of a definition's properties block, which sits three levels in
        return YamlSerializer.serialize(properties, 3);
    }

    /**
     * Adds this element as one entry of a definition's {@code properties:} block: its swagger type,
     * the constraint facets it declares, and its description.
     *
     * @param properties the block to add to, so that the entry is placed by nesting rather than at
     *        a
     *        stated indentation
     */
    public void addTypeProperty(YamlMapping properties, boolean isDslStartNode) {
        YamlMapping property = new YamlMapping();

        SwaggerType swaggerType = swaggerTypeFor(this.getAttribute("type"));
        if (swaggerType != null) {
            property.entry("type", swaggerType.type());
            if (swaggerType.format() != null) {
                property.entry("format", swaggerType.format());
            }
        } else {
            // a non-standard type still opens the key and leaves it without a value or a line
            // ending, so whatever follows continues on its line - long standing output that the
            // byte-identity constraint keeps in place
            property.fragment("type: ");
        }
        addPropertyFacetsYAML(property);
        String attrDescription = this.getPathDescriptionProperty();
        boolean hasDescription = attrDescription != null && attrDescription.length() > 0;
        if (hasDescription && !isDslStartNode) {
            property.entry("description", attrDescription);
        } else if (hasDescription || isDslStartNode) {
            // the dsl note has to sit on its own line, so the description becomes a block scalar
            property.entry("description",
                new YamlBlock().line(hasDescription ? attrDescription : "").line(
                    "*This property can be used as a filter to find the start node for a dsl query"));
        }
        properties.entry(this.getAttribute("name"), property);
    }

    /**
     * Appends the swagger validation keywords for the constraint facets this element declares. All
     * six are Swagger 2.0 keywords, so no {@code x-} extension is needed. Nothing is appended when
     * the element declares no facet that applies to its type, which is why generating a schema
     * version whose OXM carries no facet is byte-for-byte unchanged.
     */
    private void addPropertyFacetsYAML(YamlMapping property) {
        for (String facetName : declaredFacets()) {
            String facetValue = getFacet(facetName);
            if (FACET_ALLOWED_VALUES.equals(facetName)) {
                YamlSequence values = new YamlSequence();
                splitAllowedValues(facetValue).forEach(values::item);
                // the indicators are aligned with the key rather than indented past it, which YAML
                // allows and these documents use
                property.entryAtShiftedDepth("enum", values, -1);
            } else if (FACET_PATTERN.equals(facetName)) {
                // a regular expression is full of YAML-significant characters, so it is always
                // single quoted, with any embedded single quote doubled as YAML requires
                property.entry("pattern", "'" + facetValue.replace("'", "''") + "'");
            } else {
                property.entry(facetName, facetValue);
            }
        }
    }

    public boolean isStandardType() {
        switch (this.getAttribute("type")) {
            case "java.lang.String":
            case "java.lang.Long":
            case "java.lang.Integer":
            case "java.lang.Float":
            case "java.lang.Double":
            case "java.lang.Boolean":
                return true;
        }
        return false;
    }

    /**
     * The wrapped DOM element. For the callers that hand this element on to DOM-typed code or
     * construct another wrapper around it.
     */
    public Element getElement() {
        return xmlElementElement;
    }

    public String getAttribute(String name) {
        return xmlElementElement.getAttribute(name);
    }

    public boolean hasAttribute(String name) {
        return xmlElementElement.hasAttribute(name);
    }

    public NamedNodeMap getAttributes() {
        return xmlElementElement.getAttributes();
    }

    public NodeList getElementsByTagName(String name) {
        return xmlElementElement.getElementsByTagName(name);
    }

    public Node getParentNode() {
        return xmlElementElement.getParentNode();
    }

}
