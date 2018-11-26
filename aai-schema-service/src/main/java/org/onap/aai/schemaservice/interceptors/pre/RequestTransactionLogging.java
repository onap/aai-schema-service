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
package org.onap.aai.schemaservice.interceptors.pre;

import com.google.gson.JsonObject;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerException;
import org.onap.aai.schemaservice.interceptors.AAIContainerFilter;
import org.onap.aai.schemaservice.interceptors.AAIHeaderProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@PreMatching
@Priority(AAIRequestFilterPriority.REQUEST_TRANS_LOGGING)
public class RequestTransactionLogging extends AAIContainerFilter implements ContainerRequestFilter {

    private static final String DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_JSON;
    private static final String DEFAULT_RESPONSE_TYPE = MediaType.APPLICATION_XML;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT = "Accept";
    private static final String TEXT_PLAIN = "text/plain";
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        String currentTimeStamp = genDate();
        String fullId = this.getAAITxIdToHeader();
        this.addToRequestContext(requestContext, AAIHeaderProperties.AAI_TX_ID, fullId);
        this.addToRequestContext(requestContext, AAIHeaderProperties.AAI_REQUEST, this.getRequest(requestContext, fullId));
        this.addToRequestContext(requestContext, AAIHeaderProperties.AAI_REQUEST_TS, currentTimeStamp);
        this.addDefaultContentType(requestContext);
    }

    private void addToRequestContext(ContainerRequestContext requestContext, String name, String aaiTxIdToHeader) {
        requestContext.setProperty(name, aaiTxIdToHeader);
    }

    private void addDefaultContentType(ContainerRequestContext requestContext) {

        String contentType = requestContext.getHeaderString(CONTENT_TYPE);
        String acceptType = requestContext.getHeaderString(ACCEPT);

        if (contentType == null || contentType.contains(TEXT_PLAIN)) {
            requestContext.getHeaders().putSingle(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
        }

        if (StringUtils.isEmpty(acceptType) || acceptType.contains(TEXT_PLAIN)) {
            requestContext.getHeaders().putSingle(ACCEPT, DEFAULT_RESPONSE_TYPE);
        }
    }

    private String getAAITxIdToHeader() {
        String txId = UUID.randomUUID().toString();
        return txId;
    }

    private String getRequest(ContainerRequestContext requestContext, String fullId) {

        JsonObject request = new JsonObject();
        request.addProperty("ID", fullId);
        request.addProperty("Http-Method", requestContext.getMethod());
        request.addProperty(CONTENT_TYPE, httpServletRequest.getContentType());
        request.addProperty("Headers", requestContext.getHeaders().toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = requestContext.getEntityStream();

        try {
            if (in.available() > 0) {
                ReaderWriter.writeTo(in, out);
                byte[] requestEntity = out.toByteArray();
                request.addProperty("Payload", new String(requestEntity, "UTF-8"));
                requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
            }
        } catch (IOException ex) {
            throw new ContainerException(ex);
        }

        return request.toString();
    }

}
