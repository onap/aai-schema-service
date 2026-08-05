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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsIn.in;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Every.everyItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.fail;

import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mockito;
import org.onap.aai.setup.SchemaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

    /**
     * The same OXM as {@code setUp(0)} except that {@code Customer} carries constraint facets, so
     * the generators can be exercised end to end on an input that declares them. No shipped OXM
     * version declares any facet, which is why the generated artefacts are unchanged by this
     * vocabulary until an OXM opts in.
     */
    public void setUpWithFacets() throws Exception {
        StringBuilder sb = new StringBuilder(maxSizeForXml);
        addNamespace(sb);
        addBusiness(sb);
        addCustomers(sb);
        addCustomerWithFacets(sb);
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

    private void addCustomerWithFacets(StringBuilder sb) {
        sb.append("<java-type name=\"Customer\">\n");
        sb.append("<xml-root-element name=\"customer\" />\n");
        sb.append("<java-attributes>\n");
        sb.append(
            "<xml-element java-attribute=\"globalCustomerId\" name=\"global-customer-id\" required=\"true\" type=\"java.lang.String\" xml-key=\"true\">\n");
        sb.append("<xml-properties>\n");
        sb.append(
            "<xml-property name=\"description\" value=\"Global customer id used across to uniquely identify customer.\" />\n");
        sb.append("<xml-property name=\"minLength\" value=\"1\" />\n");
        sb.append("<xml-property name=\"maxLength\" value=\"36\" />\n");
        sb.append("<xml-property name=\"pattern\" value=\"^[A-Za-z0-9-]+$\" />\n");
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
        sb.append("<xml-property name=\"allowedValues\" value=\"CUST,INFRA\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        sb.append(
            "<xml-element java-attribute=\"customerRank\" name=\"customer-rank\" type=\"java.lang.Integer\">\n");
        sb.append("<xml-properties>\n");
        sb.append("<xml-property name=\"description\" value=\"Rank of the customer.\" />\n");
        sb.append("<xml-property name=\"minimum\" value=\"0\" />\n");
        sb.append("<xml-property name=\"maximum\" value=\"100\" />\n");
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
            "<xml-element java-attribute=\"serviceSubscriptions\" name=\"service-subscriptions\" type=\"inventory.aai.onap.org.v11.ServiceSubscriptions\">\n");
        sb.append("<xml-properties>\n");
        // a facet on a reference to another node type has nothing to restrict and is ignored
        sb.append("<xml-property name=\"maxLength\" value=\"10\" />\n");
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
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
    public void testGetQueryParameter() {
        GenerationContext context = new GenerationContext();
        List<String> indexedProps = new ArrayList<>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            if (javaTypeElement.getContainerProperty() != null) {
                indexedProps.addAll(javaTypeElement.getIndexedProps());
                String container = javaTypeElement.getContainerProperty();
                List<Parameter> containerProps = new ArrayList<>();
                NodeList xmlElementNodes = javaTypeElement.getElementsByTagName("xml-element");
                for (int j = 0; j < xmlElementNodes.getLength(); ++j) {
                    XSDElement xmlElement = new XSDElement((Element) xmlElementNodes.item(j));
                    if (indexedProps.contains(xmlElement.name()))
                        containerProps.add(xmlElement.getQueryParameter());
                }
                context.addContainerProps(container, containerProps);
            }
        }

        List<Parameter> customers = context.getContainerProps("customers");

        assertEquals(List.of("global-customer-id", "subscriber-name", "subscriber-type"),
            customers.stream().map(Parameter::getName).toList());
        customers.forEach(parameter -> {
            QueryParameter query = (QueryParameter) parameter;
            assertEquals("query", query.getIn());
            assertEquals("string", query.getType());
            // an indexed property is a filter, so it is never required
            assertFalse(query.getRequired());
        });
    }

    @Test
    public void testGetPathParameter() {
        List<Parameter> pathParams = new ArrayList<>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            pathParams.add(javaTypeElement.getPathParameter(javaTypeElement.name()));
        }

        assertThat(pathParams.stream().map(Parameter::getName).toList(),
            containsInAnyOrder("Inventory", "Business", "Customers", "Customer",
                "ServiceSubscriptions", "ServiceSubscription"));
        pathParams.forEach(parameter -> {
            assertEquals("path", parameter.getIn());
            // a java type is not a standard type, so it is left untyped
            assertNull(((PathParameter) parameter).getType());
            // an object the path addresses must be named for the path to address it
            assertTrue(parameter.getRequired());
            assertEquals(parameter.getName(), parameter.getDescription());
        });
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
    public void testGetTypeProperty() {
        Map<String, Property> byName = new LinkedHashMap<>();
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            byName.put(javaTypeElement.name(), javaTypeElement.getTypeProperty(false));
        }

        assertEquals(List.of("Inventory", "Business", "Customers", "Customer",
            "ServiceSubscriptions", "ServiceSubscription"), List.copyOf(byName.keySet()));
        // a java type is not a standard type, so it is left untyped
        byName.values().forEach(property -> assertNull(property.getType()));
        // the root has no description of its own in the OXM
        assertNull(byName.get("Inventory").getDescription());
        assertEquals("Namespace for business related constructs",
            byName.get("Business").getDescription());
        assertEquals(
            "Collection of customer identifiers to provide linkage back to BSS information.",
            byName.get("Customers").getDescription());
        assertEquals("customer identifiers to provide linkage back to BSS information.",
            byName.get("Customer").getDescription());
        assertEquals("Collection of objects that group service instances.",
            byName.get("ServiceSubscriptions").getDescription());
        assertEquals("Object that group service instances.",
            byName.get("ServiceSubscription").getDescription());
    }

    @Test
    public void testGetTypeProperty_asDslStartNode() {
        XSDElement customer = javaTypeNamed("Customer");

        String description = customer.getTypeProperty(true).getDescription();

        assertEquals(
            List.of("customer identifiers to provide linkage back to BSS information.",
                "*This property can be used as a filter to find the start node for a dsl query"),
            description.lines().toList());
        // the trailing newline is what makes this render as a literal block, not as one line
        assertTrue(description.endsWith("\n"));
    }

    private XSDElement javaTypeNamed(String name) {
        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            if (name.equals(javaTypeElement.name())) {
                return javaTypeElement;
            }
        }
        throw new AssertionError("no java-type named " + name + " in the test OXM");
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
        SchemaVersion schemaVersion = new SchemaVersion("v11"); // Use version "v11"
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);

        // Mock getXmlRootElementName() to return a custom value
        when(htmlDriver.getXmlRootElementName(anyString())).thenReturn("stringElement");

        // Create a dummy Element (XML Element)
        Document document = createTestXMLDocument(); // Assuming this method creates a Document
                                                     // object
        Element element = document.createElement("testElement");
        element.setAttribute("name", "testName");
        element.setAttribute("type", "java.lang.String");
        element.setAttribute("required", "false");
        element.setAttribute("container-type", "java.util.ArrayList");

        // Initialize XSDElement with the element and maxOccurs value
        XSDElement xsdelement = new XSDElement(element, "unbounded");

        // Mock the behavior of XSDElement's getHTMLAnnotation() to return an empty string (no
        // annotation)
        XSDElement mockedXSDElement = mock(XSDElement.class);
        when(mockedXSDElement.getHTMLAnnotation("field", "          ")).thenReturn("");

        // Call the method to get the actual HTML element without annotations
        String actualHtml = xsdelement.getHTMLElement(schemaVersion, true, htmlDriver);

        // Assert the generated HTML contains the expected elements and doesn't include annotation
        // text
        assertThat(actualHtml, containsString("<xs:element name=\"testName\""));
        assertThat(actualHtml, containsString("type=\"xs:string\""));
        assertThat(actualHtml, containsString("minOccurs=\"0\""));
        assertThat(actualHtml, containsString("maxOccurs=\"unbounded\""));
        assertThat(actualHtml, not(containsString("Some annotation text")));
        assertThat(actualHtml, containsString("/>"));
    }

    // Helper method to create a simple Document with a root element (can be extended as per the
    // test needs)
    private Document createTestXMLDocument() {
        try {
            // Use a simple DocumentBuilderFactory to create an empty Document
            javax.xml.parsers.DocumentBuilderFactory factory =
                javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            return document;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void testGetElementReturnsWrappedElement() {
        assertEquals(xmlElementElement, xsdelement.getElement());
    }

    @Test
    public void testGetAttribute() {
        when(xmlElementElement.getAttribute("name")).thenReturn("value");

        assertEquals("value", xsdelement.getAttribute("name"));
    }

    @Test
    public void testHasAttribute() {
        when(xmlElementElement.hasAttribute("name")).thenReturn(true);

        assertTrue(xsdelement.hasAttribute("name"));
    }

    @Test
    public void testGetAttributes() {
        NamedNodeMap attributes = mock(NamedNodeMap.class);
        when(xmlElementElement.getAttributes()).thenReturn(attributes);

        assertEquals(attributes, xsdelement.getAttributes());
    }

    @Test
    public void testGetElementsByTagName() {
        NodeList nodeList = mock(NodeList.class);
        when(xmlElementElement.getElementsByTagName("name")).thenReturn(nodeList);

        assertEquals(nodeList, xsdelement.getElementsByTagName("name"));
    }

    @Test
    public void testGetParentNode() {
        Node parentNode = mock(Node.class);
        when(xmlElementElement.getParentNode()).thenReturn(parentNode);

        assertEquals(parentNode, xsdelement.getParentNode());
    }

    @Test
    public void testGetRequiresProperty() {
        // Mocking the XML element containing the <xml-property name="requires"> element
        String xmlString = "<xml-bindings>" + "<java-type name=\"Business\">" + "<xml-properties>"
            + "<xml-property name=\"description\" value=\"Namespace for business related constructs\" />"
            + "<xml-property name=\"requires\" value=\"some-required-property\" />" + // This is
                                                                                      // what we're
                                                                                      // looking for
            "</xml-properties>" + "</java-type>" + "</xml-bindings>";

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
            assertEquals("some-required-property", requiresProperty,
                "The requires property should match the expected value.");
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            fail("An error occurred while parsing the XML or executing the test.");
        }
    }

    /**
     * The Java types an OXM element can declare, with the swagger type and format each maps to. A
     * type outside this set is not a standard type and is left untyped.
     */
    public static Stream<Arguments> javaTypes() {
        return Stream.of(arguments("java.lang.String", "string", null),
            arguments("java.lang.Long", "integer", "int64"),
            arguments("java.lang.Integer", "integer", "int32"),
            arguments("java.lang.Float", "number", "float"),
            arguments("java.lang.Double", "number", "double"),
            arguments("java.lang.Boolean", "boolean", null),
            arguments("java.lang.Unknown", null, null));
    }

    @ParameterizedTest(name = "a {0} query parameter is a {1}")
    @MethodSource("javaTypes")
    public void testGetQueryParameter_type(String javaType, String type, String format) {
        QueryParameter parameter = elementOfType("customer-id", javaType).getQueryParameter();

        assertEquals(type, parameter.getType());
        assertEquals(format, parameter.getFormat());
    }

    @ParameterizedTest(name = "a {0} path parameter is a {1}")
    @MethodSource("javaTypes")
    public void testGetPathParameter_type(String javaType, String type, String format) {
        PathParameter parameter =
            elementOfType("customer-id", javaType).getPathParameter("Customer ID");

        assertEquals(type, parameter.getType());
        assertEquals(format, parameter.getFormat());
    }

    @Test
    public void testGetQueryParameter_withDescription() {
        Element element = elementNamed("global-customer-id", "java.lang.String");
        Mockito.when(element.getAttribute("description")).thenReturn("Customer ID description");

        QueryParameter parameter = new XSDElement(element).getQueryParameter();

        assertEquals("global-customer-id", parameter.getName());
        assertEquals("query", parameter.getIn());
        assertEquals("Customer ID description", parameter.getDescription());
        // a query parameter is a filter, so it is never required
        assertFalse(parameter.getRequired());
    }

    @ParameterizedTest(name = "a query parameter described by {0} has no description")
    @NullAndEmptySource
    public void testGetQueryParameter_withoutDescription(String description) {
        Element element = elementNamed("global-customer-id", "java.lang.String");
        Mockito.when(element.getAttribute("description")).thenReturn(description);

        assertNull(new XSDElement(element).getQueryParameter().getDescription());
    }

    @Test
    public void testGetPathParameter_withDescription() {
        PathParameter parameter =
            elementOfType("Inventory", "java.lang.String").getPathParameter("Inventory");

        assertEquals("Inventory", parameter.getName());
        assertEquals("path", parameter.getIn());
        assertEquals("Inventory", parameter.getDescription());
        // the object a path addresses has to be given for the path to address it
        assertTrue(parameter.getRequired());
    }

    @ParameterizedTest(name = "a path parameter described by {0} has no description")
    @NullAndEmptySource
    public void testGetPathParameter_withoutDescription(String description) {
        PathParameter parameter =
            elementOfType("Inventory", "java.lang.String").getPathParameter(description);

        assertNull(parameter.getDescription());
    }

    @Test
    public void testGetPathParameter_withOverrideName() {
        PathParameter parameter = elementOfType("Inventory", "java.lang.String")
            .getPathParameter("Inventory", "CustomInventory");

        assertEquals("CustomInventory", parameter.getName());
        // the override names the parameter for its place in the path; the description still
        // describes the schema property it came from
        assertEquals("Inventory", parameter.getDescription());
    }

    @Test
    public void testGetPathParameter_withoutOverrideName() {
        PathParameter parameter = elementOfType("inventory-id", "java.lang.String")
            .getPathParameter("Inventory ID", null);

        assertEquals("inventory-id", parameter.getName());
    }

    private static XSDElement elementOfType(String name, String javaType) {
        return new XSDElement(elementNamed(name, javaType));
    }

    private static Element elementNamed(String name, String javaType) {
        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getAttribute("name")).thenReturn(name);
        Mockito.when(element.getAttribute("type")).thenReturn(javaType);
        return element;
    }

    @Test
    public void testGetHTMLElement_withStringType_withoutAnnotation() {
        SchemaVersion schemaVersion = new SchemaVersion("v11"); // Use version "v11"
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

        // Assert the generated HTML contains the expected elements and doesn't include annotation
        // text
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

    @ParameterizedTest(name = "a {0} property is a {1}")
    @MethodSource("javaTypes")
    public void testGetTypeProperty_type(String javaType, String type, String format) {
        Property property = propertyElementOfType(javaType).getTypeProperty(false);

        assertEquals(type, property.getType());
        assertEquals(format, property.getFormat());
        assertNull(property.getDescription());
    }

    @Test
    public void testGetTypeProperty_withDslStartNode_noDescription() {
        Property property = propertyElementOfType("java.lang.String").getTypeProperty(true);

        // the note stands alone on the second line, so the property still describes only itself
        assertEquals(
            List.of("",
                "*This property can be used as a filter to find the start node for a dsl query"),
            property.getDescription().lines().toList());
    }

    /**
     * An {@code xml-element} of the given type declaring no {@code xml-properties} at all, which is
     * the shape the type mapping alone is exercised on.
     */
    private XSDElement propertyElementOfType(String javaType) {
        when(xmlElementElement.getAttribute("name")).thenReturn("property-name");
        when(xmlElementElement.getAttribute("type")).thenReturn(javaType);
        NodeList noProperties = mock(NodeList.class);
        when(noProperties.getLength()).thenReturn(0);
        when(xmlElementElement.getElementsByTagName("xml-properties")).thenReturn(noProperties);
        return xsdelement;
    }

    /**
     * Parses a single {@code <xml-element>} carrying the given constraint facet
     * {@code <xml-property>} entries, so the facet readers see the same DOM shape a real OXM file
     * produces (facet properties are looked up relative to their own element).
     */
    private XSDElement facetElement(String type, String... facetNameValuePairs) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml-element java-attribute=\"prop\" name=\"property-name\" type=\"")
            .append(type).append("\">\n");
        sb.append("<xml-properties>\n");
        for (int i = 0; i < facetNameValuePairs.length; i += 2) {
            sb.append("<xml-property name=\"").append(facetNameValuePairs[i]).append("\" value=\"")
                .append(facetNameValuePairs[i + 1]).append("\" />\n");
        }
        sb.append("</xml-properties>\n");
        sb.append("</xml-element>\n");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Document facetDoc =
            dbFactory.newDocumentBuilder().parse(new InputSource(new StringReader(sb.toString())));
        return new XSDElement(facetDoc.getDocumentElement(), "unbounded");
    }

    @Test
    public void testGetTypeProperty_withStringFacets() throws Exception {
        XSDElement element = facetElement("java.lang.String", "minLength", "1", "maxLength", "64",
            "pattern", "^[0-9]{3}$");

        StringProperty property =
            assertInstanceOf(StringProperty.class, element.getTypeProperty(false));

        assertEquals(1, property.getMinLength());
        assertEquals(64, property.getMaxLength());
        assertEquals("^[0-9]{3}$", property.getPattern());
    }

    @Test
    public void testGetTypeProperty_withAllowedValuesBecomesEnum() throws Exception {
        XSDElement element = facetElement("java.lang.String", "allowedValues",
            "in-service-path, planned ,, provisioned");

        StringProperty property =
            assertInstanceOf(StringProperty.class, element.getTypeProperty(false));

        assertEquals(List.of("in-service-path", "planned", "provisioned"), property.getEnum());
    }

    @Test
    public void testGetTypeProperty_withNumericFacets() throws Exception {
        XSDElement element = facetElement("java.lang.Integer", "minimum", "0", "maximum", "65535");

        IntegerProperty property =
            assertInstanceOf(IntegerProperty.class, element.getTypeProperty(false));

        assertEquals(new BigDecimal("0"), property.getMinimum());
        assertEquals(new BigDecimal("65535"), property.getMaximum());
    }

    @Test
    public void testGetTypeProperty_withFacetsAndDescription() throws Exception {
        XSDElement element = facetElement("java.lang.String", "maxLength", "8", "description",
            "Mobile country code.");

        StringProperty property =
            assertInstanceOf(StringProperty.class, element.getTypeProperty(false));

        assertEquals(8, property.getMaxLength());
        assertEquals("Mobile country code.", property.getDescription());
    }

    @Test
    public void testGetTypeProperty_facetNotLegalForTypeIsSkipped() throws Exception {
        // minLength/pattern are string-only, minimum/maximum are numeric-only
        LongProperty numeric = assertInstanceOf(LongProperty.class,
            facetElement("java.lang.Long", "minLength", "1", "pattern", "^\\d+$", "minimum", "1")
                .getTypeProperty(false));
        assertEquals(new BigDecimal("1"), numeric.getMinimum());

        StringProperty string = assertInstanceOf(StringProperty.class,
            facetElement("java.lang.String", "minimum", "1", "maximum", "9", "maxLength", "9")
                .getTypeProperty(false));
        assertEquals(9, string.getMaxLength());
    }

    @Test
    public void testGetTypeProperty_booleanTakesNoFacets() throws Exception {
        XSDElement element = facetElement("java.lang.Boolean", "minLength", "1", "minimum", "0",
            "allowedValues", "true,false");

        BooleanProperty property =
            assertInstanceOf(BooleanProperty.class, element.getTypeProperty(false));

        assertNull(property.getEnum());
    }

    @Test
    public void testGetTypeProperty_emptyFacetValueIsIgnored() throws Exception {
        XSDElement element = facetElement("java.lang.String", "pattern", "", "maxLength", "3");

        StringProperty property =
            assertInstanceOf(StringProperty.class, element.getTypeProperty(false));

        assertNull(property.getPattern());
        assertEquals(3, property.getMaxLength());
    }

    @Test
    public void testGetHTMLElement_withFacetsEmitsInlineRestriction() throws Exception {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        XSDElement element = facetElement("java.lang.String", "minLength", "3", "maxLength", "3",
            "pattern", "^[0-9]{3}$");

        String actual = element.getHTMLElement(schemaVersion, false, htmlDriver);

        // an xs:element cannot carry a type attribute and an inline xs:simpleType at the same time
        assertThat(actual, not(containsString("type=\"xs:string\"")));
        assertThat(actual, containsString("<xs:element name=\"property-name\" minOccurs=\"0\">"));
        assertThat(actual, containsString("<xs:simpleType>"));
        assertThat(actual, containsString("<xs:restriction base=\"xs:string\">"));
        assertThat(actual, containsString("<xs:minLength value=\"3\"/>"));
        assertThat(actual, containsString("<xs:maxLength value=\"3\"/>"));
        assertThat(actual, containsString("<xs:pattern value=\"^[0-9]{3}$\"/>"));
        assertThat(actual, containsString("</xs:restriction>"));
        assertThat(actual, containsString("</xs:simpleType>"));
        assertThat(actual, containsString("</xs:element>"));
    }

    @Test
    public void testGetHTMLElement_annotationPrecedesSimpleType() throws Exception {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        XSDElement element = facetElement("java.lang.String", "description", "Mobile country code.",
            "maxLength", "3");

        String actual = element.getHTMLElement(schemaVersion, true, htmlDriver);

        // the content model of xs:element is ordered: xs:annotation has to come first
        assertThat(actual, containsString("<xs:annotation>"));
        assertTrue(actual.indexOf("</xs:annotation>") < actual.indexOf("<xs:simpleType>"),
            "xs:annotation must precede xs:simpleType, got:\n" + actual);
    }

    @Test
    public void testGetHTMLElement_numericFacetsUseInclusiveBounds() throws Exception {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        XSDElement element = facetElement("java.lang.Integer", "minimum", "0", "maximum", "65535");

        String actual = element.getHTMLElement(schemaVersion, false, htmlDriver);

        assertThat(actual, not(containsString("type=\"xs:int\"")));
        assertThat(actual, containsString("<xs:restriction base=\"xs:int\">"));
        assertThat(actual, containsString("<xs:minInclusive value=\"0\"/>"));
        assertThat(actual, containsString("<xs:maxInclusive value=\"65535\"/>"));
    }

    @Test
    public void testGetHTMLElement_allowedValuesBecomeEnumerations() throws Exception {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        XSDElement element =
            facetElement("java.lang.String", "allowedValues", "planned, provisioned");

        String actual = element.getHTMLElement(schemaVersion, false, htmlDriver);

        assertThat(actual, containsString("<xs:enumeration value=\"planned\"/>"));
        assertThat(actual, containsString("<xs:enumeration value=\"provisioned\"/>"));
    }

    @Test
    public void testGetHTMLElement_facetValueIsXmlAttributeEscaped() throws Exception {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        // a pattern is the facet most likely to contain markup significant characters
        XSDElement element = facetElement("java.lang.String", "pattern", "^[a-z&amp;&lt;&quot;]+$");

        String actual = element.getHTMLElement(schemaVersion, false, htmlDriver);

        assertThat(actual, containsString("<xs:pattern value=\"^[a-z&amp;&lt;&quot;]+$\"/>"));
    }

    @Test
    public void testGetHTMLElement_facetsOnNodeTypeReferenceAreIgnored() throws Exception {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        when(htmlDriver.getXmlRootElementName(anyString())).thenReturn("service-subscriptions");
        XSDElement element = facetElement("inventory.aai.onap.org.v11.ServiceSubscriptions",
            "maxLength", "3", "pattern", "^[0-9]+$");

        String actual = element.getHTMLElement(schemaVersion, false, htmlDriver);

        assertThat(actual,
            containsString("<xs:element ref=\"tns:service-subscriptions\" minOccurs=\"0\"/>"));
        assertThat(actual, not(containsString("xs:simpleType")));
        assertThat(actual, not(containsString("xs:maxLength")));
    }

    @Test
    public void testGetHTMLElement_withoutFacetsKeepsTypeAttribute() throws Exception {
        SchemaVersion schemaVersion = new SchemaVersion("v11");
        HTMLfromOXM htmlDriver = mock(HTMLfromOXM.class);
        XSDElement element = facetElement("java.lang.String", "description", "no facets here");

        String actual = element.getHTMLElement(schemaVersion, false, htmlDriver);

        assertThat(actual, containsString(
            "<xs:element name=\"property-name\" type=\"xs:string\" minOccurs=\"0\"/>"));
        assertThat(actual, not(containsString("xs:simpleType")));
    }
}
