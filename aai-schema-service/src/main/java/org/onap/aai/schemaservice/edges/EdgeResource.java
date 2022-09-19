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
import com.google.gson.GsonBuilder;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

@Path("/v1")
public class EdgeResource extends RESTAPI {

    private final EdgeService edgeService;
    private final SchemaVersions schemaVersions;
    private final Gson gson;

    @Autowired
    public EdgeResource(EdgeService edgeService, SchemaVersions schemaVersions) {
        this.edgeService = edgeService;
        this.schemaVersions = schemaVersions;
        gson = new GsonBuilder().create();
    }

    @GET
    @Path("/edgerules")
    @Produces({"application/json"})
    public Response retrieveSchema(@QueryParam("version") String version,
        @Context HttpHeaders headers, @Context UriInfo info) {
        Response response = null;

        try {

            if (StringUtils.isEmpty(version)) {
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
