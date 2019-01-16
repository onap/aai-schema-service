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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.schemaservice.nodeschema;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.restcore.HttpMethod;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.schemaservice.nodeschema.validation.AAISchemaValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Optional;

@Path("/v1")
public class NodeSchemaResource extends RESTAPI {

    private final NodeSchemaService nodeSchemaService;

    private final SchemaVersions schemaVersions;

    @Autowired
    public NodeSchemaResource(NodeSchemaService nodeSchemaService, SchemaVersions schemaVersions) {
        this.nodeSchemaService = nodeSchemaService;
        this.schemaVersions    = schemaVersions;
    }

    @GET
    @Path("/nodes")
    @Produces({ "application/xml"})
    public Response retrieveSchema(@QueryParam("version") String version,
                                   @Context HttpHeaders headers,
                                   @Context UriInfo info)
    {
        Response response;
        Optional<String> optionalSchema = nodeSchemaService.fetch(version);
        try {

            if(StringUtils.isEmpty(version)){
                throw new AAIException("AAI_3050");
            }

            SchemaVersion schemaVersion = new SchemaVersion(version);

            if(!schemaVersions.getVersions().contains(schemaVersion)){
                throw new AAIException("AAI_3018", version);
            }

            if (!optionalSchema.isPresent()) {
                throw new AAIException("AAI_3001");
            }

            response = Response.ok(optionalSchema.get()).build();

        } catch(AAIException ex){
            response = consumerExceptionResponseGenerator(headers, info, HttpMethod.GET, ex);
        } catch(AAISchemaValidationException ex){
            response = consumerExceptionResponseGenerator(headers, info, HttpMethod.GET, new AAIException("AAI_3051", version));
        } catch(Exception ex){
            response = consumerExceptionResponseGenerator(headers, info, HttpMethod.GET, new AAIException("AAI_4000"));
        }

        return response;
    }

}
