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
package org.onap.aai.schemaservice.nodeschema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.aai.schemaservice.nodeschema.validation.AAISchemaValidationException;
import static org.junit.jupiter.api.Assertions.*;

class SchemaVersionTest {

    private SchemaVersion version1;
    private SchemaVersion version2;
    private SchemaVersion version3;
    private SchemaVersion versionNull;

    @BeforeEach
    void setUp() {
        version1 = new SchemaVersion("v1");
        version2 = new SchemaVersion("v2");
        version3 = new SchemaVersion("v10");
        versionNull = null;
    }

    @Test
    void testConstructorValidVersion() {
        assertDoesNotThrow(() -> new SchemaVersion("v1"));
        assertDoesNotThrow(() -> new SchemaVersion("v100"));
    }

    @Test
    void testConstructorInvalidVersion() {
        assertThrows(AAISchemaValidationException.class, () -> new SchemaVersion("1"));
        assertThrows(AAISchemaValidationException.class, () -> new SchemaVersion("v0"));
        assertThrows(AAISchemaValidationException.class, () -> new SchemaVersion("vabc"));
        assertThrows(AAISchemaValidationException.class, () -> new SchemaVersion("version1"));
    }

    @Test
    void testEquals() {
        SchemaVersion versionA = new SchemaVersion("v1");
        SchemaVersion versionB = new SchemaVersion("v1");
        SchemaVersion versionC = new SchemaVersion("v2");

        assertTrue(versionA.equals(versionA));
        assertEquals(versionA, versionB);
        assertNotEquals(versionA, versionC);
        assertNotEquals(versionA, versionNull);
        assertNotEquals(versionA, "v1");
    }

    @Test
    void testHashCode() {
        SchemaVersion versionA = new SchemaVersion("v1");
        SchemaVersion versionB = new SchemaVersion("v1");
        SchemaVersion versionC = new SchemaVersion("v2");

        assertEquals(versionA.hashCode(), versionB.hashCode());
        assertNotEquals(versionA.hashCode(), versionC.hashCode());
    }

    @Test
    void testToString() {
        SchemaVersion versionA = new SchemaVersion("v1");
        SchemaVersion versionB = new SchemaVersion("v10");

        assertEquals("v1", versionA.toString());
        assertEquals("v10", versionB.toString());
    }

    @Test
    void testCompareTo() {
        assertTrue(version1.compareTo(version2) < 0);
        assertTrue(version2.compareTo(version1) > 0);
        assertTrue(version1.compareTo(version1) == 0);
        assertTrue(version2.compareTo(version3) < 0);
    }

    @Test
    void testCompareToWithNull() {
        assertEquals(-1, version1.compareTo(null));
    }
}
