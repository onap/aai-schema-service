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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ApiHttpVerbTest {
    Api.HttpVerb theVerb = null;
    List<String> tags;
    String type;
    String summary;
    String operationId;
    List<String> consumes;
    List<String> produces;
    String result;

    /**
     * Parameters for the test cases all following same pattern.
     */
    @Parameters
    public static Collection<String[]> testConditions() {
        String inputs[][] = {{"tag1,tag2", "typeA", "summaryB", "operationC", "consumesD,consumesE",
            "producesF,producesG",
            "HttpVerb{tags=[tag1, tag2], type='typeA', summary='summaryB', operationId='operationC', consumes=[consumesD, consumesE], produces=[producesF, producesG], responses=[], parameters=[]}"},
            {"tag11,tag22", "typeAA", "summaryBB", "operationCC", "consumesDD,consumesEE",
                "producesFF,producesGG",
                "HttpVerb{tags=[tag11, tag22], type='typeAA', summary='summaryBB', operationId='operationCC', consumes=[consumesDD, consumesEE], produces=[producesFF, producesGG], responses=[], parameters=[]}"}};
        return (Arrays.asList(inputs));
    }

    /**
     * Constructor for the test cases all following same pattern.
     */
    public ApiHttpVerbTest(String tags, String type, String summary, String operationId,
        String consumes, String produces, String result) {
        super();
        this.tags = Arrays.asList(tags.split(","));
        this.type = type;
        this.summary = summary;
        this.operationId = operationId;
        this.consumes = Arrays.asList(consumes.split(","));
        this.produces = Arrays.asList(produces.split(","));
        this.result = result;

    }

    /**
     * Initialise the test object.
     */
    @Before
    public void setUp() throws Exception {
        theVerb = new Api.HttpVerb();
    }

    /**
     * Perform the test on the test object.
     */
    @Test
    public void testApiHttpVerb() {
        theVerb.setTags(this.tags);
        theVerb.setType(this.type);
        theVerb.setSummary(this.summary);
        theVerb.setOperationId(this.operationId);
        theVerb.setConsumes(this.consumes);
        theVerb.setProduces(this.produces);

        // other stuff that can be set but not necessarily
        // included in the toString() output
        theVerb.setConsumerEnabled(true);

        List<Api.HttpVerb.Response> tmpList1 = new ArrayList<Api.HttpVerb.Response>();
        theVerb.setResponses(tmpList1);

        List<Map<String, Object>> tmpList2 = new ArrayList<Map<String, Object>>();
        theVerb.setParameters(tmpList2);

        Map<String, Object> tmpMap1 = new HashMap<String, Object>();
        theVerb.setBodyParameters(tmpMap1);
        theVerb.setBodyParametersEnabled(true);
        theVerb.setParametersEnabled(true);
        theVerb.setSchemaLink("");
        theVerb.setSchemaType("");
        theVerb.setHasReturnSchema(true);
        theVerb.setReturnSchemaLink("");
        theVerb.setReturnSchemaObject("");

        assertThat(theVerb.toString(), is(this.result));
    }

}
