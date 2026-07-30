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
 * A block sequence: each item on its own line behind a {@code -} indicator.
 *
 * <p>
 * Where the indicator sits relative to the sequence's key is a property of the document style, so
 * it
 * belongs to the serializer and not here. These documents indent it one level below the key
 * ({@code tags:}, then two more spaces before the {@code -}), which is incidentally one of the
 * reasons stock snakeyaml cannot reproduce the current bytes: it places the indicator at the
 * parent's
 * indentation and will not indent it further unless the mapping indent grows too.
 *
 * @see YamlSerializer
 */
public final class YamlSequence implements YamlNode {

    private final List<YamlNode> items = new ArrayList<>();

    /** A scalar item, the common case: {@code - application/json}. */
    public YamlSequence item(String value) {
        items.add(new YamlScalar(value));
        return this;
    }

    /** A structured item - a parameter, say, whose own keys hang off the indicator line. */
    public YamlSequence item(YamlNode node) {
        items.add(node);
        return this;
    }

    /**
     * An empty line between items - see {@link YamlRaw#BLANK_LINE}. It does not count towards
     * {@link #isEmpty()}, so a sequence of nothing but blank lines still reports itself empty and
     * its
     * key is left out.
     */
    public YamlSequence blankLine() {
        items.add(YamlRaw.BLANK_LINE);
        return this;
    }

    public List<YamlNode> items() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(item -> item == YamlRaw.BLANK_LINE);
    }
}
