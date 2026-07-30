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
 * A scalar value, emitted exactly as given.
 *
 * <p>
 * Nothing here quotes or escapes. The generated documents quote some values and not others -
 * {@code $ref: "#/definitions/x"} but {@code type: string} - and which is which is part of the byte
 * contract this refactoring preserves, so the caller supplies the value in the form the document
 * needs. A quoting policy belongs with the OpenAPI 3 emitter, where the output is allowed to change.
 *
 * <p>
 * Trailing whitespace is not modelled here. The documents do contain lines that end in blanks, but
 * they are of two kinds and neither is a property of a scalar: some arrive inside a description that
 * the OXM itself spells with a trailing space, and the rest are on a key line, where
 * {@link YamlMapping#entryWithPaddedKey} puts them.
 *
 * @param value the text of the scalar
 */
public record YamlScalar(String value) implements YamlNode {

    @Override
    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }
}
