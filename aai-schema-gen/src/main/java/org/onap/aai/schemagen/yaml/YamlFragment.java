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

package org.onap.aai.schemagen.yaml;

/**
 * Pre-rendered text placed at this node's own indentation, supplying its own line ending.
 *
 * <p>
 * Unlike {@link YamlRaw} the serializer does indent a fragment - it sits at a definite depth in the
 * tree, it just arrives as text rather than as nodes. The one instance today is the shared responses
 * block from {@code GenerateXsd.getResponsesUrl()}, which is either a URL reference or the sentence
 * "Response codes are uniform across all endpoints", already carrying its {@code description:} key
 * and its newline.
 *
 * <p>
 * Note that value can be the four characters {@code null}: the generator reads it from a system
 * property, and when the property is unset every {@code "default"} response in the document reads
 * {@code null} - which the checked-in documents and the unit-test goldens contain. Nothing here may
 * "fix" that; a null-check would change 25 published documents.
 *
 * @param text the exact characters to emit after this node's indentation
 */
public record YamlFragment(String text) implements YamlNode {

    @Override
    public boolean isEmpty() {
        return text == null || text.isEmpty();
    }
}
