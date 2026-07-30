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
 * A node of a YAML document tree.
 *
 * <p>
 * Nothing in the tree knows how deep it sits. A caller builds nested mappings, sequences and
 * scalars
 * that describe the document's <em>structure</em>, and {@link YamlSerializer} derives indentation
 * from the recursion when it walks the tree. That is the point of the type: the depth of a line
 * stops
 * being a number an author has to restate at every call site and becomes a consequence of where the
 * node was attached.
 *
 * <p>
 * The vocabulary is deliberately small and closed. The swagger documents this generator produces
 * use
 * block mappings, block sequences, plain scalars and literal block scalars, and nothing else -
 * no flow collections, no anchors, no tags - so the sealed hierarchy covers the whole grammar in
 * use, and {@link YamlSerializer} can switch over it exhaustively with no default branch to rot.
 *
 * <p>
 * Two members are not really YAML grammar: {@link YamlRaw} and {@link YamlFragment} carry text that
 * was already rendered elsewhere in the pipeline. They exist so that the conversion away from
 * string
 * concatenation can proceed one emitter at a time instead of as one large rewrite, and each of them
 * documents the fragments it currently holds.
 */
public sealed
interface YamlNode
permits YamlMapping, YamlSequence, YamlScalar, YamlBlock, YamlRaw, YamlFragment
{

    /**
     * True when this node would contribute nothing to the document. Callers use it to decide
     * whether
     * to emit a key at all - an empty {@code required:} list is omitted rather than written as an
     * empty sequence.
     */
    boolean isEmpty();
}
