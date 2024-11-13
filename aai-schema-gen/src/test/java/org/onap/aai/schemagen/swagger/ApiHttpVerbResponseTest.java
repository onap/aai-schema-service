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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ApiHttpVerbResponseTest {
    Api.HttpVerb.Response theResponse = null;
    String responseCode;
    String description;
    String result;

    /**
     * Parameters for the test cases all following same pattern.
     */
    public static Collection<String[]> testConditions() {
        String inputs[][] = {{"200", "OK", "Response{responseCode='200', description='OK'}"},
            {"400", "Bad Request", "Response{responseCode='400', description='Bad Request'}"},
            {"500", "Internal Server Error",
                "Response{responseCode='500', description='Internal Server Error'}"},
            {"fake", "random message",
                "Response{responseCode='fake', description='random message'}"}};
        return (Arrays.asList(inputs));
    }

    /**
     * Constructor for the test cases all following same pattern.
     */
    public void initApiHttpVerbResponseTest(String responseCode, String description, String result) {
        this.responseCode = responseCode;
        this.description = description;
        this.result = result;
    }

    /**
     * Initialise the test object.
     */
    @BeforeEach
    public void setUp() throws Exception {
        theResponse = new Api.HttpVerb.Response();
    }

    /**
     * Perform the test on the test object.
     */
    @MethodSource("testConditions")
    @ParameterizedTest
    public void testApiHttpVerbResponse(String responseCode, String description, String result) {
        initApiHttpVerbResponseTest(responseCode, description, result);
        theResponse.setResponseCode(this.responseCode);
        theResponse.setDescription(this.description);
        assertThat(theResponse.toString(), is(this.result));
    }

}
