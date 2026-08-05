/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2026 Deutsche Telekom.
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

package org.onap.aai.schemagen.genxsd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Compares generated output against a committed golden file. To (re)generate a golden after an
 * intentional output change, delete it and run the test once: the current output is written to
 * {@code target/characterization/} and the test fails with instructions to review and commit it.
 */
final class GoldenFile {

    private static final Path GOLDEN_DIR = Path.of("src/test/resources/characterization");
    private static final Path ACTUAL_DIR = Path.of("target/characterization");

    private GoldenFile() {
    }

    static void assertMatches(String goldenName, String actual) throws IOException {
        assertFalse(actual == null || actual.isBlank(),
            "generation produced empty output - the OXM version/namespace wiring is probably wrong");

        Path golden = GOLDEN_DIR.resolve(goldenName);
        Path actualOut = ACTUAL_DIR.resolve(goldenName);
        if (!Files.exists(golden)) {
            write(actualOut, actual);
            fail("No golden file at " + golden + ". Wrote current output to " + actualOut
                + " - review it and, if correct, commit it as the golden file.");
        }

        String expected = Files.readString(golden, StandardCharsets.UTF_8);
        if (!expected.equals(actual)) {
            write(actualOut, actual);
            assertEquals(expected, actual, "Generated output differs from the golden file " + golden
                + ". If the change is intentional, replace it with " + actualOut + ".");
        }
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }
}
