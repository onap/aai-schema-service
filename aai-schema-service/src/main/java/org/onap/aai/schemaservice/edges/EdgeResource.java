/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aai.schemaservice.edges;

import com.google.gson.Gson;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.restcore.HttpMethod;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.schemaservice.nodeschema.SchemaVersion;
import org.onap.aai.schemaservice.nodeschema.SchemaVersions;
import org.onap.aai.schemaservice.nodeschema.validation.AAISchemaValidationException;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RestController;

@Path("/v1")
@RestController
@RequiredArgsConstructor
@Tag(name = "ONAP Schema Service - Edge Rules", description = "Provides access to ONAP edge rules for a given schema version in JSON format.")
public class EdgeResource extends RESTAPI {

    private final EdgeService edgeService;
    private final SchemaVersions schemaVersions;
    private final Gson gson;

    @GET
    @Path("/edgerules")
    @Produces({ "application/json" })
    @Operation(summary = "Retrieve edge rules by version", description = "Returns the JSON-formatted edge rules for the specified ONAP schema version.", parameters = {
            @Parameter(name = "version", description = "Schema version", required = true, example = "v30")
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Edge rules retrieved successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Missing or invalid version", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Edge rules not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    public Response retrieveSchema(@QueryParam("version") String version,
        @Context HttpHeaders headers, @Context UriInfo info) {
        Response response = null;

        try {

            if (ObjectUtils.isEmpty(version)) {
                throw new AAIException("AAI_3050");
            }

            SchemaVersion schemaVersion = new SchemaVersion(version);

            if (!schemaVersions.getVersions().contains(schemaVersion)) {
                throw new AAIException("AAI_3018", version);
            }

            Optional<EdgeRules> edgeRulesOptional = edgeService.findRules(version);

            if (!edgeRulesOptional.isPresent()) {
                throw new AAIException("AAI_3001");
            }

            response = Response.ok(gson.toJson(edgeRulesOptional.get())).build();
        } catch (AAIException ex) {
            response = consumerExceptionResponseGenerator(headers, info, HttpMethod.GET, ex);
        } catch (AAISchemaValidationException ex) {
            response = consumerExceptionResponseGenerator(headers, info, HttpMethod.GET,
                new AAIException("AAI_3051", version));
        } catch (Exception ex) {
            response = consumerExceptionResponseGenerator(headers, info, HttpMethod.GET,
                new AAIException("AAI_4000"));
        }

        return response;
    }
}
