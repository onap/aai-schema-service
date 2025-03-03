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

package org.onap.aai.schemaservice.nodeschema.validation;

import java.util.List;
import java.util.Map.Entry;

import org.onap.aai.schemaservice.config.ConfigTranslator;
import org.onap.aai.schemaservice.nodeschema.SchemaVersion;
import org.springframework.stereotype.Component;

@Component
public class NodeValidator {
    private ConfigTranslator translator;
    private SchemaErrorStrategy strat;
    private DuplicateNodeDefinitionValidationModule dupChecker;

    public NodeValidator(ConfigTranslator translator, SchemaErrorStrategy strategy,
        DuplicateNodeDefinitionValidationModule dupChecker) {
        this.translator = translator;
        this.strat = strategy;
        this.dupChecker = dupChecker;
    }

    public boolean validate() {

        for (Entry<SchemaVersion, List<String>> entry : translator.getNodeFiles().entrySet()) {
            String result = dupChecker.findDuplicates(entry.getValue(), entry.getKey());
            if (!"".equals(result)) {
                strat.notifyOnError(result);
            }
        }
        return strat.isOK();
    }

    public String getErrorMsg() {
        return strat.getErrorMsg();
    }
}
