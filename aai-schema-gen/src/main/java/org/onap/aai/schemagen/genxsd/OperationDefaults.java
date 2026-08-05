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

import io.swagger.models.RefModel;
import io.swagger.models.Response;

import java.util.List;

/**
 * The parts every AAI operation documents the same way: which media types it speaks and which
 * responses it declares.
 *
 * <p>
 * Each operation emitter used to spell these out itself, so a change of wording meant five edits
 * and
 * the responses arrived as a pre-rendered YAML fragment threaded through a static field on
 * {@code GenerateXsd}.
 */
final class OperationDefaults {

    /** Most endpoints accept and return either representation. */
    static final List<String> JSON_AND_XML = List.of("application/json", "application/xml");

    /** PATCH is JSON only: a patch document has no XML form here. */
    static final List<String> JSON_ONLY = List.of("application/json");

    private OperationDefaults() {
    }

    /**
     * The {@code default} response. AAI answers every endpoint with the same set of status codes,
     * so
     * rather than repeat them per operation the documents point at the shared list.
     */
    static Response uniformResponse() {
        Response response = new Response();
        response.setDescription("Response codes are uniform across all endpoints.");
        return response;
    }

    /** The {@code 200} response of a GET, whose body is the object the path addresses. */
    static Response successResponse(String xmlRootElementName) {
        Response response = new Response();
        response.setDescription("successful operation");
        response.setResponseSchema(new RefModel(xmlRootElementName));
        return response;
    }
}
