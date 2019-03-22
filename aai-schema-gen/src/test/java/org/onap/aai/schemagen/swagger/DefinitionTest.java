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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DefinitionTest {
    Definition theDefinition = null;
    String definitionName;
    String definitionDescription;
    String result;

    /**
     * Parameters for the test cases all following same pattern.
     */
    @Parameters
    public static Collection<String[]> testConditions() {
        String inputs[][] = {
            {"name1", "desc1", 
                "Definition{definitionName='name1', definitionDescription='desc1', propertyList=[]}"},
            {"name2", "desc2", 
                "Definition{definitionName='name2', definitionDescription='desc2', propertyList=[]}"},
            {"fake", "random", 
                "Definition{definitionName='fake', definitionDescription='random', propertyList=[]}"}};
        return (Arrays.asList(inputs));
    }

    /**
     * Constructor for the test cases all following same pattern.
     */
    public DefinitionTest(String definitionName, String definitionDescription, String result) {
        super();
        this.definitionName = definitionName;
        this.definitionDescription = definitionDescription;
        this.result = result;
    }

    /**
     * Initialise the test object.
     */
    @Before
    public void setUp() throws Exception {
        theDefinition = new Definition();
    }

    /**
     * Perform the test on the test object.
     */
    @Test
    public void testDefinitionProperty() {
        theDefinition.setDefinitionName(this.definitionName);
        theDefinition.setDefinitionDescription(this.definitionDescription);

        List<Definition.Property> tmpList1 = new ArrayList<Definition.Property>();
        theDefinition.setPropertyList(tmpList1);
        assertThat(theDefinition.toString(), is(this.result));

        // other stuff that can be set but not necessarily
        // included in the toString() output
        theDefinition.setHasDescription(true);
        assertThat(theDefinition.isHasDescription(), is(true));

        theDefinition.setSchemaPropertyList(tmpList1);
        assertThat(theDefinition.getSchemaPropertyList(), is(tmpList1));

        theDefinition.setRegularPropertyList(tmpList1);
        assertThat(theDefinition.getRegularPropertyList(), is(tmpList1));
    }
}
