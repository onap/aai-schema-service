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

import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.onap.aai.schemagen.swagger.GenerateSwagger;
import org.onap.aai.setup.SchemaConfigVersions;
import org.onap.aai.setup.SchemaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AutoGenerateHtml {

    private static Logger logger = LoggerFactory.getLogger(AutoGenerateHtml.class);
    private static final String AAI_GENERATE_VERSION = "aai.generate.version";
    public static final String DEFAULT_SCHEMA_DIR = "../aai-schema";
    // if the program is run from aai-common, use this directory as default"
    public static final String ALT_SCHEMA_DIR = "aai-schema";
    // used to check to see if program is run from aai-schema-gen
    public static final String DEFAULT_RUN_DIR = "aai-schema-gen";

    public static void main(String[] args) throws IOException, TemplateException {
        String savedProperty = System.getProperty(AAI_GENERATE_VERSION);

        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
            "org.onap.aai.setup", "org.onap.aai.schemagen")) {

            SchemaConfigVersions schemaConfigVersions = ctx.getBean(SchemaConfigVersions.class);

            List<SchemaVersion> versionsToGen = schemaConfigVersions.getVersions();
            Collections.sort(versionsToGen);
            Collections.reverse(versionsToGen);
            ListIterator<SchemaVersion> versionIterator = versionsToGen.listIterator();
            String schemaDir;
            if (System.getProperty("user.dir") != null
                && !System.getProperty("user.dir").contains(DEFAULT_RUN_DIR)) {
                schemaDir = ALT_SCHEMA_DIR;
            } else {
                schemaDir = DEFAULT_SCHEMA_DIR;
            }
            String release = System.getProperty("aai.release", "onap");
            while (versionIterator.hasNext()) {
                System.setProperty(AAI_GENERATE_VERSION, versionIterator.next().toString());
                String yamlFile =
                    schemaDir + "/src/main/resources/" + release + "/aai_swagger_yaml/aai_swagger_"
                        + System.getProperty(AAI_GENERATE_VERSION) + ".yaml";
                File swaggerYamlFile = new File(yamlFile);
                if (swaggerYamlFile.exists()) {
                    GenerateSwagger.schemaConfigVersions = schemaConfigVersions;
                    GenerateSwagger.main(args);
                }
            }
        } catch (BeansException e) {
            logger.warn("Unable to initialize AnnotationConfigApplicationContext ", e);
        }

        System.setProperty(AAI_GENERATE_VERSION, savedProperty);
    }
}
