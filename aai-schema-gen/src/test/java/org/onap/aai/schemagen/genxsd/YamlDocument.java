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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * A generated document as snakeyaml parses it, which is how the downstream HTML generation reads it
 * too - so asserting against this both states the structure and proves the output still parses.
 */
final class YamlDocument {

    private final Map<String, Object> root;

    private YamlDocument(Map<String, Object> root) {
        this.root = root;
    }

    static YamlDocument parse(String yaml) {
        return new YamlDocument(asMap(new Yaml().load(yaml), "the document"));
    }

    /** The mapping at the given path, or the whole document when the path is empty. */
    Map<String, Object> map(String... path) {
        Map<String, Object> current = root;
        StringBuilder walked = new StringBuilder();
        for (String key : path) {
            walked.append("/").append(key);
            current = asMap(current.get(key), walked.toString());
        }
        return current;
    }

    /** The keys of the mapping at the given path, in document order. */
    List<String> keys(String... path) {
        return List.copyOf(map(path).keySet());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value, String what) {
        assertNotNull(value, "expected a mapping at " + what);
        return (Map<String, Object>) value;
    }
}
