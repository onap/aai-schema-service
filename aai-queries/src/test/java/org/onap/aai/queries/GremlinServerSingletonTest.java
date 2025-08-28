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
package org.onap.aai.queries;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import lombok.SneakyThrows;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GremlinServerSingletonTest {

    private GremlinServerSingleton gremlinServerSingleton;
    private GetCustomQueryConfig mockQueryConfig;
    private CustomQueryConfig mockCustomQueryConfig;
    private Path storedQueriesFilePath;  // To store the dynamic path of the temporary file
    private static final String TEST_KEY = "testKey";
    private static final String TEST_QUERY = "MATCH (n) RETURN n";

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        // Create a temporary directory to store test files
        Path tempDir = Files.createTempDirectory("testDir");
        storedQueriesFilePath = tempDir.resolve("stored-queries.json");

        // Create the necessary directories
        Files.createDirectories(storedQueriesFilePath.getParent());

        // Write the mock query data to the temporary file
        FileWriter fileWriter = new FileWriter(storedQueriesFilePath.toFile());
        fileWriter.write("{\"query\": \"" + TEST_QUERY + "\"}");
        fileWriter.close();

        // Mock dependencies
        mockQueryConfig = mock(GetCustomQueryConfig.class);
        mockCustomQueryConfig = mock(CustomQueryConfig.class);

        when(mockQueryConfig.getStoredQuery(TEST_KEY)).thenReturn(mockCustomQueryConfig);
        when(mockCustomQueryConfig.getQuery()).thenReturn(TEST_QUERY);

        // Spy on GremlinServerSingleton to mock the initialization
        GremlinServerSingleton spyGremlinServerSingleton = spy(new GremlinServerSingleton());

        // Inject the mock using reflection
        setPrivateField(spyGremlinServerSingleton, "queryConfig", mockQueryConfig);

        // Use the spy instance
        gremlinServerSingleton = spyGremlinServerSingleton;
    }


    @Test
    public void testGetStoredQueryFromConfig_QueryExists() {
        // Call the method with a valid key
        String query = gremlinServerSingleton.getStoredQueryFromConfig(TEST_KEY);

        // Assert that the query is returned as expected
        assertNotNull(query);
        assertEquals(TEST_QUERY, query);
    }

    @Test
    public void testGetCustomQueryConfig_QueryExists() {
        // Call the method with a valid key
        CustomQueryConfig result = gremlinServerSingleton.getCustomQueryConfig(TEST_KEY);

        // Assert that the result is the mock CustomQueryConfig
        assertNotNull(result);
        assertEquals(mockCustomQueryConfig, result);
    }

    @SneakyThrows
    private void setPrivateField(Object target, String fieldName, Object value) {
        Class<?> targetClass = target.getClass();
        Field field = null;

        // Traverse class hierarchy to find the field
        while (targetClass != null) {
            try {
                field = targetClass.getDeclaredField(fieldName);
                break; // Stop if found
            } catch (NoSuchFieldException e) {
                targetClass = targetClass.getSuperclass(); // Move to parent
            }
        }

        if (field != null) {
            field.setAccessible(true);
            field.set(target, value);
        } else {
            throw new NoSuchFieldException("Field " + fieldName + " not found in class hierarchy.");
        }
    }

    // Optionally clean up the temporary files after the test run
    @AfterEach
    public void tearDown() throws IOException {
        // Delete the temporary file and directory
        Files.deleteIfExists(storedQueriesFilePath);
        Files.deleteIfExists(storedQueriesFilePath.getParent());
    }
}
