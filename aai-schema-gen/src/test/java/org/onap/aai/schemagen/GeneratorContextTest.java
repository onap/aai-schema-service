/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2026 Deutsche Telekom. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.schemagen.genxsd.YAMLfromOXM;
import org.onap.aai.setup.SchemaConfigVersions;
import org.onap.aai.setup.SchemaVersion;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

class GeneratorContextTest {

    @Test
    void generatorContextProvidesTheSchemaBeans() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("generator", Map.of(
                    "schema.uri.base.path", "/aai",
                    "schema.version.list", "v32",
                    "schema.version.api.default", "v32",
                    "schema.version.depth.start", "v32",
                    "schema.version.related.link.start", "v32",
                    "schema.version.app.root.start", "v32",
                    "schema.version.namespace.change.start", "v32",
                    "schema.version.edge.label.start", "v32")));
            // the same packages GenerateXsd and AutoGenerateHtml scan
            ctx.scan("org.onap.aai.setup", "org.onap.aai.schemagen");
            ctx.refresh();

            SchemaConfigVersions versions = ctx.getBean(SchemaConfigVersions.class);
            assertEquals(List.of(new SchemaVersion("v32")), versions.getVersions());
            assertNotNull(ctx.getBean(NodeIngestor.class).getSchema(new SchemaVersion("v32")));
            assertNotNull(ctx.getBean(YAMLfromOXM.class));
        }
    }
}
