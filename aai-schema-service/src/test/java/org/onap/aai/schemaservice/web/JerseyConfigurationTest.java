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

package org.onap.aai.schemaservice.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;

import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.onap.aai.schemaservice.Profiles;
import org.springframework.mock.env.MockEnvironment;

class JerseyConfigurationTest {

    private static final String REQUEST = ContainerRequestFilter.class.getSimpleName();
    private static final String RESPONSE = ContainerResponseFilter.class.getSimpleName();

    @Test
    void registersFiltersWithTheirPriorities() {
        assertEquals(defaultFilters(), registeredFilters(new MockEnvironment()));
    }

    @Test
    void registersOneWaySslAuthorizationOnlyWithItsProfile() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles(Profiles.ONE_WAY_SSL);
        Map<String, String> expected = defaultFilters();
        expected.put("org.onap.aai.schemaservice.interceptors.pre.OneWaySslAuthorization",
            REQUEST + "=4500");

        assertEquals(expected, registeredFilters(env));
    }

    private static Map<String, String> defaultFilters() {
        String pre = "org.onap.aai.schemaservice.interceptors.pre.";
        String post = "org.onap.aai.schemaservice.interceptors.post.";
        Map<String, String> filters = new TreeMap<>();
        filters.put("org.onap.aai.aailog.filter.AaiAuditLogContainerFilter",
            REQUEST + "=1, " + RESPONSE + "=1");
        filters.put(pre + "RequestTransactionLogging", REQUEST + "=1000");
        filters.put(pre + "HeaderValidation", REQUEST + "=2000");
        filters.put(pre + "HttpHeaderInterceptor", REQUEST + "=4000");
        filters.put(pre + "RequestHeaderManipulation", REQUEST + "=6000");
        filters.put(post + "InvalidResponseStatus", RESPONSE + "=1000");
        filters.put(post + "ResponseTransactionLogging", RESPONSE + "=3000");
        filters.put(post + "ResponseHeaderManipulation", RESPONSE + "=4000");
        return filters;
    }

    private static Map<String, String> registeredFilters(MockEnvironment env) {
        JerseyConfiguration config = new JerseyConfiguration(env);
        Map<String, String> filters = new TreeMap<>();
        for (Class<?> clazz : config.getClasses()) {
            Map<String, Integer> contracts = new TreeMap<>();
            config.getConfiguration().getContracts(clazz).forEach((contract, priority) -> {
                if (contract == ContainerRequestFilter.class
                    || contract == ContainerResponseFilter.class) {
                    contracts.put(contract.getSimpleName(), priority);
                }
            });
            if (!contracts.isEmpty()) {
                filters.put(clazz.getName(), contracts.toString().replaceAll("[{}]", ""));
            }
        }
        return filters;
    }
}
