/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright © 2018 IBM.
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

package org.onap.aai.schemagen;

import java.util.HashSet;
import java.util.Set;

import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.setup.AAIConfigTranslator;
import org.onap.aai.setup.SchemaConfigVersions;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.Translator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SchemaConfiguration {

    @Bean
    public EdgeIngestor edgeIngestor(SchemaLocationsBean schemaLocationsBean,
        SchemaConfigVersions schemaConfigVersions) {
        return new EdgeIngestor(configTranslators(schemaLocationsBean, schemaConfigVersions));
    }

    @Bean(name = "nodeIngestor")
    public NodeIngestor nodeIngestor(SchemaLocationsBean schemaLocationsBean,
        SchemaConfigVersions schemaConfigVersions) {
        return new NodeIngestor(configTranslators(schemaLocationsBean, schemaConfigVersions));
    }

    @Bean(name = "configTranslator")
    public Set<Translator> configTranslators(SchemaLocationsBean schemaLocationsBean,
        SchemaConfigVersions schemaConfigVersions) {
        Set<Translator> translators = new HashSet<>();
        AAIConfigTranslator translator =
            new AAIConfigTranslator(schemaLocationsBean, schemaConfigVersions);
        translators.add(translator);
        return translators;
    }
}
