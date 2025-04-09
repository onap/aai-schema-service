/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom. All rights reserved.
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
package org.onap.aai.schemaservice.service;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.onap.aai.util.AAIConstants;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class AuthorizationServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Spy
    private AuthorizationService authorizationService;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        Map<String, String> mockAuthorizedUsers = new HashMap<>();
        mockAuthorizedUsers.put(Base64.getEncoder().encodeToString("john:secret123".getBytes(StandardCharsets.UTF_8)), "admin");
        mockAuthorizedUsers.put(Base64.getEncoder().encodeToString("jane:password456".getBytes(StandardCharsets.UTF_8)), "user");

        java.lang.reflect.Field field = AuthorizationService.class.getDeclaredField("authorizedUsers");
        field.setAccessible(true);
        field.set(authorizationService, mockAuthorizedUsers);
    }

    @Test
    public void testCheckIfUserAuthorized_authorizedUser() {
        String validAuthHeader = Base64.getEncoder().encodeToString("john:secret123".getBytes(StandardCharsets.UTF_8));
        assertTrue(authorizationService.checkIfUserAuthorized(validAuthHeader));
    }

    @Test
    public void testCheckIfUserAuthorized_unauthorizedUser() {
        String validAuthHeader = Base64.getEncoder().encodeToString("jane:password456".getBytes(StandardCharsets.UTF_8));
        assertFalse(authorizationService.checkIfUserAuthorized(validAuthHeader));
    }

    @Test
    public void testCheckIfUserAuthorized_invalidAuthorizationHeader() {
        String invalidAuthHeader = "InvalidAuthString";
        assertFalse(authorizationService.checkIfUserAuthorized(invalidAuthHeader));
    }

    @Test
    public void testCheckIfUserAuthorized_missingAuthorizationHeader() {
        String emptyAuthHeader = "";
        assertFalse(authorizationService.checkIfUserAuthorized(emptyAuthHeader));
    }

    @Test
    public void testGetBasicAuthFilePath() {
        String expectedPath = AAIConstants.AAI_HOME_ETC_AUTH + AAIConstants.AAI_FILESEP + "realm.properties";
        assertEquals(expectedPath, authorizationService.getBasicAuthFilePath());
    }

    @Test
    public void testInit_invalidFormat() {
        String invalidEntry = "john:secret123,admin:extra";
        Path mockFile = createMockFile(invalidEntry);

        authorizationService = new AuthorizationService() {
            @Override
            public String getBasicAuthFilePath() {
                return mockFile.toString();
            }
        };

        try {
            authorizationService.init();
            fail("Expected RuntimeException due to invalid format in realm.properties");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("This username / pwd is not a valid entry in realm.properties"));
        }
    }

    @Test
    public void testInit_withObfuscatedPassword() {
        String validObfuscatedPassword = "OBF:U0002U0004U0005U0006";
        String obfuscatedEntry = "john:" + validObfuscatedPassword + ",admin";
        Path mockFile = createMockFile(obfuscatedEntry);

        authorizationService = new AuthorizationService() {
            @Override
            public String getBasicAuthFilePath() {
                return mockFile.toString();
            }
        };

        authorizationService.init();
        String decodedPassword = "secret123";
        String validAuthHeader = Arrays.toString(Base64.getEncoder().encodeToString(("john:" + decodedPassword).getBytes()).getBytes(StandardCharsets.UTF_8));
        assertFalse(authorizationService.checkIfUserAuthorized(validAuthHeader));
    }

    @Test
    public void testInit_fileNotFound() {
        authorizationService = new AuthorizationService() {
            @Override
            public String getBasicAuthFilePath() {
                return "src/test/resources/non_existent_file.properties";
            }
        };

        thrown.expect(IOException.class);
        thrown.expectMessage("IO Exception occurred during the reading of realm.properties");

        authorizationService.init();
    }

    private Path createMockFile(String content) {
        try {
            Path tempFile = Files.createTempFile("realm", ".properties");
            Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Error creating mock file", e);
        }
    }
}
