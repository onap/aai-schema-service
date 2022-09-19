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

package org.onap.aai.schemaservice;

import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.onap.aai.aailog.logs.AaiDebugLog;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.schemaservice.config.PropertyPasswordConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextListener;

@SpringBootApplication
// Component Scan provides a way to look for spring beans
// It only searches beans in the following packages
// Any method annotated with @Bean annotation or any class
// with @Component, @Configuration, @Service will be picked up
@EnableAutoConfiguration(
    exclude = {DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ComponentScan(basePackages = {"org.onap.aai.schemaservice", "org.onap.aai.aaf"})
public class SchemaServiceApp {

    private static final Logger logger = LoggerFactory.getLogger(SchemaServiceApp.class.getName());

    private static final String APP_NAME = "aai-schema-service";
    private static AaiDebugLog debugLog = new AaiDebugLog();
    static {
        debugLog.setupMDC();
    }

    @Autowired
    private Environment env;

    public static void main(String[] args) throws AAIException {

        setDefaultProps();

        SpringApplication app = new SpringApplication(SchemaServiceApp.class);
        app.setLogStartupInfo(false);
        app.setRegisterShutdownHook(true);
        app.addInitializers(new PropertyPasswordConfiguration());

        Environment env = app.run(args).getEnvironment();

        logger.debug("Application '{}' is running on {}!",
            env.getProperty("spring.application.name"), env.getProperty("server.port"));

        logger.debug("SchemaService MicroService Started");

        System.out.println("SchemaService Microservice Started");

    }

    public static void setDefaultProps() {

        if (System.getProperty("file.separator") == null) {
            System.setProperty("file.separator", "/");
        }

        String currentDirectory = System.getProperty("user.dir");

        if (System.getProperty("AJSC_HOME") == null) {
            System.setProperty("AJSC_HOME", ".");
        }

        if (currentDirectory.contains(APP_NAME)) {
            if (System.getProperty("BUNDLECONFIG_DIR") == null) {
                System.setProperty("BUNDLECONFIG_DIR", "src/main/resources");
            }
        } else {
            if (System.getProperty("BUNDLECONFIG_DIR") == null) {
                System.setProperty("BUNDLECONFIG_DIR", "aai-schema-service/src/main/resources");
            }
        }
    }

    @PostConstruct
    private void init() throws AAIException {
        System.setProperty("org.onap.aai.serverStarted", "false");
        setDefaultProps();

        logger.debug("SchemaService initialization started...");

        // Setting this property to allow for encoded slash (/) in the path parameter
        // This is only needed for tomcat keeping this as temporary
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");

        if (env.acceptsProfiles(Profiles.TWO_WAY_SSL)
            && env.acceptsProfiles(Profiles.ONE_WAY_SSL)) {
            logger.warn("You have seriously misconfigured your application");
        }

    }

    @PreDestroy
    public void cleanup() {

        logger.debug("SchemaService shutting down");
    }
}
