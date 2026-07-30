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
 * A block mapping, in insertion order.
 *
 * <p>
 * Order is part of the byte contract - an operation lists {@code tags} before {@code summary}
 * before
 * {@code operationId} - so this holds a list of entries rather than a {@code Map}. Repeated keys
 * are
 * preserved for the same reason: rejecting them would be a behaviour change, and this phase changes
 * no output.
 */
public final class YamlMapping implements YamlNode {

    /**
     * One entry of the mapping.
     *
     * @param key the key, emitted as given - already quoted where the document quotes it, and
     *        {@code null} for an entry that contributes only pre-rendered text
     * @param value the value node
     * @param levelShift levels added to the value's indentation, normally zero; see
     *        {@link YamlMapping#entryAtShiftedDepth}
     * @param keyPadding spaces appended after the key's colon, normally zero; see
     *        {@link YamlMapping#entryWithPaddedKey}
     */
    public record Entry(String key, YamlNode value, int levelShift, int keyPadding) {

        public Entry(String key, YamlNode value) {
            this(key, value, 0, 0);
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    /** A key with a scalar value on the same line: {@code name: value}. */
    public YamlMapping entry(String key, String value) {
        entries.add(new Entry(key, new YamlScalar(value)));
        return this;
    }

    /** A key whose value is a nested node - a mapping, a sequence or a block scalar. */
    public YamlMapping entry(String key, YamlNode value) {
        entries.add(new Entry(key, value));
        return this;
    }

    /**
     * A key whose value is indented {@code levelShift} levels away from where its nesting alone
     * would
     * put it - deeper for a positive shift, shallower for a negative one.
     *
     * <p>
     * The generated documents indent two constructs unusually, and this is where both are recorded
     * rather than as unexplained numbers at their call sites:
     *
     * <ul>
     * <li><b>+1</b> for the {@code $ref} under a GET response's {@code schema:}, which sits two
     * levels below its key instead of one. Measured on {@code aai_swagger_v32.yaml}: 842 response
     * refs
     * indented four spaces past {@code schema:}, against 1382 body-parameter refs at the regular
     * two.</li>
     * <li><b>-1</b> for the sequence under a property's {@code enum:}, whose {@code -} indicators
     * are
     * aligned with the key rather than indented past it - valid YAML, and the form these documents
     * use.</li>
     * </ul>
     *
     * <p>
     * No parser can tell either apart from the regular form, but this phase is byte-identical, so
     * both are reproduced. Neither should survive into the OpenAPI 3 documents, which are generated
     * fresh.
     */
    public YamlMapping entryAtShiftedDepth(String key, YamlNode value, int levelShift) {
        entries.add(new Entry(key, value, levelShift, 0));
        return this;
    }

    /**
     * A key whose colon is followed by {@code keyPadding} spaces before the line ends.
     *
     * <p>
     * For the {@code items:} key of an array property, the one key in these documents that trails
     * whitespace. YAML ignores it, but this phase is byte-identical, so it is reproduced - as a
     * count
     * attached to the entry that has the anomaly rather than as blanks at the end of a string
     * literal, which any editor that trims on save would silently drop. It should not survive into
     * the OpenAPI 3 documents.
     */
    public YamlMapping entryWithPaddedKey(String key, YamlNode value, int keyPadding) {
        entries.add(new Entry(key, value, 0, keyPadding));
        return this;
    }

    /**
     * Appends pre-rendered text that is not a value of any key - see {@link YamlRaw}. Ordering is
     * preserved along with the real entries, which is what lets a partially converted emitter
     * interleave the two.
     */
    public YamlMapping raw(String preRendered) {
        entries.add(new Entry(null, new YamlRaw(preRendered)));
        return this;
    }

    /**
     * Appends pre-rendered text at this mapping's own indentation, without a line ending - see
     * {@link YamlFragment}.
     *
     * <p>
     * For the one property in these documents whose {@code type:} is written with no value and no
     * newline, so that whatever comes next continues on its line. A key with an empty value would
     * be
     * the natural spelling and would produce different bytes.
     */
    public YamlMapping fragment(String preRendered) {
        entries.add(new Entry(null, new YamlFragment(preRendered)));
        return this;
    }

    /** An empty line between entries - see {@link YamlRaw#BLANK_LINE}. */
    public YamlMapping blankLine() {
        entries.add(new Entry(null, YamlRaw.BLANK_LINE));
        return this;
    }

    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
