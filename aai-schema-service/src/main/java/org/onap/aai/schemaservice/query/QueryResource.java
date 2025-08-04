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
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Path("/v1")
@RestController
@RequiredArgsConstructor
@Tag(name = "Stored Queries Resource", description = "Returns custom stored queries")
public class QueryResource {

    private final QueryService queryService;

    @GET
    @Path("/stored-queries")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Fetch stored queries", description = "Fetch predefined stored queries used in AAI traversals")
    public Response retrieveStoredQueries() {
        return Response.ok().entity(queryService.getStoredQueries()).build();
    }

}
