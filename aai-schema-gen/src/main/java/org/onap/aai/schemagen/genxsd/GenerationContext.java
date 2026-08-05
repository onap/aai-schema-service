/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.schemagen.genxsd;

import io.swagger.models.parameters.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mutable state shared between the swagger operation emitters during a generation run.
 *
 * <p>
 * This state used to live in {@code static} fields on {@link DeleteOperation},
 * {@link PutRelationPathSet} and {@link GetOperation}, which made the emitters implicitly coupled
 * through global variables and required {@code reset*()} calls to stop state leaking between runs.
 * Collecting it here makes the data flow explicit and the emitters testable in isolation.
 *
 * <p>
 * <b>Lifetime matters for output fidelity.</b> {@code GenerateXsd} generates every configured API
 * version inside a single JVM run, and the three maps below were never cleared between versions —
 * so a later version observes the paths registered by earlier ones. {@code getKinObjectPath} in
 * {@link PutRelationPathSet} picks the closest-matching delete path across everything registered so
 * far, so narrowing the lifetime to a single version would change the generated relations files.
 * This context is therefore held as a <em>singleton</em> spanning the whole run, deliberately
 * reproducing the pre-existing accumulate-across-versions behaviour.
 *
 * <p>
 * {@link NodesYAMLfromOXM}'s node-GET state is the exception: it was explicitly reset per version
 * via {@code NodeGetOperation.resetContainers()} and keeps that per-version lifetime in
 * {@link NodeGenerationContext}.
 *
 * <p>
 * Not thread-safe — a generation run is single-threaded, as it was before.
 */
public class GenerationContext {

    /**
     * Maps an API path to the {@code xml-root-element} name of the object it addresses, for every
     * DELETE operation emitted so far. Populated by
     * {@link DeleteOperation#register(GenerationContext)} and read by {@link PutRelationPathSet} to
     * resolve relationship targets.
     *
     * <p>
     * Deliberately a {@link HashMap}: {@code PutRelationPathSet} iterates this map when scoring
     * candidate paths, so its iteration order is baked into the generated relations files.
     */
    private final Map<String, String> deletePaths = new HashMap<>();

    /**
     * Maps a PUT operation id to its {@code .../relationship-list/relationship} path. Populated by
     * {@link PutOperation#register(GenerationContext)} and consumed by
     * {@link PutRelationPathSet#generateRelations}.
     */
    private final Map<String, String> putRelationPaths = new HashMap<>();

    /**
     * Maps a container name to the query parameters contributed by its indexed properties, used by
     * {@link GetOperation} to emit query parameters.
     */
    private final Map<String, List<Parameter>> containers = new HashMap<>();

    public Map<String, String> getDeletePaths() {
        return deletePaths;
    }

    public Map<String, String> getPutRelationPaths() {
        return putRelationPaths;
    }

    /** Records the path of an emitted DELETE operation against the object it addresses. */
    public void addDeletePath(String path, String xmlRootElementName) {
        deletePaths.put(path, xmlRootElementName);
    }

    /** Looks up the object addressed by a previously registered DELETE path, or {@code null}. */
    public String getDeletePathObject(String path) {
        return deletePaths.get(path);
    }

    /** Records the relationship path of an emitted PUT operation against its operation id. */
    public void addPutRelationPath(String useOpId, String path) {
        putRelationPaths.put(useOpId, path);
    }

    /** Associates the query parameters of a container with that container's name. */
    public void addContainerProps(String container, List<Parameter> containerProps) {
        containers.put(container, containerProps);
    }

    /**
     * Returns the query parameters for a container, or {@code null} when the container has no
     * indexed properties. A {@code null} return is meaningful to callers and distinct from an empty
     * list.
     */
    public List<Parameter> getContainerProps(String container) {
        return containers.get(container);
    }

    /**
     * Discards all accumulated state. Intended for tests, which share one context bean across test
     * methods and need each to start clean; production code creates a context per run instead of
     * clearing one.
     */
    void clear() {
        deletePaths.clear();
        putRelationPaths.clear();
        containers.clear();
    }
}
