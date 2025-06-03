/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright © 2025 Deutsche Telekom.
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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.aai.setup.SchemaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsIn.in;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Every.everyItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.fail;

public class XSDElementTest {
    private static final Logger logger = LoggerFactory.getLogger("XSDElementTest.class");
    private static final int maxSizeForXml = 20000;
    protected String testXML;
    protected Document doc = null;
    protected NodeList javaTypeNodes = null;

    private Element xmlElementElement;
    private XSDElement xsdelement;

    public String getTestXML() {
        return testXML;
    }

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
    }

    @BeforeEach
    public void setUp() throws Exception {
        setUp(0);
        // Mocking the xmlElementElement which is an instance of org.w3c.dom.Element
        xmlElementElement = mock(Element.class);

        // Create an instance of XSDElement
        xsdelement = new XSDElement(xmlElementElement, "unbounded");
    }

    public void setUp(int sbopt) throws Exception {
        StringBuilder sb = new StringBuilder(maxSizeForXml);
        addNamespace(sb);
        addBusiness(sb);
        addCustomers(sb);
        if (sbopt == 0) {
            addCustomer(sb);
        } else {
            addCustomerNoSubscriberType(sb);
            addCustomerSubscriberType(sb);
        }
        addServiceSubscriptions(sb);
        addServiceSubscription(sb);
        addEndOfXML(sb);
        testXML = sb.toString();
        init();
    }

    public void setUpRelationship() throws Exception {
        StringBuilder sb = new StringBuilder(maxSizeForXml);
        addNamespaceNoInventory(sb);
        addRelationship(sb);
        addRelationshipList(sb);
        addRelatedToProperty(sb);
        addRelationshipData(sb);
        addEndOfXML(sb);
        testXML = sb.toString();
        init();
    }

    private void addNamespace(StringBuilder sb) {
        sb.append(
            "<xml-bindings xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence/oxm\" package-name=\"inventory.aai.onap.org.v11\" xml-mapping-metadata-complete=\"true\">\n");
        sb.append("<xml-schema element-form-default=\"QUALIFIED\">\n");
        sb.append("<xml-ns namespace-uri=\"http://org.onap.aai.inventory/v11\" />\n");
        sb.append("</xml-schema>\n");
        sb.append("<java-types>\n");
        sb.append("<java-type name=\"Inventory\">\n");
        sb.append("<xml-root-element name=\"inventory\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element java-attribute=\"business\" name=\"business\" type=\"inventory.aai.onap.org.v11.Business\" />\n");
        sb.append("</java-attributes>\n");
        sb.append("</java-type>\n");
    }

    private void addNamespaceNoInventory(StringBuilder sb) {
        sb.append(
            "<xml-bindings xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence/oxm\" package-name=\"inventory.aai.onap.org.v11\" xml-mapping-metadata-complete=\"true\">\n");
        sb.append("<xml-schema element-form-default=\"QUALIFIED\">\n");
        sb.append("<xml-ns namespace-uri=\"http://org.onap.aai.inventory/v11\" />\n");
        sb.append("</xml-schema>\n");
        sb.append("<java-types>\n");
    }

    private void addBusiness(StringBuilder sb) {
        sb.append("<java-type name=\"Business\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Namespace for business related constructs\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("<xml-root-element name=\"business\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element java-attribute=\"customers\" name=\"customers\" type=\"inventory.aai.onap.org.v11.Customers\" />\n");
        sb.append("</java-attributes>\n");
        sb.append("</java-type>\n");
    }

    private void addCustomers(StringBuilder sb) {
        sb.append("<java-type name=\"Customers\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Collection of customer identifiers to provide linkage back to BSS information.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("<xml-root-element name=\"customers\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element container-type=\"java.util.ArrayList\" java-attribute=\"customer\" name=\"customer\" type=\"inventory.aai.onap.org.v11.Customer\" />\n");
        sb.append("</java-attributes>\n");
        sb.append("<xml-properties>\n");
        sb.append("<xml-property name=\"maximumDepth\" value=\"0\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</java-type>\n");
    }

    private void addCustomer(StringBuilder sb) {
        sb.append("<java-type name=\"Customer\">\n");
        sb.append("<xml-root-element name=\"customer\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element java-attribute=\"globalCustomerId\" name=\"global-customer-id\" required=\"true\" type=\"java.lang.String\" xml-key=\"true\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Global customer id used across to uniquely identify customer.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"subscriberName\" name=\"subscriber-name\" required=\"true\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Subscriber name, an alternate way to retrieve a customer.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"subscriberType\" name=\"subscriber-type\" required=\"true\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Subscriber type, a way to provide VID with only the INFRA customers.\" />\n");
        sb.append("<xml-property name=\"defaultValue\" value=\"CUST\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"resourceVersion\" name=\"resource-version\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Used for optimistic concurrency.  Must be empty on create, valid on update and delete.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"serviceSubscriptions\" name=\"service-subscriptions\" type=\"inventory.aai.onap.org.v11.ServiceSubscriptions\" />\n");
        // sb.append("<xml-element java-attribute=\"relationshipList\" name=\"relationship-list\"
        // type=\"inventory.aai.onap.org.v11.RelationshipList\" />\n");
        sb.append("</java-attributes>\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"customer identifiers to provide linkage back to BSS information.\" />\n");
        sb.append("<xml-property name=\"nameProps\" value=\"subscriber-name\" />\n");
        sb.append(
            "<xml-property name=\"indexedProps\" value=\"subscriber-name,global-customer-id,subscriber-type\" />\n");
        sb.append(
            "<xml-property name=\"searchable\" value=\"global-customer-id,subscriber-name\" />\n");
        sb.append("<xml-property name=\"uniqueProps\" value=\"global-customer-id\" />\n");
        sb.append("<xml-property name=\"container\" value=\"customers\" />\n");
        sb.append("<xml-property name=\"namespace\" value=\"business\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</java-type>\n");
    }

    private void addCustomerNoSubscriberType(StringBuilder sb) {
        sb.append("<java-type name=\"Customer\">\n");
        sb.append("<xml-root-element name=\"customer\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element java-attribute=\"globalCustomerId\" name=\"global-customer-id\" required=\"true\" type=\"java.lang.String\" xml-key=\"true\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Global customer id used across to uniquely identify customer.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"subscriberName\" name=\"subscriber-name\" required=\"true\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Subscriber name, an alternate way to retrieve a customer.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"resourceVersion\" name=\"resource-version\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Used for optimistic concurrency.  Must be empty on create, valid on update and delete.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"serviceSubscriptions\" name=\"service-subscriptions\" type=\"inventory.aai.onap.org.v11.ServiceSubscriptions\" />\n");
        sb.append("</java-attributes>\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"customer identifiers to provide linkage back to BSS information.\" />\n");
        sb.append("<xml-property name=\"nameProps\" value=\"subscriber-name\" />\n");
        sb.append(
            "<xml-property name=\"indexedProps\" value=\"subscriber-name,global-customer-id\" />\n");
        sb.append(
            "<xml-property name=\"searchable\" value=\"global-customer-id,subscriber-name\" />\n");
        sb.append("<xml-property name=\"uniqueProps\" value=\"global-customer-id\" />\n");
        sb.append("<xml-property name=\"container\" value=\"customers\" />\n");
        sb.append("<xml-property name=\"namespace\" value=\"business\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</java-type>\n");
    }

    private void addCustomerSubscriberType(StringBuilder sb) {
        sb.append("<java-type name=\"Customer\">\n");
        sb.append("<xml-root-element name=\"customer\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element java-attribute=\"subscriberType\" name=\"subscriber-type\" required=\"true\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Subscriber type, a way to provide VID with only the INFRA customers.\" />\n");
        sb.append("<xml-property name=\"defaultValue\" value=\"CUST\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append("</java-attributes>\n");
        sb.append("<xml-properties>\n");
        sb.append("<xml-property name=\"indexedProps\" value=\"subscriber-type\" />\n");
        sb.append("<xml-property name=\"container\" value=\"customers\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</java-type>\n");
    }

    private void addServiceSubscriptions(StringBuilder sb) {
        sb.append("<java-type name=\"ServiceSubscriptions\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Collection of objects that group service instances.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("<xml-root-element name=\"service-subscriptions\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element container-type=\"java.util.ArrayList\" java-attribute=\"serviceSubscription\" name=\"service-subscription\" type=\"inventory.aai.onap.org.v11.ServiceSubscription\" />\n");
        sb.append("</java-attributes>\n");
        sb.append("</java-type>\n");
    }

    private void addServiceSubscription(StringBuilder sb) {
        sb.append("<java-type name=\"ServiceSubscription\">\n");
        sb.append("<xml-root-element name=\"service-subscription\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element java-attribute=\"serviceType\" name=\"service-type\" required=\"true\" type=\"java.lang.String\" xml-key=\"true\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Value defined by orchestration to identify this service.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"tempUbSubAccountId\" name=\"temp-ub-sub-account-id\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"This property will be deleted from A&amp;AI in the near future. Only stop gap solution.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"resourceVersion\" name=\"resource-version\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Used for optimistic concurrency.  Must be empty on create, valid on update and delete.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        // sb.append("<xml-element java-attribute=\"relationshipList\" name=\"relationship-list\"
        // type=\"inventory.aai.onap.org.v11.RelationshipList\" />\n");
        sb.append("</java-attributes>\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Object that group service instances.\" />\n");
        sb.append("<xml-property name=\"indexedProps\" value=\"service-type\" />\n");
        sb.append("<xml-property name=\"dependentOn\" value=\"customer\" />\n");
        sb.append("<xml-property name=\"container\" value=\"service-subscriptions\" />\n");
        sb.append(
            "<xml-property name=\"crossEntityReference\" value=\"service-instance,service-type\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</java-type>\n");
    }

    private void addRelationshipList(StringBuilder sb) {
        sb.append("<java-type name=\"RelationshipList\">\n");
        sb.append("<xml-root-element name=\"relationship-list\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element container-type=\"java.util.ArrayList\" java-attribute=\"relationship\" name=\"relationship\" type=\"inventory.aai.onap.org.v11.Relationship\" />/n");
        sb.append("</java-attributes>\n");
        sb.append("</java-type>\n");
    }

    private void addRelationship(StringBuilder sb) {
        sb.append("<java-type name=\"Relationship\">\n");
        sb.append("<xml-root-element name=\"relationship\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element java-attribute=\"relatedTo\" name=\"related-to\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"A keyword provided by A&amp;AI to indicate type of node.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"relatedLink\" name=\"related-link\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append("<xml-property name=\"description\" value=\"URL to the object in A&amp;AI.\" />");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element container-type=\"java.util.ArrayList\" java-attribute=\"relationshipData\" name=\"relationship-data\" type=\"inventory.aai.onap.org.v11.RelationshipData\" />\n");
        sb.append(
            "<xml-element container-type=\"java.util.ArrayList\" java-attribute=\"relatedToProperty\" name=\"related-to-property\" type=\"inventory.aai.onap.org.v11.RelatedToProperty\" />\n");
        sb.append("</java-attributes>\n");
        sb.append("</java-type>\n");
    }

    private void addRelatedToProperty(StringBuilder sb) {
        sb.append("<java-type name=\"RelatedToProperty\">\n");
        sb.append("<xml-root-element name=\"related-to-property\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element java-attribute=\"propertyKey\" name=\"property-key\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append("<xml-property name=\"description\" value=\"Key part of a key/value pair\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"propertyValue\" name=\"property-value\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Value part of a key/value pair\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append("</java-attributes>\n");
        sb.append("</java-type>\n");
    }

    private void addRelationshipData(StringBuilder sb) {
        sb.append("<java-type name=\"RelationshipData\">\n");
        sb.append("<xml-root-element name=\"relationship-data\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element java-attribute=\"relationshipKey\" name=\"relationship-key\" required=\"true\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"A keyword provided by A&amp;AI to indicate an attribute.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"relationshipValue\" name=\"relationship-value\" required=\"true\" type=\"java.lang.String\">\n");
        sb.append("<xml-properties>\n");
        sb.append("<xml-property name=\"description\" value=\"Value of the attribute.\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append("</java-attributes>\n");
        sb.append("</java-type>\n");
    }

    private void addEndOfXML(StringBuilder sb) {
        sb.append("</java-types>\n");
        sb.append("</xml-bindings>\n");
    }

    public void init() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder dBuilder = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw e;
        }
        try {
            InputSource isInput = new InputSource(new StringReader(testXML));
            doc = dBuilder.parse(isInput);
        } catch (SAXException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }

        NodeList bindingsNodes = doc.getElementsByTagName("xml-bindings");
        Element bindingElement;
        NodeList javaTypesNodes;
        Element javaTypesElement;

        if (bindingsNodes == null || bindingsNodes.getLength() == 0) {
            throw new SAXException("OXM file error: missing <binding-nodes> in XML");
        }

        bindingElement = (Element) bindingsNodes.item(0);
        javaTypesNodes = bindingElement.getElementsByTagName("java-types");
        if (javaTypesNodes.getLength() < 1) {
            throw new SAXException("OXM file error: missing <binding-nodes><java-types> in XML");
        }
        javaTypesElement = (Element) javaTypesNodes.item(0);

        javaTypeNodes = javaTypesElement.getElementsByTagName("java-type");
        if (javaTypeNodes.getLength() < 1) {
            throw new SAXException(
                "OXM file error: missing <binding-nodes><java-types><java-type> in XML");
        }
        logger.debug(testXML);
    }

    @Test
    public void testXSDElement() {
        // repeat of testGetIndexedProps() which uses the constructor
        ArrayList<String> target = new ArrayList<String>();
        target.add("subscriber-name");
        target.add("global-customer-id");
        target.add("subscriber-type");
        target.add("service-type");

        Vector<String> indexedProps = new Vector<String>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            indexedProps.addAll(javaTypeElement.getIndexedProps());
        }
        assertThat(new ArrayList<>(indexedProps),
            both(everyItem(is(in(target.toArray())))).and(containsInAnyOrder(target.toArray())));
    }

    @Test
    public void testName() {
        ArrayList<String> target = new ArrayList<String>();
        target.add("ServiceSubscriptions");
        target.add("ServiceSubscription");
        target.add("Inventory");
        target.add("Business");
        target.add("Customers");
        target.add("Customer");
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            names.add(javaTypeElement.name());
        }
        logger.debug(String.join("|", names));
        assertThat(names,
            both(everyItem(is(in(target.toArray())))).and(containsInAnyOrder(target.toArray())));
    }

    @Test
    public void testGetAddTypes() {
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> target = new HashMap<String, ArrayList<String>>();
        target.put("Customer",
            new ArrayList<>(Arrays.asList("ServiceSubscriptions", "RelationshipList")));
        target.put("Customer", new ArrayList<>(Arrays.asList("ServiceSubscriptions")));
        target.put("Business", new ArrayList<>(Arrays.asList("Customers")));
        target.put("Inventory", new ArrayList<>(Arrays.asList("Business")));
        target.put("Customers", new ArrayList<>(Arrays.asList("Customer")));
        target.put("ServiceSubscription", new ArrayList<>(Arrays.asList("RelationshipList")));
        target.put("ServiceSubscription", new ArrayList<>(Arrays.asList()));
        target.put("ServiceSubscriptions", new ArrayList<>(Arrays.asList("ServiceSubscription")));

        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            ArrayList<String> addTypes = new ArrayList<String>();
            NodeList xmlElementNodes = javaTypeElement.getElementsByTagName("xml-element");
            String name = javaTypeElement.name();
            for (int j = 0; j < xmlElementNodes.getLength(); ++j) {
                XSDElement xmlElement = new XSDElement((Element) xmlElementNodes.item(j));
                addTypes.addAll(xmlElement.getAddTypes("v11"));
                map.put(name, addTypes);
            }
        }
        for (String key : map.keySet()) {
            assertThat("Expected for key:" + key, map.get(key), equalTo(target.get(key)));
        }
    }

    /*
     * @Test
     * public void testGetRequiredElements() {
     * HashMap<String,ArrayList<String>> map = new HashMap<String,ArrayList<String>>();
     * ArrayList<String> target = new ArrayList<String>();
     * target.add("global-customer-id\n");
     * target.add("subscriber-name\n");
     * target.add("subscriber-type");
     * for ( int i = 0; i < javaTypeNodes.getLength(); ++i ) {
     * XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
     * ArrayList<String> requiredItems = new ArrayList<String>();
     * String name=javaTypeElement.name();
     * requiredItems.addAll(javaTypeElement.getRequiredElements("v11"));
     * map.put(name,requiredItems);
     * }
     * for(String key : map.keySet()) {
     * assertThat(map.get(key),equalTo(target));
     * }
     * }
     */
    @Test
    public void testGetPathDescriptionProperty() {
        ArrayList<String> target = new ArrayList<String>();
        target.add("Namespace for business related constructs");
        target
            .add("Collection of customer identifiers to provide linkage back to BSS information.");
        target.add("customer identifiers to provide linkage back to BSS information.");
        target.add("Collection of objects that group service instances.");
        target.add("Object that group service instances.");
        List<String> descs = new ArrayList<String>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            if (javaTypeElement.getPathDescriptionProperty() != null)
                descs.add(javaTypeElement.getPathDescriptionProperty());
        }
        logger.debug(String.join("|", descs));
        assertThat(new ArrayList<>(descs),
            both(everyItem(is(in(target.toArray())))).and(containsInAnyOrder(target.toArray())));
    }

    @Test
    public void testGetIndexedProps() {
        ArrayList<String> target = new ArrayList<String>();
        target.add("subscriber-name");
        target.add("global-customer-id");
        target.add("subscriber-type");
        target.add("service-type");

        Vector<String> indexedProps = new Vector<String>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            indexedProps.addAll(javaTypeElement.getIndexedProps());
        }
        assertThat(new ArrayList<>(indexedProps),
            both(everyItem(is(in(target.toArray())))).and(containsInAnyOrder(target.toArray())));
    }

    @Test
    public void testGetContainerProperty() {
        ArrayList<String> target = new ArrayList<String>();
        target.add("service-subscriptions");
        target.add("customers");
        List<String> containers = new ArrayList<String>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            if (javaTypeElement.getContainerProperty() != null)
                containers.add(javaTypeElement.getContainerProperty());
        }
        logger.debug(String.join("|", containers));
        assertThat(new ArrayList<>(containers),
            both(everyItem(is(in(target.toArray())))).and(containsInAnyOrder(target.toArray())));
    }

    @Test
    public void testGetQueryParamYAML() {
        ArrayList<String> target = new ArrayList<String>();
        target.add(
            "        - name: global-customer-id\n          in: query\n          required: false\n          type: string\n");
        target.add(
            "        - name: subscriber-name\n          in: query\n          required: false\n          type: string\n");
        target.add(
            "        - name: subscriber-type\n          in: query\n          required: false\n          type: string\n");
        Vector<String> indexedProps = new Vector<String>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            if (javaTypeElement.getContainerProperty() != null) {
                indexedProps.addAll(javaTypeElement.getIndexedProps());
                String container = javaTypeElement.getContainerProperty();
                Vector<String> containerProps = new Vector<String>();
                NodeList xmlElementNodes = javaTypeElement.getElementsByTagName("xml-element");
                for (int j = 0; j < xmlElementNodes.getLength(); ++j) {
                    XSDElement xmlElement = new XSDElement((Element) xmlElementNodes.item(j));
                    if (indexedProps.contains(xmlElement.name()))
                        containerProps.add(xmlElement.getQueryParamYAML());
                }
                GetOperation.addContainerProps(container, containerProps);
            }
        }
        /*
         * List<String> queryParams = new ArrayList<String>();
         * for ( int i = 0; i < javaTypeNodes.getLength(); ++ i ) {
         * XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
         * if(javaTypeElement.getQueryParamYAML() != null)
         * queryParams.add(javaTypeElement.getQueryParamYAML());
         * }
         */
        assertThat(GetOperation.containers.get("customers"), equalTo(target));
    }

    @Test
    public void testGetPathParamYAML() {
        ArrayList<String> target = new ArrayList<String>();
        target.add(
            "        - name: Inventory\n          in: path\n          description: Inventory\n          required: true\n");
        target.add(
            "        - name: Business\n          in: path\n          description: Business\n          required: true\n");
        target.add(
            "        - name: Customers\n          in: path\n          description: Customers\n          required: true\n");
        target.add(
            "        - name: Customer\n          in: path\n          description: Customer\n          required: true\n");
        target.add(
            "        - name: ServiceSubscriptions\n          in: path\n          description: ServiceSubscriptions\n          required: true\n");
        target.add(
            "        - name: ServiceSubscription\n          in: path\n          description: ServiceSubscription\n          required: true\n");
        List<String> pathParams = new ArrayList<String>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            if (javaTypeElement.getPathParamYAML(javaTypeElement.name()) != null)
                pathParams.add(javaTypeElement.getPathParamYAML(javaTypeElement.name()));
        }
        logger.debug(String.join("|", pathParams));
        assertThat(new ArrayList<>(pathParams),
            both(everyItem(is(in(target.toArray())))).and(containsInAnyOrder(target.toArray())));
    }

    @Test
    public void testGetHTMLAnnotation() {
        ArrayList<String> target = new ArrayList<String>();
        target.add("  <xs:annotation>" + OxmFileProcessor.LINE_SEPARATOR + "    <xs:appinfo>"
            + OxmFileProcessor.LINE_SEPARATOR
            + "      <annox:annotate target=\"Business\">@org.onap.aai.annotations.Metadata(description=\"Namespace for business related constructs\")</annox:annotate>"
            + OxmFileProcessor.LINE_SEPARATOR + "    </xs:appinfo>"
            + OxmFileProcessor.LINE_SEPARATOR + "  </xs:annotation>"
            + OxmFileProcessor.LINE_SEPARATOR);
        target.add("  <xs:annotation>" + OxmFileProcessor.LINE_SEPARATOR + "    <xs:appinfo>"
            + OxmFileProcessor.LINE_SEPARATOR
            + "      <annox:annotate target=\"Customers\">@org.onap.aai.annotations.Metadata(description=\"Collection of customer identifiers to provide linkage back to BSS information.\")</annox:annotate>"
            + OxmFileProcessor.LINE_SEPARATOR + "    </xs:appinfo>"
            + OxmFileProcessor.LINE_SEPARATOR + "  </xs:annotation>"
            + OxmFileProcessor.LINE_SEPARATOR);
        target.add("  <xs:annotation>" + OxmFileProcessor.LINE_SEPARATOR + "    <xs:appinfo>"
            + OxmFileProcessor.LINE_SEPARATOR
            + "      <annox:annotate target=\"Customer\">@org.onap.aai.annotations.Metadata(description=\"customer identifiers to provide linkage back to BSS information.\",nameProps=\"subscriber-name\",indexedProps=\"subscriber-name,global-customer-id,subscriber-type\",searchable=\"global-customer-id,subscriber-name\",uniqueProps=\"global-customer-id\",container=\"customers\",namespace=\"business\")</annox:annotate>"
            + OxmFileProcessor.LINE_SEPARATOR + "    </xs:appinfo>"
            + OxmFileProcessor.LINE_SEPARATOR + "  </xs:annotation>"
            + OxmFileProcessor.LINE_SEPARATOR);
        target.add("  <xs:annotation>" + OxmFileProcessor.LINE_SEPARATOR + "    <xs:appinfo>"
            + OxmFileProcessor.LINE_SEPARATOR
            + "      <annox:annotate target=\"ServiceSubscriptions\">@org.onap.aai.annotations.Metadata(description=\"Collection of objects that group service instances.\")</annox:annotate>"
            + OxmFileProcessor.LINE_SEPARATOR + "    </xs:appinfo>"
            + OxmFileProcessor.LINE_SEPARATOR + "  </xs:annotation>"
            + OxmFileProcessor.LINE_SEPARATOR);
        target.add("  <xs:annotation>" + OxmFileProcessor.LINE_SEPARATOR + "    <xs:appinfo>"
            + OxmFileProcessor.LINE_SEPARATOR
            + "      <annox:annotate target=\"ServiceSubscription\">@org.onap.aai.annotations.Metadata(description=\"Object that group service instances.\",indexedProps=\"service-type\",dependentOn=\"customer\",container=\"service-subscriptions\",crossEntityReference=\"service-instance,service-type\")</annox:annotate>"
            + OxmFileProcessor.LINE_SEPARATOR + "    </xs:appinfo>"
            + OxmFileProcessor.LINE_SEPARATOR + "  </xs:annotation>"
            + OxmFileProcessor.LINE_SEPARATOR);
        List<String> annotes = new ArrayList<String>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            if (StringUtils
                .isNotEmpty(javaTypeElement.getHTMLAnnotation(javaTypeElement.name(), "")))
                annotes.add(javaTypeElement.getHTMLAnnotation(javaTypeElement.name(), "  "));
        }
        logger.debug("result:");
        logger.debug(String.join("|", annotes));
        logger.debug("Expected:");
        logger.debug(String.join("|", target));
        assertThat(new ArrayList<>(annotes),
            both(everyItem(is(in(target.toArray())))).and(containsInAnyOrder(target.toArray())));

    }

    @Test
    public void testGetTypePropertyYAML() {
        ArrayList<String> target = new ArrayList<String>();
        target.add("      Inventory:\n        type: ");
        target.add(
            "      Business:\n        type:         description: Namespace for business related constructs\n");
        target.add(
            "      Customers:\n        type:         description: Collection of customer identifiers to provide linkage back to BSS information.\n");
        target.add(
            "      Customer:\n        type:         description: customer identifiers to provide linkage back to BSS information.\n");
        target.add(
            "      ServiceSubscriptions:\n        type:         description: Collection of objects that group service instances.\n");
        target.add(
            "      ServiceSubscription:\n        type:         description: Object that group service instances.\n");
        StringBuilder sb = new StringBuilder(
            "      Customer:\n        type:         description: |\n          customer identifiers to provide linkage back to BSS information.\n");
        sb.append(
            "          *This property can be used as a filter to find the start node for a dsl query\n");
        String yamlDesc = sb.toString();
        List<String> types = new ArrayList<String>();
        String container;
        String customerDesc = null;
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            if (javaTypeElement.getTypePropertyYAML(false) != null)
                types.add(javaTypeElement.getTypePropertyYAML(false));
            container = javaTypeElement.getContainerProperty();
            if ("customers".equals(container)) {
                customerDesc = javaTypeElement.getTypePropertyYAML(true);
            }
        }
        assertThat(new ArrayList<>(types),
            both(everyItem(is(in(target.toArray())))).and(containsInAnyOrder(target.toArray())));
        assertEquals(customerDesc, yamlDesc);
    }

    @Test
    public void testIsStandardType() {
        HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        HashMap<String, ArrayList<String>> target = new HashMap<String, ArrayList<String>>();
        target.put("Customer", new ArrayList<>(Arrays.asList("global-customer-id",
            "subscriber-name", "subscriber-type", "resource-version")));
        target.put("Business", new ArrayList<>());
        target.put("Inventory", new ArrayList<>());
        target.put("Customers", new ArrayList<>());
        target.put("ServiceSubscriptions", new ArrayList<>());
        target.put("ServiceSubscription", new ArrayList<>(
            Arrays.asList("service-type", "temp-ub-sub-account-id", "resource-version")));

        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            ArrayList<String> addTypes = new ArrayList<String>();
            NodeList xmlElementNodes = javaTypeElement.getElementsByTagName("xml-element");
            String name = javaTypeElement.name();
            for (int j = 0; j < xmlElementNodes.getLength(); ++j) {
                XSDElement xmlElement = new XSDElement((Element) xmlElementNodes.item(j));
                if (xmlElement.isStandardType())
                    addTypes.add(xmlElement.name());
            }
            map.put(name, addTypes);
        }
        for (String key : map.keySet()) {
            assertThat(map.get(key), equalTo(target.get(key)));
        }
    }

    @Test
    public void testGetHTMLElement_withoutAnnotation() {
        // Create the mock SchemaVersion and HTMLfromOXM
        SchemaVersion schemaVersion = new SchemaVersion("v11");  // Use version "v11"
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);

        // Mock getXmlRootElementName() to return a custom value
        when(htmlDriver.getXmlRootElementName(anyString())).thenReturn("stringElement");

        // Create a dummy Element (XML Element)
        Document document = createTestXMLDocument();  // Assuming this method creates a Document object
        Element element = document.createElement("testElement");
        element.setAttribute("name", "testName");
        element.setAttribute("type", "java.lang.String");
        element.setAttribute("required", "false");
        element.setAttribute("container-type", "java.util.ArrayList");

        // Initialize XSDElement with the element and maxOccurs value
        XSDElement xsdelement = new XSDElement(element, "unbounded");

        // Mock the behavior of XSDElement's getHTMLAnnotation() to return an empty string (no annotation)
        XSDElement mockedXSDElement = mock(XSDElement.class);
        when(mockedXSDElement.getHTMLAnnotation("field", "          ")).thenReturn("");

        // Call the method to get the actual HTML element without annotations
        String actualHtml = xsdelement.getHTMLElement(schemaVersion, true, htmlDriver);

        // Assert the generated HTML contains the expected elements and doesn't include annotation text
        assertThat(actualHtml, containsString("<xs:element name=\"testName\""));
        assertThat(actualHtml, containsString("type=\"xs:string\""));
        assertThat(actualHtml, containsString("minOccurs=\"0\""));
        assertThat(actualHtml, containsString("maxOccurs=\"unbounded\""));
        assertThat(actualHtml, not(containsString("Some annotation text")));
        assertThat(actualHtml, containsString("/>"));
    }

    // Helper method to create a simple Document with a root element (can be extended as per the test needs)
    private Document createTestXMLDocument() {
        try {
            // Use a simple DocumentBuilderFactory to create an empty Document
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            return document;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void testGetOwnerDocument() {
        Document ownerDocument = mock(Document.class);
        when(xmlElementElement.getOwnerDocument()).thenReturn(ownerDocument);

        Document result = xsdelement.getOwnerDocument();

        assertEquals(ownerDocument, result);
    }

    @Test
    public void testInsertBefore() throws DOMException {
        Node newChild = mock(Node.class);
        Node refChild = mock(Node.class);
        when(xmlElementElement.insertBefore(newChild, refChild)).thenReturn(newChild);

        Node result = xsdelement.insertBefore(newChild, refChild);

        assertEquals(newChild, result);
    }

    @Test
    public void testReplaceChild() throws DOMException {
        Node newChild = mock(Node.class);
        Node oldChild = mock(Node.class);
        when(xmlElementElement.replaceChild(newChild, oldChild)).thenReturn(newChild);

        Node result = xsdelement.replaceChild(newChild, oldChild);

        assertEquals(newChild, result);
    }

    @Test
    public void testRemoveChild() throws DOMException {
        Node oldChild = mock(Node.class);
        when(xmlElementElement.removeChild(oldChild)).thenReturn(oldChild);

        Node result = xsdelement.removeChild(oldChild);

        assertEquals(oldChild, result);
    }

    @Test
    public void testAppendChild() throws DOMException {
        Node newChild = mock(Node.class);
        when(xmlElementElement.appendChild(newChild)).thenReturn(newChild);

        Node result = xsdelement.appendChild(newChild);

        assertEquals(newChild, result);
    }

    @Test
    public void testHasChildNodes() {
        when(xmlElementElement.hasChildNodes()).thenReturn(true);

        boolean result = xsdelement.hasChildNodes();

        assertTrue(result);
    }

    @Test
    public void testCloneNode() {
        Node clone = mock(Node.class);
        when(xmlElementElement.cloneNode(true)).thenReturn(clone);

        Node result = xsdelement.cloneNode(true);

        assertEquals(clone, result);
    }

    @Test
    public void testNormalize() {
        doNothing().when(xmlElementElement).normalize();

        xsdelement.normalize();

        verify(xmlElementElement, times(1)).normalize();
    }

    @Test
    public void testIsSupported() {
        when(xmlElementElement.isSupported("XML", "1.0")).thenReturn(true);

        boolean result = xsdelement.isSupported("XML", "1.0");

        assertTrue(result);
    }

    @Test
    public void testGetNamespaceURI() {
        when(xmlElementElement.getNamespaceURI()).thenReturn("http://example.com");

        String result = xsdelement.getNamespaceURI();

        assertEquals("http://example.com", result);
    }

    @Test
    public void testGetPrefix() {
        when(xmlElementElement.getPrefix()).thenReturn("ex");

        String result = xsdelement.getPrefix();

        assertEquals("ex", result);
    }

    @Test
    public void testSetPrefix() throws DOMException {
        doNothing().when(xmlElementElement).setPrefix("ex");

        xsdelement.setPrefix("ex");

        verify(xmlElementElement, times(1)).setPrefix("ex");
    }

    @Test
    public void testGetLocalName() {
        when(xmlElementElement.getLocalName()).thenReturn("localName");

        String result = xsdelement.getLocalName();

        assertEquals("localName", result);
    }

    @Test
    public void testHasAttributes() {
        when(xmlElementElement.hasAttributes()).thenReturn(true);

        boolean result = xsdelement.hasAttributes();

        assertTrue(result);
    }

    @Test
    public void testGetBaseURI() {
        when(xmlElementElement.getBaseURI()).thenReturn("http://baseuri.com");

        String result = xsdelement.getBaseURI();

        assertEquals("http://baseuri.com", result);
    }

    @Test
    public void testCompareDocumentPosition() throws DOMException {
        Node otherNode = mock(Node.class);
        when(xmlElementElement.compareDocumentPosition(otherNode)).thenReturn(Node.DOCUMENT_POSITION_FOLLOWING);

        short result = xsdelement.compareDocumentPosition(otherNode);

        assertEquals(Node.DOCUMENT_POSITION_FOLLOWING, result);
    }

    @Test
    public void testGetTextContent() throws DOMException {
        when(xmlElementElement.getTextContent()).thenReturn("some text content");

        String result = xsdelement.getTextContent();

        assertEquals("some text content", result);
    }

    @Test
    public void testSetTextContent() throws DOMException {
        doNothing().when(xmlElementElement).setTextContent("new content");

        xsdelement.setTextContent("new content");

        verify(xmlElementElement, times(1)).setTextContent("new content");
    }

    @Test
    public void testIsSameNode() {
        Node otherNode = mock(Node.class);
        when(xmlElementElement.isSameNode(otherNode)).thenReturn(true);

        boolean result = xsdelement.isSameNode(otherNode);

        assertTrue(result);
    }

    @Test
    public void testLookupPrefix() {
        when(xmlElementElement.lookupPrefix("http://example.com")).thenReturn("ex");

        String result = xsdelement.lookupPrefix("http://example.com");

        assertEquals("ex", result);
    }

    @Test
    public void testIsDefaultNamespace() {
        when(xmlElementElement.isDefaultNamespace("http://example.com")).thenReturn(true);

        boolean result = xsdelement.isDefaultNamespace("http://example.com");

        assertTrue(result);
    }

    @Test
    public void testLookupNamespaceURI() {
        when(xmlElementElement.lookupNamespaceURI("ex")).thenReturn("http://example.com");

        String result = xsdelement.lookupNamespaceURI("ex");

        assertEquals("http://example.com", result);
    }

    @Test
    public void testIsEqualNode() {
        Node otherNode = mock(Node.class);
        when(xmlElementElement.isEqualNode(otherNode)).thenReturn(true);

        boolean result = xsdelement.isEqualNode(otherNode);

        assertTrue(result);
    }

    @Test
    public void testGetFeature() {
        when(xmlElementElement.getFeature("XML", "1.0")).thenReturn("feature");

        Object result = xsdelement.getFeature("XML", "1.0");

        assertEquals("feature", result);
    }

    @Test
    public void testSetUserData() {
        UserDataHandler handler = mock(UserDataHandler.class);
        when(xmlElementElement.setUserData("key", "data", handler)).thenReturn("data");

        Object result = xsdelement.setUserData("key", "data", handler);

        assertEquals("data", result);
    }

    @Test
    public void testGetUserData() {
        when(xmlElementElement.getUserData("key")).thenReturn("userData");

        Object result = xsdelement.getUserData("key");

        assertEquals("userData", result);
    }

    @Test
    public void testGetTagName() {
        when(xmlElementElement.getTagName()).thenReturn("tagName");

        String result = xsdelement.getTagName();

        assertEquals("tagName", result);
    }

    @Test
    public void testGetAttribute() {
        when(xmlElementElement.getAttribute("name")).thenReturn("value");

        String result = xsdelement.getAttribute("name");

        assertEquals("value", result);
    }

    @Test
    public void testSetAttribute() throws DOMException {
        doNothing().when(xmlElementElement).setAttribute("name", "value");

        xsdelement.setAttribute("name", "value");

        verify(xmlElementElement, times(1)).setAttribute("name", "value");
    }

    @Test
    public void testGetNodeName() {
        // Arrange
        String expectedNodeName = "testElement";
        when(xmlElementElement.getNodeName()).thenReturn(expectedNodeName);

        // Act
        String nodeName = xsdelement.getNodeName();

        // Assert
        assertEquals(expectedNodeName, nodeName);
    }

    @Test
    public void testGetNodeValue() throws DOMException {
        // Arrange
        String expectedNodeValue = "testValue";
        when(xmlElementElement.getNodeValue()).thenReturn(expectedNodeValue);

        // Act
        String nodeValue = xsdelement.getNodeValue();

        // Assert
        assertEquals(expectedNodeValue, nodeValue);
    }

    @Test
    public void testSetNodeValue() throws DOMException {
        // Arrange
        String newValue = "newValue";
        doNothing().when(xmlElementElement).setNodeValue(newValue);

        // Act
        xsdelement.setNodeValue(newValue);

        // Assert
        verify(xmlElementElement, times(1)).setNodeValue(newValue);
    }

    @Test
    public void testGetNodeType() {
        // Arrange
        short expectedNodeType = Node.ELEMENT_NODE;
        when(xmlElementElement.getNodeType()).thenReturn(expectedNodeType);

        // Act
        short nodeType = xsdelement.getNodeType();

        // Assert
        assertEquals(expectedNodeType, nodeType);
    }

    @Test
    public void testGetParentNode() {
        // Arrange
        Node parentNode = mock(Node.class);
        when(xmlElementElement.getParentNode()).thenReturn(parentNode);

        // Act
        Node parent = xsdelement.getParentNode();

        // Assert
        assertEquals(parentNode, parent);
    }

    @Test
    public void testGetChildNodes() {
        // Arrange
        NodeList nodeList = mock(NodeList.class);
        when(xmlElementElement.getChildNodes()).thenReturn(nodeList);

        // Act
        NodeList childNodes = xsdelement.getChildNodes();

        // Assert
        assertEquals(nodeList, childNodes);
    }

    @Test
    public void testGetFirstChild() {
        // Arrange
        Node firstChild = mock(Node.class);
        when(xmlElementElement.getFirstChild()).thenReturn(firstChild);

        // Act
        Node first = xsdelement.getFirstChild();

        // Assert
        assertEquals(firstChild, first);
    }

    @Test
    public void testGetLastChild() {
        // Arrange
        Node lastChild = mock(Node.class);
        when(xmlElementElement.getLastChild()).thenReturn(lastChild);

        // Act
        Node last = xsdelement.getLastChild();

        // Assert
        assertEquals(lastChild, last);
    }

    @Test
    public void testGetPreviousSibling() {
        // Arrange
        Node previousSibling = mock(Node.class);
        when(xmlElementElement.getPreviousSibling()).thenReturn(previousSibling);

        // Act
        Node previous = xsdelement.getPreviousSibling();

        // Assert
        assertEquals(previousSibling, previous);
    }

    @Test
    public void testGetNextSibling() {
        // Arrange
        Node nextSibling = mock(Node.class);
        when(xmlElementElement.getNextSibling()).thenReturn(nextSibling);

        // Act
        Node next = xsdelement.getNextSibling();

        // Assert
        assertEquals(nextSibling, next);
    }

    @Test
    public void testRemoveAttribute() throws DOMException {
        doNothing().when(xmlElementElement).removeAttribute("name");

        xsdelement.removeAttribute("name");

        verify(xmlElementElement, times(1)).removeAttribute("name");
    }

    @Test
    public void testGetAttributeNode() {
        Attr attr = mock(Attr.class);
        when(xmlElementElement.getAttributeNode("name")).thenReturn(attr);

        Attr result = xsdelement.getAttributeNode("name");

        assertEquals(attr, result);
    }

    @Test
    public void testSetAttributeNode() throws DOMException {
        Attr newAttr = mock(Attr.class);
        when(xmlElementElement.setAttributeNode(newAttr)).thenReturn(newAttr);

        Attr result = xsdelement.setAttributeNode(newAttr);

        assertEquals(newAttr, result);
    }

    @Test
    public void testRemoveAttributeNode() throws DOMException {
        Attr oldAttr = mock(Attr.class);
        when(xmlElementElement.removeAttributeNode(oldAttr)).thenReturn(oldAttr);

        Attr result = xsdelement.removeAttributeNode(oldAttr);

        assertEquals(oldAttr, result);
    }

    @Test
    public void testGetElementsByTagName() {
        NodeList nodeList = mock(NodeList.class);
        when(xmlElementElement.getElementsByTagName("name")).thenReturn(nodeList);

        NodeList result = xsdelement.getElementsByTagName("name");

        assertEquals(nodeList, result);
    }

    @Test
    public void testGetAttributeNS() throws DOMException {
        when(xmlElementElement.getAttributeNS("namespaceURI", "localName")).thenReturn("value");

        String result = xsdelement.getAttributeNS("namespaceURI", "localName");

        assertEquals("value", result);
    }

    @Test
    public void testSetAttributeNS() throws DOMException {
        doNothing().when(xmlElementElement).setAttributeNS("namespaceURI", "qualifiedName", "value");

        xsdelement.setAttributeNS("namespaceURI", "qualifiedName", "value");

        verify(xmlElementElement, times(1)).setAttributeNS("namespaceURI", "qualifiedName", "value");
    }

    @Test
    public void testRemoveAttributeNS() throws DOMException {
        doNothing().when(xmlElementElement).removeAttributeNS("namespaceURI", "localName");

        xsdelement.removeAttributeNS("namespaceURI", "localName");

        verify(xmlElementElement, times(1)).removeAttributeNS("namespaceURI", "localName");
    }

    @Test
    public void testGetAttributeNodeNS() throws DOMException {
        Attr attr = mock(Attr.class);
        when(xmlElementElement.getAttributeNodeNS("namespaceURI", "localName")).thenReturn(attr);

        Attr result = xsdelement.getAttributeNodeNS("namespaceURI", "localName");

        assertEquals(attr, result);
    }

    @Test
    public void testSetAttributeNodeNS() throws DOMException {
        Attr newAttr = mock(Attr.class);
        when(xmlElementElement.setAttributeNodeNS(newAttr)).thenReturn(newAttr);

        Attr result = xsdelement.setAttributeNodeNS(newAttr);

        assertEquals(newAttr, result);
    }

    @Test
    public void testGetElementsByTagNameNS() throws DOMException {
        NodeList nodeList = mock(NodeList.class);
        when(xmlElementElement.getElementsByTagNameNS("namespaceURI", "localName")).thenReturn(nodeList);

        NodeList result = xsdelement.getElementsByTagNameNS("namespaceURI", "localName");

        assertEquals(nodeList, result);
    }

    @Test
    public void testHasAttribute() {
        when(xmlElementElement.hasAttribute("name")).thenReturn(true);

        boolean result = xsdelement.hasAttribute("name");

        assertTrue(result);
    }

    @Test
    public void testHasAttributeNS() throws DOMException {
        when(xmlElementElement.hasAttributeNS("namespaceURI", "localName")).thenReturn(true);

        boolean result = xsdelement.hasAttributeNS("namespaceURI", "localName");

        assertTrue(result);
    }

    @Test
    public void testGetSchemaTypeInfo() {
        TypeInfo typeInfo = mock(TypeInfo.class);
        when(xmlElementElement.getSchemaTypeInfo()).thenReturn(typeInfo);

        TypeInfo result = xsdelement.getSchemaTypeInfo();

        assertEquals(typeInfo, result);
    }

    @Test
    public void testSetIdAttribute() throws DOMException {
        doNothing().when(xmlElementElement).setIdAttribute("name", true);

        xsdelement.setIdAttribute("name", true);

        verify(xmlElementElement, times(1)).setIdAttribute("name", true);
    }

    @Test
    public void testSetIdAttributeNS() throws DOMException {
        doNothing().when(xmlElementElement).setIdAttributeNS("namespaceURI", "localName", true);

        xsdelement.setIdAttributeNS("namespaceURI", "localName", true);

        verify(xmlElementElement, times(1)).setIdAttributeNS("namespaceURI", "localName", true);
    }

    @Test
    public void testSetIdAttributeNode() throws DOMException {
        Attr idAttr = mock(Attr.class);
        doNothing().when(xmlElementElement).setIdAttributeNode(idAttr, true);

        xsdelement.setIdAttributeNode(idAttr, true);

        verify(xmlElementElement, times(1)).setIdAttributeNode(idAttr, true);
    }

    @Test
    public void testGetRequiresProperty() {
        // Mocking the XML element containing the <xml-property name="requires"> element
        String xmlString =
            "<xml-bindings>" +
                "<java-type name=\"Business\">" +
                "<xml-properties>" +
                "<xml-property name=\"description\" value=\"Namespace for business related constructs\" />" +
                "<xml-property name=\"requires\" value=\"some-required-property\" />" + // This is what we're looking for
                "</xml-properties>" +
                "</java-type>" +
                "</xml-bindings>";

        try {
            // Parse the XML string into a Document object
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

            // Find the <java-type> element and pass it into XSDElement
            Element javaTypeElement = (Element) doc.getElementsByTagName("java-type").item(0);
            XSDElement xsdelement = new XSDElement(javaTypeElement);

            // Call getRequiresProperty and assert the result
            String requiresProperty = xsdelement.getRequiresProperty();
            assertEquals("some-required-property", requiresProperty, "The requires property should match the expected value.");
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            fail("An error occurred while parsing the XML or executing the test.");
        }
    }

    @Test
    public void testGetQueryParamYAML_withDescription() {
        // Mock Element with description
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("global-customer-id");
        Mockito.when(element.getAttribute("description")).thenReturn("Customer ID description");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.String");

        XSDElement xsdElement = new XSDElement(element);

        String expectedYAML = "        - name: global-customer-id\n"
            + "          in: query\n"
            + "          description: Customer ID description\n"
            + "          required: false\n"
            + "          type: string\n";

        String result = xsdElement.getQueryParamYAML();
        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetQueryParamYAML_withoutDescription() {
        // Mock Element without description (empty description)
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("global-customer-id");
        Mockito.when(element.getAttribute("description")).thenReturn(""); // Empty description
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.String");

        XSDElement xsdElement = new XSDElement(element);

        String expectedYAML = "        - name: global-customer-id\n"
            + "          in: query\n"
            + "          required: false\n"
            + "          type: string\n";

        String result = xsdElement.getQueryParamYAML();
        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetQueryParamYAML_withNullDescription() {
        // Mock Element with null description
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("global-customer-id");
        Mockito.when(element.getAttribute("description")).thenReturn(null); // Null description
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.String");

        XSDElement xsdElement = new XSDElement(element);

        String expectedYAML = "        - name: global-customer-id\n"
            + "          in: query\n"
            + "          required: false\n"
            + "          type: string\n";

        String result = xsdElement.getQueryParamYAML();
        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetQueryParamYAML_withLongType() {
        // Mock Element with Long type
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("customer-id");
        Mockito.when(element.getAttribute("description")).thenReturn("Customer ID");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.Long");

        XSDElement xsdElement = new XSDElement(element);

        String expectedYAML = "        - name: customer-id\n"
            + "          in: query\n"
            + "          description: Customer ID\n"
            + "          required: false\n"
            + "          type: integer\n"
            + "          format: int64\n";

        String result = xsdElement.getQueryParamYAML();
        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetQueryParamYAML_withIntegerType() {
        // Mock Element with Integer type
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("order-id");
        Mockito.when(element.getAttribute("description")).thenReturn("Order ID");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.Integer");

        XSDElement xsdElement = new XSDElement(element);

        String expectedYAML = "        - name: order-id\n"
            + "          in: query\n"
            + "          description: Order ID\n"
            + "          required: false\n"
            + "          type: integer\n"
            + "          format: int32\n";

        String result = xsdElement.getQueryParamYAML();
        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetQueryParamYAML_withFloatType() {
        // Mock Element with Float type
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("price");
        Mockito.when(element.getAttribute("description")).thenReturn("Price of the product");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.Float");

        XSDElement xsdElement = new XSDElement(element);

        String expectedYAML = "        - name: price\n"
            + "          in: query\n"
            + "          description: Price of the product\n"
            + "          required: false\n"
            + "          type: number\n"
            + "          format: float\n";

        String result = xsdElement.getQueryParamYAML();
        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetQueryParamYAML_withDoubleType() {
        // Mock Element with Double type
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("amount");
        Mockito.when(element.getAttribute("description")).thenReturn("Amount in dollars");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.Double");

        XSDElement xsdElement = new XSDElement(element);

        String expectedYAML = "        - name: amount\n"
            + "          in: query\n"
            + "          description: Amount in dollars\n"
            + "          required: false\n"
            + "          type: number\n"
            + "          format: double\n";

        String result = xsdElement.getQueryParamYAML();
        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetQueryParamYAML_withBooleanType() {
        // Mock Element with Boolean type
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("active");
        Mockito.when(element.getAttribute("description")).thenReturn("Active status of user");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.Boolean");

        XSDElement xsdElement = new XSDElement(element);

        String expectedYAML = "        - name: active\n"
            + "          in: query\n"
            + "          description: Active status of user\n"
            + "          required: false\n"
            + "          type: boolean\n";

        String result = xsdElement.getQueryParamYAML();
        assertEquals(expectedYAML, result);
    }

    // Add a test for when type is unrecognized, to ensure the method handles unexpected types correctly
    @Test
    public void testGetQueryParamYAML_withUnrecognizedType() {
        // Mock Element with an unrecognized type
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("some-id");
        Mockito.when(element.getAttribute("description")).thenReturn("Some ID");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.Unknown");

        XSDElement xsdElement = new XSDElement(element);

        String expectedYAML = "        - name: some-id\n"
            + "          in: query\n"
            + "          description: Some ID\n"
            + "          required: false\n";
        String result = xsdElement.getQueryParamYAML();
        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetPathParamYAML_withDescription() {
        // Mock Element with description
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("Inventory");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.String");

        XSDElement xsdElement = new XSDElement(element);

        String elementDescription = "Inventory";
        String result = xsdElement.getPathParamYAML(elementDescription, null);

        String expectedYAML = "        - name: Inventory\n"
            + "          in: path\n"
            + "          description: Inventory\n"
            + "          required: true\n"
            + "          type: string\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetPathParamYAML_withoutDescription() {
        // Mock Element without description
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("Inventory");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.String");

        XSDElement xsdElement = new XSDElement(element);

        String elementDescription = "";
        String result = xsdElement.getPathParamYAML(elementDescription, null);

        String expectedYAML = "        - name: Inventory\n"
            + "          in: path\n"
            + "          required: true\n"
            + "          type: string\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetPathParamYAML_withOverrideName() {
        // Mock Element with override name
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("Inventory");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.String");

        XSDElement xsdElement = new XSDElement(element);

        String elementDescription = "Inventory";
        String overrideName = "CustomInventory";
        String result = xsdElement.getPathParamYAML(elementDescription, overrideName);

        String expectedYAML = "        - name: CustomInventory\n"
            + "          in: path\n"
            + "          description: Inventory\n"
            + "          required: true\n"
            + "          type: string\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetPathParamYAML_withLongType() {
        // Mock Element with Long type
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("customer-id");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.Long");

        XSDElement xsdElement = new XSDElement(element);

        String elementDescription = "Customer ID";
        String result = xsdElement.getPathParamYAML(elementDescription, null);

        String expectedYAML = "        - name: customer-id\n"
            + "          in: path\n"
            + "          description: Customer ID\n"
            + "          required: true\n"
            + "          type: integer\n"
            + "          format: int64\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetPathParamYAML_withIntegerType() {
        // Mock Element with Integer type
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("order-id");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.Integer");

        XSDElement xsdElement = new XSDElement(element);

        String elementDescription = "Order ID";
        String result = xsdElement.getPathParamYAML(elementDescription, null);

        String expectedYAML = "        - name: order-id\n"
            + "          in: path\n"
            + "          description: Order ID\n"
            + "          required: true\n"
            + "          type: integer\n"
            + "          format: int32\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetPathParamYAML_withFloatType() {
        // Mock Element with Float type
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("price");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.Float");

        XSDElement xsdElement = new XSDElement(element);

        String elementDescription = "Price of the product";
        String result = xsdElement.getPathParamYAML(elementDescription, null);

        String expectedYAML = "        - name: price\n"
            + "          in: path\n"
            + "          description: Price of the product\n"
            + "          required: true\n"
            + "          type: number\n"
            + "          format: float\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetPathParamYAML_withDoubleType() {
        // Mock Element with Double type
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("amount");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.Double");

        XSDElement xsdElement = new XSDElement(element);

        String elementDescription = "Amount in dollars";
        String result = xsdElement.getPathParamYAML(elementDescription, null);

        String expectedYAML = "        - name: amount\n"
            + "          in: path\n"
            + "          description: Amount in dollars\n"
            + "          required: true\n"
            + "          type: number\n"
            + "          format: double\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetPathParamYAML_withBooleanType() {
        // Mock Element with Boolean type
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("active");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.Boolean");

        XSDElement xsdElement = new XSDElement(element);

        String elementDescription = "Active status of user";
        String result = xsdElement.getPathParamYAML(elementDescription, null);

        String expectedYAML = "        - name: active\n"
            + "          in: path\n"
            + "          description: Active status of user\n"
            + "          required: true\n"
            + "          type: boolean\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetPathParamYAML_withNullOverrideName() {
        // Mock Element with null override name
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("inventory-id");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.String");

        XSDElement xsdElement = new XSDElement(element);

        String elementDescription = "Inventory ID";
        String result = xsdElement.getPathParamYAML(elementDescription, null);

        String expectedYAML = "        - name: inventory-id\n"
            + "          in: path\n"
            + "          description: Inventory ID\n"
            + "          required: true\n"
            + "          type: string\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetPathParamYAML_withEmptyOverrideName() {
        // Mock Element with empty override name
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn("inventory-id");
        Mockito.when(element.getAttribute("type")).thenReturn("java.lang.String");

        XSDElement xsdElement = new XSDElement(element);

        String elementDescription = "Inventory ID";
        String overrideName = "";
        String result = xsdElement.getPathParamYAML(elementDescription, overrideName);

        String expectedYAML = "        - name: \n"
            + "          in: path\n"
            + "          description: Inventory ID\n"
            + "          required: true\n"
            + "          type: string\n";
        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetHTMLElement_withStringType_withoutAnnotation() {
        SchemaVersion schemaVersion = new SchemaVersion("v11");  // Use version "v11"
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        when(htmlDriver.getXmlRootElementName(anyString())).thenReturn("stringElement");

        // Create a dummy Element (XML Element)
        Document document = createTestXMLDocument();
        Element element = document.createElement("testElement");
        element.setAttribute("name", "testName");
        element.setAttribute("type", "java.lang.String");
        element.setAttribute("required", "false");

        // Initialize XSDElement with the element
        XSDElement xsdelement = new XSDElement(element, "unbounded");

        // Call the method to get the actual HTML element without annotations
        String actualHtml = xsdelement.getHTMLElement(schemaVersion, false, htmlDriver);

        // Assert the generated HTML contains the expected elements and doesn't include annotation text
        assertThat(actualHtml, containsString("<xs:element name=\"testName\""));
        assertThat(actualHtml, containsString("type=\"xs:string\""));
        assertThat(actualHtml, containsString("minOccurs=\"0\""));
        assertThat(actualHtml, containsString("/>"));
    }

    // Test for Long type without annotation
    @Test
    public void testGetHTMLElement_withLongType_withoutAnnotation() {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        when(htmlDriver.getXmlRootElementName(anyString())).thenReturn("longElement");

        Document document = createTestXMLDocument();
        Element element = document.createElement("testElement");
        element.setAttribute("name", "testName");
        element.setAttribute("type", "java.lang.Long");
        element.setAttribute("required", "false");

        XSDElement xsdelement = new XSDElement(element, "unbounded");

        String actualHtml = xsdelement.getHTMLElement(schemaVersion, false, htmlDriver);

        assertThat(actualHtml, containsString("type=\"xs:unsignedInt\""));
        assertThat(actualHtml, containsString("minOccurs=\"0\""));
        assertThat(actualHtml, containsString("/>"));
    }

    // Test for Integer type without annotation
    @Test
    public void testGetHTMLElement_withIntegerType_withoutAnnotation() {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        when(htmlDriver.getXmlRootElementName(anyString())).thenReturn("intElement");

        Document document = createTestXMLDocument();
        Element element = document.createElement("testElement");
        element.setAttribute("name", "testName");
        element.setAttribute("type", "java.lang.Integer");
        element.setAttribute("required", "false");

        XSDElement xsdelement = new XSDElement(element, "unbounded");

        String actualHtml = xsdelement.getHTMLElement(schemaVersion, false, htmlDriver);

        assertThat(actualHtml, containsString("type=\"xs:int\""));
        assertThat(actualHtml, containsString("minOccurs=\"0\""));
        assertThat(actualHtml, containsString("/>"));
    }

    // Test for Float type without annotation
    @Test
    public void testGetHTMLElement_withFloatType_withoutAnnotation() {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        when(htmlDriver.getXmlRootElementName(anyString())).thenReturn("floatElement");

        Document document = createTestXMLDocument();
        Element element = document.createElement("testElement");
        element.setAttribute("name", "testName");
        element.setAttribute("type", "java.lang.Float");
        element.setAttribute("required", "false");

        XSDElement xsdelement = new XSDElement(element, "unbounded");

        String actualHtml = xsdelement.getHTMLElement(schemaVersion, false, htmlDriver);

        assertThat(actualHtml, containsString("type=\"xs:float\""));
        assertThat(actualHtml, containsString("minOccurs=\"0\""));
        assertThat(actualHtml, containsString("/>"));
    }

    // Test for Double type without annotation
    @Test
    public void testGetHTMLElement_withDoubleType_withoutAnnotation() {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        when(htmlDriver.getXmlRootElementName(anyString())).thenReturn("doubleElement");

        Document document = createTestXMLDocument();
        Element element = document.createElement("testElement");
        element.setAttribute("name", "testName");
        element.setAttribute("type", "java.lang.Double");
        element.setAttribute("required", "false");

        XSDElement xsdelement = new XSDElement(element, "unbounded");

        String actualHtml = xsdelement.getHTMLElement(schemaVersion, false, htmlDriver);

        assertThat(actualHtml, containsString("type=\"xs:double\""));
        assertThat(actualHtml, containsString("minOccurs=\"0\""));
        assertThat(actualHtml, containsString("/>"));
    }

    // Test for Boolean type without annotation
    @Test
    public void testGetHTMLElement_withBooleanType_withoutAnnotation() {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        when(htmlDriver.getXmlRootElementName(anyString())).thenReturn("booleanElement");

        Document document = createTestXMLDocument();
        Element element = document.createElement("testElement");
        element.setAttribute("name", "testName");
        element.setAttribute("type", "java.lang.Boolean");
        element.setAttribute("required", "false");

        XSDElement xsdelement = new XSDElement(element, "unbounded");

        String actualHtml = xsdelement.getHTMLElement(schemaVersion, false, htmlDriver);

        assertThat(actualHtml, containsString("type=\"xs:boolean\""));
        assertThat(actualHtml, containsString("minOccurs=\"0\""));
        assertThat(actualHtml, containsString("/>"));
    }

    // Test for ArrayList container type without annotation
    @Test
    public void testGetHTMLElement_withArrayListContainerType_withoutAnnotation() {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        when(htmlDriver.getXmlRootElementName(anyString())).thenReturn("listElement");

        Document document = createTestXMLDocument();
        Element element = document.createElement("testElement");
        element.setAttribute("name", "testName");
        element.setAttribute("type", "java.lang.String");
        element.setAttribute("required", "false");
        element.setAttribute("container-type", "java.util.ArrayList");

        XSDElement xsdelement = new XSDElement(element, "unbounded");

        String actualHtml = xsdelement.getHTMLElement(schemaVersion, false, htmlDriver);

        assertThat(actualHtml, containsString("maxOccurs=\"unbounded\""));
        assertThat(actualHtml, containsString("minOccurs=\"0\""));
        assertThat(actualHtml, containsString("/>"));
    }

    @Test
    public void testGetTypePropertyYAML_withStringType() {
        when(xsdelement.getAttribute("name")).thenReturn("property-name");

        // Mock the getAttribute method for the type
        when(xsdelement.getAttribute("type")).thenReturn("java.lang.String");

        // Mock the getElementsByTagName method to return a NodeList
        NodeList mockNodeList = mock(NodeList.class);
        when(mockNodeList.getLength()).thenReturn(0);  // No xml-properties present
        when(xsdelement.getElementsByTagName("xml-properties")).thenReturn(mockNodeList);

        String result = xsdelement.getTypePropertyYAML(false);

        String expectedYAML = "      property-name:\n"
            + "        type: string\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetTypePropertyYAML_withLongType() {
        when(xsdelement.getAttribute("name")).thenReturn("property-name");

        // Mock the getAttribute method for the type
        when(xsdelement.getAttribute("type")).thenReturn("java.lang.Long");

        // Mock the getElementsByTagName method to return a NodeList
        NodeList mockNodeList = mock(NodeList.class);
        when(mockNodeList.getLength()).thenReturn(0);  // No xml-properties present
        when(xsdelement.getElementsByTagName("xml-properties")).thenReturn(mockNodeList);

        String result = xsdelement.getTypePropertyYAML(false);

        String expectedYAML = "      property-name:\n"
            + "        type: integer\n"
            + "        format: int64\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetTypePropertyYAML_withIntegerType() {
        when(xsdelement.getAttribute("name")).thenReturn("property-name");

        // Mock the getAttribute method for the type
        when(xsdelement.getAttribute("type")).thenReturn("java.lang.Integer");

        // Mock the getElementsByTagName method to return a NodeList
        NodeList mockNodeList = mock(NodeList.class);
        when(mockNodeList.getLength()).thenReturn(0);  // No xml-properties present
        when(xsdelement.getElementsByTagName("xml-properties")).thenReturn(mockNodeList);

        String result = xsdelement.getTypePropertyYAML(false);

        String expectedYAML = "      property-name:\n"
            + "        type: integer\n"
            + "        format: int32\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetTypePropertyYAML_withFloatType() {
        when(xsdelement.getAttribute("name")).thenReturn("property-name");

        // Mock the getAttribute method for the type
        when(xsdelement.getAttribute("type")).thenReturn("java.lang.Float");

        // Mock the getElementsByTagName method to return a NodeList
        NodeList mockNodeList = mock(NodeList.class);
        when(mockNodeList.getLength()).thenReturn(0);  // No xml-properties present
        when(xsdelement.getElementsByTagName("xml-properties")).thenReturn(mockNodeList);

        String result = xsdelement.getTypePropertyYAML(false);

        String expectedYAML = "      property-name:\n"
            + "        type: number\n"
            + "        format: float\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetTypePropertyYAML_withDoubleType() {
        when(xsdelement.getAttribute("name")).thenReturn("property-name");

        // Mock the getAttribute method for the type
        when(xsdelement.getAttribute("type")).thenReturn("java.lang.Double");

        // Mock the getElementsByTagName method to return a NodeList
        NodeList mockNodeList = mock(NodeList.class);
        when(mockNodeList.getLength()).thenReturn(0);  // No xml-properties present
        when(xsdelement.getElementsByTagName("xml-properties")).thenReturn(mockNodeList);

        String result = xsdelement.getTypePropertyYAML(false);

        String expectedYAML = "      property-name:\n"
            + "        type: number\n"
            + "        format: double\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetTypePropertyYAML_withBooleanType() {
        when(xsdelement.getAttribute("name")).thenReturn("property-name");

        // Mock the getAttribute method for the type
        when(xsdelement.getAttribute("type")).thenReturn("java.lang.Boolean");

        // Mock the getElementsByTagName method to return a NodeList
        NodeList mockNodeList = mock(NodeList.class);
        when(mockNodeList.getLength()).thenReturn(0);  // No xml-properties present
        when(xsdelement.getElementsByTagName("xml-properties")).thenReturn(mockNodeList);

        String result = xsdelement.getTypePropertyYAML(false);

        String expectedYAML = "      property-name:\n"
            + "        type: boolean\n";

        assertEquals(expectedYAML, result);
    }

    @Test
    public void testGetTypePropertyYAML_withDslStartNode_noDescription() {
        when(xsdelement.getAttribute("name")).thenReturn("property-name");

        // Mock the getAttribute method for the type
        when(xsdelement.getAttribute("type")).thenReturn("java.lang.String");

        // Mock the getElementsByTagName method to return a NodeList
        NodeList mockNodeList = mock(NodeList.class);
        when(mockNodeList.getLength()).thenReturn(0);  // No xml-properties present
        when(xsdelement.getElementsByTagName("xml-properties")).thenReturn(mockNodeList);

        // Call the method with isDslStartNode set to true
        String result = xsdelement.getTypePropertyYAML(true);

        String expectedYAML = "      property-name:\n"
            + "        type: string\n"
            + "        description: |\n"
            + "          \n"
            + "          *This property can be used as a filter to find the start node for a dsl query\n";

        assertEquals(expectedYAML, result);
    }

}
