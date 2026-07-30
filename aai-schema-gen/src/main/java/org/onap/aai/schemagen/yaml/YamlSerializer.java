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
 * Renders a {@link YamlNode} tree as Swagger-2.0-flavoured YAML text.
 *
 * <p>
 * All indentation lives here. The recursion carries the depth, so a node's position in the tree is
 * what decides its indentation and no caller states a number per line. That is the whole reason
 * this
 * class exists as a separate step: the emitters describe structure, this describes layout, and the
 * two can be changed independently - a second serializer for OpenAPI 3 needs no change to any
 * emitter.
 *
 * <p>
 * <b>Why this is hand-written rather than snakeyaml.</b> This phase must reproduce the checked-in
 * documents byte for byte, and snakeyaml cannot: it indents block-sequence indicators at the
 * parent's
 * level while these documents indent them one level deeper (and {@code setIndicatorIndent} refuses
 * any value not smaller than the mapping indent, so the combination AAI uses is unreachable), it
 * normalises quoting, it re-wraps long scalars, and it has no way to express the two positional
 * anomalies below. Once the OpenAPI 3 documents are generated fresh, none of those constraints hold
 * and a snakeyaml-based serializer becomes the natural second implementation - the tree is already
 * in the right shape to hand to it.
 *
 * <p>
 * Two deviations from ordinary YAML layout are reproduced on request, never by default:
 * {@link YamlMapping#entryAtShiftedDepth} shifts a value's depth, and
 * {@link YamlMapping#entryWithPaddedKey} pads a key line with trailing blanks. Both are documented
 * at their definition, and both should disappear when the documents may change.
 */
public final class YamlSerializer {

    /** One nesting level. These documents are indented two spaces per level throughout. */
    private static final String INDENT_UNIT = "  ";

    /**
     * The line ending used for the document body. Deliberately {@code \n} and not the platform
     * separator: the generated YAML files use it throughout, so this stays fixed regardless of the
     * machine that runs the build.
     */
    private static final String NEWLINE = "\n";

    private final StringBuilder out = new StringBuilder();

    private YamlSerializer() {
    }

    /** Renders {@code root} as a whole document, its top level unindented. */
    public static String serialize(YamlNode root) {
        return serialize(root, 0);
    }

    /**
     * Renders {@code root} as a fragment whose top level sits {@code startLevel} levels deep.
     *
     * <p>
     * For the emitters that still hand their output to a caller as a string to be concatenated - an
     * operation spliced under a path, a parameter spliced into an operation. The starting depth is
     * stated once, for the fragment's root, and every line inside it follows from the tree; as more
     * of the pipeline is converted these call sites collapse into their parents and the argument
     * goes
     * away.
     */
    public static String serialize(YamlNode root, int startLevel) {
        YamlSerializer serializer = new YamlSerializer();
        serializer.write(root, startLevel);
        return serializer.out.toString();
    }

    private void write(YamlNode node, int level) {
        if (node instanceof YamlMapping mapping) {
            writeMapping(mapping, level);
        } else if (node instanceof YamlSequence sequence) {
            writeSequence(sequence, level);
        } else if (node instanceof YamlBlock block) {
            writeBlock(block, level);
        } else if (node instanceof YamlScalar scalar) {
            indent(level);
            writeScalar(scalar);
            out.append(NEWLINE);
        } else if (node instanceof YamlFragment fragment) {
            indent(level);
            out.append(fragment.text());
        } else if (node instanceof YamlRaw raw) {
            out.append(raw.text());
        } else {
            // unreachable while YamlNode stays sealed, but a new member must not fail silently
            throw new IllegalArgumentException("unsupported node type: " + node.getClass());
        }
    }

    private void writeMapping(YamlMapping mapping, int level) {
        for (YamlMapping.Entry entry : mapping.entries()) {
            if (entry.key() == null) {
                write(entry.value(), level);
                continue;
            }
            int valueLevel = level + 1 + entry.levelShift();
            if (entry.value()instanceof YamlScalar scalar) {
                // a scalar shares its key's line: "name: value"
                indent(level).append(entry.key()).append(": ");
                writeScalar(scalar);
                out.append(NEWLINE);
            } else if (entry.value()instanceof YamlBlock block) {
                indent(level).append(entry.key()).append(": |").append(NEWLINE);
                writeBlock(block, valueLevel);
            } else {
                indent(level).append(entry.key()).append(":").append(" ".repeat(entry.keyPadding()))
                    .append(NEWLINE);
                write(entry.value(), valueLevel);
            }
        }
    }

    private void writeSequence(YamlSequence sequence, int level) {
        for (YamlNode item : sequence.items()) {
            if (item instanceof YamlScalar scalar) {
                indent(level).append("- ");
                writeScalar(scalar);
                out.append(NEWLINE);
            } else if (item instanceof YamlMapping mapping) {
                writeMappingItem(mapping, level);
            } else if (item instanceof YamlRaw raw) {
                // a run of items that another emitter already rendered, indicators and all
                out.append(raw.text());
            } else {
                throw new IllegalArgumentException(
                    "a sequence item must be a scalar, a mapping or pre-rendered text, not "
                        + item.getClass());
            }
        }
    }

    /**
     * A mapping that is itself a sequence item: its first key shares the {@code -} line and the
     * rest
     * hang one level below, as in
     *
     * <pre>
     *   - name: body
     *     in: body
     * </pre>
     */
    private void writeMappingItem(YamlMapping mapping, int level) {
        if (mapping.isEmpty()) {
            return;
        }
        YamlMapping.Entry first = mapping.entries().get(0);
        if (first.key() == null || !(first.value()instanceof YamlScalar firstValue)) {
            throw new IllegalArgumentException(
                "the first entry of a mapping used as a sequence item must be a scalar entry, "
                    + "so that it can share the indicator line");
        }
        indent(level).append("- ").append(first.key()).append(": ");
        writeScalar(firstValue);
        out.append(NEWLINE);
        for (YamlMapping.Entry entry : mapping.entries().subList(1, mapping.entries().size())) {
            writeMapping(new YamlMapping().entry(entry.key(), entry.value()), level + 1);
        }
    }

    private void writeBlock(YamlBlock block, int level) {
        for (String line : block.lines()) {
            indent(level).append(line).append(NEWLINE);
        }
        if (block.trailing() != null) {
            out.append(block.trailing());
        }
    }

    private void writeScalar(YamlScalar scalar) {
        out.append(scalar.value());
    }

    private StringBuilder indent(int level) {
        if (level < 0) {
            throw new IllegalArgumentException("negative indentation level: " + level);
        }
        return out.append(INDENT_UNIT.repeat(level));
    }
}
