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
 * Pre-rendered text spliced in verbatim: no indentation applied, no line ending added.
 *
 * <p>
 * The escape hatch for text that is already indented by the emitter that produced it. Today that is
 * the parameter list an operation inherits from the path it hangs under: it comes in as a string with
 * its own leading spaces, and it is deliberately <em>not</em> newline-terminated, so whatever follows
 * continues on its last line. The current documents therefore contain lines such as
 *
 * <pre>
 *   example: __VNF-ID__        - name: body
 * </pre>
 *
 * which the goldens pin exactly. Re-indenting such a fragment would mean parsing text this generator
 * just produced; carrying it verbatim keeps the conversion incremental and honest about what has not
 * been modelled yet.
 *
 * <p>
 * Every remaining {@code YamlRaw} is a candidate for becoming real nodes - each one that disappears
 * is a piece of the document that the tree fully describes.
 *
 * @param text the exact characters to emit
 */
public record YamlRaw(String text) implements YamlNode {

    /**
     * An empty line, added to a mapping or a sequence by its {@code blankLine()} method.
     *
     * <p>
     * YAML does not need it and most of these documents do not have it, but the node-only flavour
     * puts blank lines around an operation's parameter block, so it is part of the bytes. A shared
     * instance so that a collection can recognise it by identity and not count it as content: a
     * sequence holding nothing but blank lines is still empty, and its key stays out of the document.
     */
    public static final YamlRaw BLANK_LINE = new YamlRaw("\n");

    @Override
    public boolean isEmpty() {
        return text == null || text.isEmpty();
    }
}
