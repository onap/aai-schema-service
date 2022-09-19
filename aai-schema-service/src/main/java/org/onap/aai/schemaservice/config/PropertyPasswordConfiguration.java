/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.schemaservice.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

public class PropertyPasswordConfiguration
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Pattern decodePasswordPattern = Pattern.compile("password\\((.*?)\\)");
    private PasswordDecoder passwordDecoder = new JettyPasswordDecoder();
    private static final Logger logger =
        LoggerFactory.getLogger(PropertyPasswordConfiguration.class.getName());

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String certPath = environment.getProperty("server.certs.location");
        Map<String, Object> sslProps = new LinkedHashMap<>();

        // Override the passwords from application.properties if we find AAF certman files
        if (certPath != null) {
            File passwordFile = new File(certPath + ".password");
            try (InputStream passwordStream = new FileInputStream(passwordFile)) {
                String keystorePassword = null;

                keystorePassword = IOUtils.toString(passwordStream);
                if (keystorePassword != null) {
                    keystorePassword = keystorePassword.trim();
                    sslProps.put("server.ssl.key-store-password", keystorePassword);
                } else {
                    logger.warn("Keystore password is null in AAF Certman password file");
                }
            } catch (IOException e) {
                logger.warn("Not using AAF Certman password file " + passwordFile.getName() + " e="
                    + e.getMessage());
            }

            File passphrasesFile = new File(certPath + ".passphrases");
            try (InputStream passphrasesStream = new FileInputStream(passphrasesFile)) {
                String truststorePassword = null;
                Properties passphrasesProps = new Properties();
                passphrasesProps.load(passphrasesStream);
                truststorePassword = passphrasesProps.getProperty("cadi_truststore_password");
                if (truststorePassword != null) {
                    truststorePassword = truststorePassword.trim();
                    sslProps.put("server.ssl.trust-store-password", truststorePassword);
                } else {
                    logger.warn("Truststore password is null in AAF Certman passphrases file");
                }
            } catch (IOException e) {
                logger.warn("Not using AAF Certman passphrases file " + passphrasesFile.getName()
                    + " e=" + e.getMessage());
            }
        }
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            Map<String, Object> propertyOverrides = new LinkedHashMap<>();
            decodePasswords(propertySource, propertyOverrides);
            if (!propertyOverrides.isEmpty()) {
                PropertySource<?> decodedProperties =
                    new MapPropertySource("decoded " + propertySource.getName(), propertyOverrides);
                environment.getPropertySources().addBefore(propertySource.getName(),
                    decodedProperties);
            }

        }
        if (!sslProps.isEmpty()) {
            logger.info("Using AAF Certman files");
            PropertySource<?> additionalProperties =
                new MapPropertySource("additionalProperties", sslProps);
            environment.getPropertySources().addFirst(additionalProperties);
        }

    }

    private void decodePasswords(PropertySource<?> source, Map<String, Object> propertyOverrides) {
        if (source instanceof EnumerablePropertySource) {
            EnumerablePropertySource<?> enumerablePropertySource =
                (EnumerablePropertySource<?>) source;
            for (String key : enumerablePropertySource.getPropertyNames()) {
                Object rawValue = source.getProperty(key);
                if (rawValue instanceof String) {
                    String decodedValue = decodePasswordsInString((String) rawValue);
                    propertyOverrides.put(key, decodedValue);
                }
            }
        }
    }

    private String decodePasswordsInString(String input) {
        if (input == null) {
            return null;
        }
        StringBuffer output = new StringBuffer();
        Matcher matcher = decodePasswordPattern.matcher(input);
        while (matcher.find()) {
            String replacement = passwordDecoder.decode(matcher.group(1));
            matcher.appendReplacement(output, replacement);
        }
        matcher.appendTail(output);
        return output.toString();
    }

}
