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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.HashMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XSDJavaTypeTest extends XSDElementTest {

    XSDJavaType xsdJavaType;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testXSDJavaTypeElement() {
        HashMap<String, String> map = new HashMap<String, String>();
        HashMap<String, String> target = new HashMap<String, String>();
        target.put("Customer", "global-customer-id");
        target.put("Business", "customers");
        target.put("Inventory", "business");
        target.put("Customers", "customer");
        target.put("ServiceSubscriptions", "service-subscription");
        target.put("ServiceSubscription", "service-type");

        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            XSDJavaType javaType = new XSDJavaType(javaTypeElement);
            map.put(javaType.name(), javaType.getItemName());
        }
        for (String key : map.keySet()) {
            assertThat("For key: " + key, map.get(key), equalTo(target.get(key)));
        }
    }

    @Test
    public void testGetItemName() {
        HashMap<String, String> map = new HashMap<String, String>();
        HashMap<String, String> target = new HashMap<String, String>();
        target.put("Customer", "global-customer-id");
        target.put("Business", "customers");
        target.put("Inventory", "business");
        target.put("Customers", "customer");
        target.put("ServiceSubscriptions", "service-subscription");
        target.put("ServiceSubscription", "service-type");

        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            XSDJavaType javaType = new XSDJavaType(javaTypeElement);
            map.put(javaType.name(), javaType.getItemName());
        }
        for (String key : map.keySet()) {
            assertThat("For key: " + key, map.get(key), equalTo(target.get(key)));
        }
    }

    @Test
    public void testGetArrayType() {
        HashMap<String, String> map = new HashMap<String, String>();
        HashMap<String, String> target = new HashMap<String, String>();
        target.put("Customer", null);
        target.put("Business", null);
        target.put("Inventory", null);
        target.put("Customers", "customer");
        target.put("ServiceSubscriptions", "service-subscription");
        target.put("ServiceSubscription", null);

        for (int i = 0; i < javaTypeNodes.getLength(); ++i) {
            XSDElement javaTypeElement = new XSDElement((Element) javaTypeNodes.item(i));
            XSDJavaType javaType = new XSDJavaType(javaTypeElement);
            map.put(javaType.name(), javaType.getArrayType());
        }
        for (String key : map.keySet()) {
            assertThat(map.get(key), equalTo(target.get(key)));
        }
    }

    @Test
    public void testGetItemName_withEmptyNodeList() {
        NodeList nodeListMock = mock(NodeList.class);
        XSDElement xsdelementMock = mock(XSDElement.class);
        // Set up mocks to simulate behavior for empty NodeList
        when(xsdelementMock.getElementsByTagName("java-attributes")).thenReturn(nodeListMock);
        when(nodeListMock.getLength()).thenReturn(0);

        XSDJavaType javaType = new XSDJavaType(xsdelementMock);

        String itemName = javaType.getItemName();

        assertThat(itemName, equalTo(null));
    }

    @Test
    public void testGetArrayType_withEmptyNodeList() {
        NodeList nodeListMock = mock(NodeList.class);
        XSDElement xsdelementMock = mock(XSDElement.class);
        when(xsdelementMock.getElementsByTagName("java-attributes")).thenReturn(nodeListMock);
        when(nodeListMock.getLength()).thenReturn(0);

        XSDJavaType javaType = new XSDJavaType(xsdelementMock);

        String itemName = javaType.getArrayType();

        assertThat(itemName, equalTo(null));
    }
}
