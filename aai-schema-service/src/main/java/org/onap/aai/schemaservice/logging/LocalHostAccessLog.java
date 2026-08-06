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

package org.onap.aai.schemaservice.logging;

import ch.qos.logback.access.common.PatternLayout;
import ch.qos.logback.access.jetty.RequestLogImpl;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.onap.aai.logging.CNName;
import org.onap.aai.logging.DME2RestFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalHostAccessLog {

    /**
     * Declaring the web server factory explicitly also decides the servlet container: both
     * spring-boot-starter-jetty and (transitively via starter-jersey) starter-tomcat are on the
     * classpath, and Tomcat's auto-configuration is imported first. Removing this bean would
     * silently move the service to Tomcat and drop the access log with it.
     */
    @Bean
    public AbstractServletWebServerFactory jettyConfigBean(
        @Value("${jetty.threadPool.maxThreads:200}") final String maxThreads,
        @Value("${jetty.threadPool.minThreads:8}") final String minThreads) {

        registerAccessLogConverters();

        JettyServletWebServerFactory jef = new JettyServletWebServerFactory();
        jef.addServerCustomizers((JettyServerCustomizer) server -> {

            RequestLogImpl requestLogImpl = new RequestLogImpl();
            requestLogImpl.setResource("/localhost-access-logback.xml");
            requestLogImpl.start();

            // Jetty 12 removed HandlerCollection/RequestLogHandler; the request log is now set
            // on the server itself instead of being wrapped around the handler chain.
            server.setRequestLog(requestLogImpl);

            final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
            threadPool.setMaxThreads(Integer.valueOf(maxThreads));
            threadPool.setMinThreads(Integer.valueOf(minThreads));
        });
        return jef;
    }

    /**
     * Registers AAI's %z (client cert CN) and %y (REST/DME2 flag) access-log converters.
     *
     * <p>aai-common's {@code CustomLogPatternLayout} declares them by overriding the *instance*
     * method {@code getDefaultConverterSupplierMap()}, but {@code PatternLayoutEncoder.start()}
     * replaces the layout with a plain {@link PatternLayout}, so that override never takes effect
     * and the pattern renders as {@code %PARSER_ERROR[z]}. Registering into the static map that the
     * plain layout consults is what actually makes the converters resolve, and it keeps working when
     * the OOM chart mounts its own localhost-access-logback.xml over ours.
     */
    private static void registerAccessLogConverters() {
        PatternLayout.ACCESS_DEFAULT_CONVERTER_SUPPLIER_MAP.putIfAbsent("z", CNName::new);
        PatternLayout.ACCESS_DEFAULT_CONVERTER_SUPPLIER_MAP.putIfAbsent("y", DME2RestFlag::new);
    }
}
