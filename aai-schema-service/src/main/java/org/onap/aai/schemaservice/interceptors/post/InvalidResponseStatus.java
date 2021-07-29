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
package org.onap.aai.schemaservice.interceptors.post;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.schemaservice.interceptors.AAIContainerFilter;

import javax.annotation.Priority;
import javax.print.attribute.standard.Media;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Priority(AAIResponseFilterPriority.INVALID_RESPONSE_STATUS)
public class InvalidResponseStatus extends AAIContainerFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
        throws IOException {

        String contentType = responseContext.getHeaderString("Content-Type");
        ArrayList<String> templateVars = new ArrayList<>();
        List<MediaType> mediaTypeList = new ArrayList<>();
        AAIException e;
        String message = "";

        if (responseContext.getStatus() == 405) {

            // add the accept type error msg here as well.

            responseContext.setStatus(400);
            e = new AAIException("AAI_3012");

            if (contentType == null) {
                mediaTypeList.add(MediaType.APPLICATION_XML_TYPE);
            } else {
                mediaTypeList.add(MediaType.valueOf(contentType));
            }

            message = ErrorLogHelper.getRESTAPIErrorResponse(mediaTypeList, e, templateVars);

            responseContext.setEntity(message);
        } else if (responseContext.getStatus() == 406) {
            responseContext.setStatus(406);
            mediaTypeList.add(MediaType.valueOf(contentType));
            if (contentType == null) {
                mediaTypeList.add(MediaType.APPLICATION_XML_TYPE);
                e = new AAIException("AAI_3019", "null");
            } else if (contentType.equals(MediaType.APPLICATION_XML)) {
                e = new AAIException("AAI_3019", MediaType.APPLICATION_XML);
            } else if (contentType.equals(MediaType.APPLICATION_JSON)) {
                e = new AAIException("AAI_3019", MediaType.APPLICATION_JSON);
            } else {
                mediaTypeList.add(MediaType.valueOf(contentType));
                e = new AAIException("AAI_3019", contentType);
            }
            message = ErrorLogHelper.getRESTAPIErrorResponse(mediaTypeList, e, templateVars);
            responseContext.setEntity(message);
        }
    }
}
