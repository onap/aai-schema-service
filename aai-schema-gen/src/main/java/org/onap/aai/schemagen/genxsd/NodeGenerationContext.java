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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Mutable state shared by the node-GET emitter while generating one version's {@code nodes} swagger
 * document.
 *
 * <p>
 * This replaces the {@code static} {@code containers} and {@code checklist} fields on
 * {@link NodeGetOperation}, together with the {@code resetContainers()} call that
 * {@link NodesYAMLfromOXM#process} had to make to stop state leaking between versions. Because the
 * old code reset this state at the top of every {@code process()} call, the context is created
 * per-version rather than per-run — unlike {@link GenerationContext}, which deliberately spans the
 * whole run.
 *
 * <p>
 * Not thread-safe — a generation run is single-threaded, as it was before.
 */
public class NodeGenerationContext {

    /**
     * Maps a container name to the rendered YAML query parameters contributed by its indexed
     * properties, used by {@link NodeGetOperation} to emit query parameters.
     */
    private final Map<String, Vector<String>> containers = new HashMap<>();

    /**
     * Objects for which a node-GET operation has already been emitted. Each object yields at most
     * one
     * {@code /nodes/...} endpoint, so the first emission wins and later ones are suppressed.
     */
    private final List<String> emittedObjects = new ArrayList<>();

    /** Associates the rendered query parameters of a container with that container's name. */
    public void addContainerProps(String container, Vector<String> containerProps) {
        containers.put(container, containerProps);
    }

    /**
     * Returns the rendered query parameters for a container, or {@code null} when the container has
     * no indexed properties. A {@code null} return is meaningful to callers and distinct from an
     * empty list.
     */
    public Vector<String> getContainerProps(String container) {
        return containers.get(container);
    }

    /** Whether a node-GET operation has already been emitted for this object. */
    public boolean isAlreadyEmitted(String xmlRootElementName) {
        return emittedObjects.contains(xmlRootElementName);
    }

    /** Marks this object as having had its node-GET operation emitted. */
    public void markEmitted(String xmlRootElementName) {
        emittedObjects.add(xmlRootElementName);
    }
}
