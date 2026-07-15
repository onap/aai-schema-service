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

import org.apache.commons.lang3.StringUtils;

/**
 * Shared path/tag guards used by the swagger operation emitters (GET/PUT/PATCH/DELETE and the node
 * GET). Each operation historically re-implemented these checks inline; centralising them removes
 * the duplication while preserving the exact filtering behaviour. Each predicate is independent and
 * corresponds to a "return empty operation" guard, so callers may apply them in any order.
 */
final class OperationFilter {

    private OperationFilter() {
    }

    /** A valid (non-empty) tag is required for any operation to be emitted. */
    static boolean hasNoTag(String tag) {
        return StringUtils.isEmpty(tag);
    }

    /** Paths nested under a relationship (.../relationship/...) are not emitted as operations. */
    static boolean isRelationshipChildPath(String path) {
        return path.contains("/relationship/");
    }

    /** The relationship-list container endpoint itself is not emitted. */
    static boolean isRelationshipListPath(String path) {
        return path.endsWith("/relationship-list");
    }

    /** Search endpoints are documented separately and are never emitted here. */
    static boolean isSearchPath(String path) {
        return path.startsWith("/search");
    }
}
