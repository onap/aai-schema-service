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

package org.onap.aai.schemaservice.healthcheck;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.restcore.RESTAPI;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Class EchoResponse.
 */
@Path("/util")
@RestController
public class EchoResource extends RESTAPI {

    /**
     * Simple health-check API that echos back the X-FromAppId and X-TransactionId to clients.
     * If there is a query string, a transaction gets logged into hbase, proving the application is
     * connected to the data store.
     * If there is no query string, no transaction logging is done to hbase.
     *
     * @param headers the headers
     * @param req the req
     * @param uriInfo uri information
     * @return the response
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/echo")
    public Response echoResult(@Context HttpHeaders headers, @Context HttpServletRequest req,
        @Context UriInfo uriInfo) {
        Response response = null;

        AAIException ex = null;
        String fromAppId = null;
        String transId = null;

        try {
            fromAppId = getFromAppId(headers);
            transId = getTransId(headers);
        } catch (AAIException e) {
            ArrayList<String> templateVars = new ArrayList<String>();
            templateVars.add("Headers missing");
            return Response
                .status(e.getErrorObject().getHTTPResponseCode()).entity(ErrorLogHelper
                    .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
                .build();
        }

        try {

            HashMap<AAIException, ArrayList<String>> exceptionList =
                new HashMap<AAIException, ArrayList<String>>();

            ArrayList<String> templateVars = new ArrayList<String>();
            templateVars.add(fromAppId);
            templateVars.add(transId);

            exceptionList.put(new AAIException("AAI_0002", "OK"), templateVars);

            response = Response
                .status(Status.OK).entity(ErrorLogHelper
                    .getRESTAPIInfoResponse(new ArrayList<>(headers.getAcceptableMediaTypes()), exceptionList))
                .build();

        } catch (Exception e) {
            ex = new AAIException("AAI_4000", e);
            ArrayList<String> templateVars = new ArrayList<String>();
            templateVars.add(Action.GET.name());
            templateVars.add(fromAppId + " " + transId);

            response = Response
                .status(Status.INTERNAL_SERVER_ERROR).entity(ErrorLogHelper
                    .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), ex, templateVars))
                .build();

        } finally {
            if (ex != null) {
                ErrorLogHelper.logException(ex);
            }

        }

        return response;
    }

}
