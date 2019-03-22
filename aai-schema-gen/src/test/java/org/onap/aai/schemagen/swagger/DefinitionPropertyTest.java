/*
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2019 Huawei Technologies (Australia) Pty Ltd. All rights reserved.
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

package org.onap.aai.schemagen.swagger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DefinitionPropertyTest {
    Definition.Property theProperty = null;
    String propertyName;
    String propertyType;
    String propertyReference;
    String result;

    /**
     * Parameters for the test cases all following same pattern.
     */
    @Parameters
    public static Collection<String[]> testConditions() {
        String inputs[][] = {
            {"name1", "type1", "ref1",
                "Property{propertyName='name1', propertyType='type1', propertyReference='ref1'}"},
            {"name2", "type2", "ref2",
                "Property{propertyName='name2', propertyType='type2', propertyReference='ref2'}"},
            {"fake", "random", "bluff",
                "Property{propertyName='fake', propertyType='random', propertyReference='bluff'}"}};
        return (Arrays.asList(inputs));
    }

    /**
     * Constructor for the test cases all following same pattern.
     */
    public DefinitionPropertyTest(String propertyName, String propertyType,
        String propertyReference, String result) {
        super();
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        this.propertyReference = propertyReference;
        this.result = result;
    }

    /**
     * Initialise the test object.
     */
    @Before
    public void setUp() throws Exception {
        theProperty = new Definition.Property();
    }

    /**
     * Perform the test on the test object.
     */
    @Test
    public void testDefinitionProperty() {
        theProperty.setPropertyName(this.propertyName);
        theProperty.setPropertyType(this.propertyType);
        theProperty.setPropertyReference(this.propertyReference);
        assertThat(theProperty.toString(), is(this.result));

        // other stuff that can be set but not necessarily
        // included in the toString() output
        theProperty.setHasType(true);
        assertThat(theProperty.isHasType(), is(true));

        theProperty.setRequired(true);
        assertThat(theProperty.isRequired(), is(true));

        theProperty.setHasPropertyReference(true);
        assertThat(theProperty.isHasPropertyReference(), is(true));

        theProperty.setPropertyReferenceObjectName("someName");
        assertThat(theProperty.getPropertyReferenceObjectName(), is("someName"));

        theProperty.setPropertyDescription("some description");
        assertThat(theProperty.getPropertyDescription(), is("some description"));

        theProperty.setHasPropertyDescription(true);
        assertThat(theProperty.isHasPropertyDescription(), is(true));
    }
}
