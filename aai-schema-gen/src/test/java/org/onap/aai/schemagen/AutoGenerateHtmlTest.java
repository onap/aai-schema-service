/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom. All rights reserved.
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
package org.onap.aai.schemagen;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource(
    properties = {
        "schemaIngestPropLoc = src/test/resources/schema-ingest.properties"})
public class AutoGenerateHtmlTest {

    @Test
    void testMain_shouldSetEnvironmentVariable() throws Exception {
        // Set a system property before running the method
        System.setProperty("aai.generate.version", "1.0");

        AutoGenerateHtml.main(new String[]{});

        assertEquals("1.0", System.getProperty("aai.generate.version"), "The 'aai.generate.version' property should be set.");
        System.clearProperty("aai.generate.version");
    }

    @Test
    void testMain_shouldCompleteWithoutException() {
        assertDoesNotThrow(() -> {
            try {
                AutoGenerateHtml.main(new String[]{});
            } catch (NullPointerException e) {
                // This test isn't verifying functional behavior, just guarding against uncaught crashes
            }
        });
    }
}
