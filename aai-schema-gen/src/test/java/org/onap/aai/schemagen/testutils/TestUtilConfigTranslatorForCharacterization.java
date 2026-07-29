/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-18 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aai.schemagen.testutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.onap.aai.setup.AAIConfigTranslator;
import org.onap.aai.setup.SchemaConfigVersions;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.SchemaVersion;

/**
 * Config translator for the swagger-generation characterization test. Unlike
 * {@link TestUtilConfigTranslatorforBusiness} (which maps the whole business+network fixture set to
 * the default version, whose namespace does not match), this maps the ~92-type v13 OXM fixture set
 * to {@code v13} so the OXM namespace ({@code inventory.aai.onap.org.v13}) matches the schema
 * version being generated. That much larger input exercises the deep-nesting, combineElements,
 * ArrayList-container and relationship code paths in {@code processJavaTypeElementSwagger} that the
 * 6-type v11 golden test never reaches.
 */
public class TestUtilConfigTranslatorForCharacterization extends AAIConfigTranslator {

    public TestUtilConfigTranslatorForCharacterization(SchemaLocationsBean bean,
        SchemaConfigVersions schemaConfigVersions) {
        super(bean, schemaConfigVersions);
    }

    @Override
    public Map<SchemaVersion, List<String>> getNodeFiles() {
        List<String> files13 = new ArrayList<>();
        // synthetic top-level Inventory root so path generation has an entry point (the other
        // fixtures define only namespace sub-trees, no Inventory)
        files13.add("src/test/resources/oxm/inventory_oxm_v13.xml");
        files13.add("src/test/resources/oxm/business_oxm_v13.xml");
        files13.add("src/test/resources/oxm/common_oxm_v13.xml");
        files13.add("src/test/resources/oxm/serviceDesign_oxm_v13.xml");
        files13.add("src/test/resources/oxm/network_oxm_v13.xml");

        Map<SchemaVersion, List<String>> input = new TreeMap<>();
        input.put(new SchemaVersion("v13"), files13);
        return input;
    }

    @Override
    public Map<SchemaVersion, List<String>> getEdgeFiles() {
        List<String> files = new ArrayList<>();
        files.add("src/test/resources/dbedgerules/DbEdgerules_one.json");
        Map<SchemaVersion, List<String>> input = new TreeMap<>();
        input.put(new SchemaVersion("v13"), files);
        return input;
    }
}
