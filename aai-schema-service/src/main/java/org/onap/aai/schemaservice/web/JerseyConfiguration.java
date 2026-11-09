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

package org.onap.aai.schemaservice.web;

import jakarta.annotation.Priority;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;
import org.onap.aai.aailog.filter.AaiAuditLogContainerFilter;
import org.onap.aai.schemaservice.edges.EdgeResource;
import org.onap.aai.schemaservice.healthcheck.EchoResource;
import org.onap.aai.schemaservice.interceptors.post.InvalidResponseStatus;
import org.onap.aai.schemaservice.interceptors.post.ResponseHeaderManipulation;
import org.onap.aai.schemaservice.interceptors.post.ResponseTransactionLogging;
import org.onap.aai.schemaservice.interceptors.pre.HeaderValidation;
import org.onap.aai.schemaservice.interceptors.pre.HttpHeaderInterceptor;
import org.onap.aai.schemaservice.interceptors.pre.OneWaySslAuthorization;
import org.onap.aai.schemaservice.interceptors.pre.RequestHeaderManipulation;
import org.onap.aai.schemaservice.interceptors.pre.RequestTransactionLogging;
import org.onap.aai.schemaservice.nodeschema.NodeSchemaChecksumResource;
import org.onap.aai.schemaservice.nodeschema.NodeSchemaResource;
import org.onap.aai.schemaservice.query.QueryResource;
import org.onap.aai.schemaservice.versions.VersionResource;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfiguration extends ResourceConfig {

    private static final Logger log = Logger.getLogger(JerseyConfiguration.class.getName());

    private Environment env;

    public JerseyConfiguration(Environment env) {

        this.env = env;

        register(VersionResource.class);
        register(EchoResource.class);
        register(NodeSchemaResource.class);
        register(QueryResource.class);
        register(EdgeResource.class);
        register(NodeSchemaChecksumResource.class);

        registerFilters();
    }

    private void registerFilters() {
        List<Class<?>> filters =
            List.of(AaiAuditLogContainerFilter.class, RequestTransactionLogging.class,
                HeaderValidation.class, OneWaySslAuthorization.class, HttpHeaderInterceptor.class,
                RequestHeaderManipulation.class, InvalidResponseStatus.class,
                ResponseTransactionLogging.class, ResponseHeaderManipulation.class);

        for (Class<?> filterClass : filters) {
            if (filterClass.getAnnotation(Priority.class) == null) {
                throw new RuntimeException("Container filter " + filterClass.getName()
                    + " does not have @Priority annotation");
            }
        }

        filters.stream().filter(f -> {
            if (f.isAnnotationPresent(Profile.class)
                && !env.acceptsProfiles(Profiles.of(f.getAnnotation(Profile.class).value()))) {
                return false;
            }
            return true;
        }).sorted(Comparator.comparingInt(f -> f.getAnnotation(Priority.class).value()))
            .forEach(this::register);
    }

}
