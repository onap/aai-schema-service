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

package org.onap.aai.schemaservice.query;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Path("/v1")
@RestController
@RequiredArgsConstructor
@Tag(name = "ONAP Schema Service - Queries", description = "APIs to retrieve stored queries for ONAP schema service.")
public class QueryResource {

    private final QueryService queryService;

    @GET
    @Path("/stored-queries")
    @Operation(summary = "Retrieve stored queries", description = "Returns the list of stored queries available in the system.", responses = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully", content = @Content(mediaType = "application/json"))
    })
    public Response retrieveStoredQueries() {
        return Response.ok().entity(queryService.getStoredQueries()).build();
    }

}
