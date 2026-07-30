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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A literal block scalar - the {@code |} form - whose content is a list of lines.
 *
 * <p>
 * The long prose in this schema is markdown: the PATCH description, the "valid relationships"
 * notes,
 * the related-nodes lists. It has to survive with its own line breaks intact, which is what the
 * literal block is for, and the serializer indents each content line one level below the key that
 * introduces it.
 */
public final class YamlBlock implements YamlNode {

    private final List<String> lines = new ArrayList<>();
    private String trailingRaw;

    public YamlBlock line(String line) {
        lines.add(line);
        return this;
    }

    /**
     * Appends already-indented text after the content lines, emitted verbatim.
     *
     * <p>
     * Used for the related-nodes description, which the edge-rule side renders as a whole block
     * with
     * its own indentation baked in. Splitting that string back into lines so the serializer could
     * re-indent them would be a second parse of text this generator itself produced, and any
     * mismatch would show up as a corrupted document; passing it through is the honest option until
     * that emitter also builds nodes.
     */
    public YamlBlock trailingRaw(String raw) {
        this.trailingRaw = raw;
        return this;
    }

    public List<String> lines() {
        return Collections.unmodifiableList(lines);
    }

    public String trailing() {
        return trailingRaw;
    }

    @Override
    public boolean isEmpty() {
        return lines.isEmpty() && (trailingRaw == null || trailingRaw.isEmpty());
    }
}
