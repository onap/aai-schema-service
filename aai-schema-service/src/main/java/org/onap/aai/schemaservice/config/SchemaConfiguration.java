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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.schemaservice.config;

import org.onap.aai.schemaservice.nodeschema.NodeIngestor;
import org.onap.aai.schemaservice.nodeschema.SchemaVersions;
import org.onap.aai.schemaservice.nodeschema.validation.CheckEverythingStrategy;
import org.onap.aai.schemaservice.nodeschema.validation.DefaultDuplicateNodeDefinitionValidationModule;
import org.onap.aai.schemaservice.nodeschema.validation.DuplicateNodeDefinitionValidationModule;
import org.onap.aai.schemaservice.nodeschema.validation.NodeValidator;
import org.onap.aai.schemaservice.nodeschema.validation.SchemaErrorStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchemaConfiguration {

    @Bean(name = "nodeIngestor")
    public NodeIngestor nodeIngestor(ConfigTranslator configTranslator) {
        return new NodeIngestor(configTranslator);
    }

    @Bean(name = "configTranslator")
    public ConfigTranslator configTranslator(SchemaLocationsBean schemaLocationsBean, SchemaVersions schemaVersions) {
        return new AAIConfigTranslator(schemaLocationsBean, schemaVersions);
    }

    @Bean
    public SchemaErrorStrategy schemaErrorStrategy() {
        return new CheckEverythingStrategy();
    }

    @Bean
    public DuplicateNodeDefinitionValidationModule duplicateNodeDefinitionValidationModule() {
        return new DefaultDuplicateNodeDefinitionValidationModule();
    }

    @Bean
    public NodeValidator nodeValidator(
        ConfigTranslator configTranslator,
        SchemaErrorStrategy schemaErrorStrategy,
        DuplicateNodeDefinitionValidationModule duplicateNodeDefinitionValidationModule
    ) {
        return new NodeValidator(configTranslator, schemaErrorStrategy, duplicateNodeDefinitionValidationModule);
    }

    @Bean
    public SchemaLocationsBean schemaLocationsBean() {
        return new SchemaLocationsBean();
    }

    @Bean
    public SchemaVersions schemaVersions() {
        return new SchemaVersions();
    }
}
