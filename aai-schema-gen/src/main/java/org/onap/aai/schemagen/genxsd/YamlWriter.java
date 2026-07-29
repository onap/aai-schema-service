/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom.
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

/**
 * Appends YAML lines at a nesting level instead of at a hand-counted number of spaces.
 *
 * <p>
 * The generators emit swagger by string concatenation, and every line used to carry its own indent
 * literal - {@code "        - application/json\n"}. Counting those spaces correctly is on the
 * author, a miscount is invisible in the source, and it shows up as a malformed document. Here the
 * caller states the level and this class owns the arithmetic: one level is two spaces, the depth
 * the swagger documents use throughout.
 *
 * <p>
 * Levels are absolute, not relative to a cursor, because the emitters are not written as a
 * traversal - an operation class emits its lines knowing statically where in the document they sit.
 * A level therefore reads as a fact about the output rather than as writer state, and each method
 * corresponds to one YAML construct:
 *
 * <pre>
 *   key(1, "/business/customers")      ->  "  /business/customers:"
 *   key(2, "get")                      ->  "    get:"
 *   key(3, "tags")                     ->  "      tags:"
 *   item(4, "Business")                ->  "        - Business"
 *   entry(3, "summary", "returns ...") ->  "      summary: returns ..."
 * </pre>
 *
 * <p>
 * Nothing here escapes or quotes: callers pass values that are already in the form the document
 * needs, which is what keeps this a drop-in for the concatenation it replaces.
 */
public final class YamlWriter {

    /** One nesting level. The swagger documents are indented two spaces per level throughout. */
    private static final String INDENT_UNIT = "  ";

    private final StringBuilder sb;

    public YamlWriter() {
        this(new StringBuilder());
    }

    /**
     * Writes into an existing buffer, for the emitters that hand their buffer around or interleave
     * with code that still appends directly.
     */
    public YamlWriter(StringBuilder sb) {
        this.sb = sb;
    }

    /** A key that opens a nested block: {@code name:}. */
    public YamlWriter key(int level, String name) {
        return indent(level).append(name).append(":").newline();
    }

    /** A key with a scalar value on the same line: {@code name: value}. */
    public YamlWriter entry(int level, String name, String value) {
        return indent(level).append(name).append(": ").append(value).newline();
    }

    /** A sequence entry: {@code - value}. */
    public YamlWriter item(int level, String value) {
        return indent(level).append("- ").append(value).newline();
    }

    /**
     * A key introducing a literal block scalar: {@code name: |}. The lines that follow are the
     * block's content and are emitted with {@link #text(int, String)} one level deeper.
     */
    public YamlWriter blockScalar(int level, String name) {
        return indent(level).append(name).append(": |").newline();
    }

    /**
     * A line of literal content at the given level: the body of a block scalar - markdown, mostly -
     * or, occasionally, a line whose exact shape no YAML construct here produces.
     */
    public YamlWriter text(int level, String value) {
        return indent(level).append(value).newline();
    }

    /**
     * A pre-rendered fragment placed at the given level. The fragment supplies its own line ending;
     * this is for the values that arrive already formatted, such as the shared responses block.
     */
    public YamlWriter fragment(int level, String preRendered) {
        return indent(level).append(preRendered);
    }

    /**
     * A pre-rendered block appended verbatim, for the fragments that already carry their own
     * indentation - a parameter list built up by another emitter, most often.
     */
    public YamlWriter raw(String preRendered) {
        return append(preRendered);
    }

    /** An empty line. Only where the current output has one; YAML itself does not need it. */
    public YamlWriter blankLine() {
        return newline();
    }

    /** True when nothing has been written yet. */
    public boolean isEmpty() {
        return sb.length() == 0;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    private YamlWriter indent(int level) {
        for (int i = 0; i < level; ++i) {
            sb.append(INDENT_UNIT);
        }
        return this;
    }

    private YamlWriter append(String s) {
        sb.append(s);
        return this;
    }

    private YamlWriter newline() {
        sb.append("\n");
        return this;
    }
}
