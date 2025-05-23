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

import java.io.IOException;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class SchemaServiceTestConfiguration {

    private static final Logger logger =
        LoggerFactory.getLogger(SchemaServiceTestConfiguration.class);

    @Autowired
    private Environment env;

    /**
     * Create a RestTemplate bean, using the RestTemplateBuilder provided
     * by the auto-configuration.
     */
    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) throws Exception {

        RestTemplate restTemplate = null;

        if (env.acceptsProfiles(Profiles.of("one-way-ssl", "two-way-ssl"))) {
            SSLContext sslContext = SSLContextBuilder.create().build();

            HttpClient client = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier((s, sslSession) -> true)
                .build();

            restTemplate = builder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(client)).build();
        } else {
            restTemplate = builder.build();
        }

        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
                if (clientHttpResponse.getStatusCode() != HttpStatus.OK) {

                    logger.debug("Status code: " + clientHttpResponse.getStatusCode());

                    if (clientHttpResponse.getStatusCode() == HttpStatus.FORBIDDEN) {
                        logger.debug("Call returned a error 403 forbidden resposne ");
                        return true;
                    }

                    if (clientHttpResponse.getRawStatusCode() % 100 == 5) {
                        logger.debug("Call returned a error " + clientHttpResponse.getStatusText());
                        return true;
                    }
                }

                return false;
            }

            @Override
            public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
            }
        });

        return restTemplate;
    }
}
