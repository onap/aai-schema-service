/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2024 Deutsche Telekom. All rights reserved.
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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.web.bind.annotation.RestController;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Path("/v1")
@RestController
// @NoArgsConstructor
// @RequiredArgsConstructor
public class NodeSchemaChecksumResource {

    private final NodeSchemaService nodeSchemaService;
    private final ChecksumResponse checksumResponse;

    public NodeSchemaChecksumResource(NodeSchemaService nodeSchemaService, SchemaVersions schemaVersions) {
        this.nodeSchemaService = nodeSchemaService;
        Map<SchemaVersion, Long> checksumMap = schemaVersions.getVersions().stream()
            .collect(Collectors.toMap(
                version -> version,
                version -> getChecksumForSchemaVersion(version))
            );
        checksumResponse = new ChecksumResponse(checksumMap);
    }

    @GET
    @Path("/nodes/checksums")
    @Produces({"application/json"})
    public Response getChecksumByVersion(@Context HttpHeaders headers, @Context UriInfo info) {
        return Response.ok(checksumResponse).build();
    }

    private Long getChecksumForSchemaVersion(SchemaVersion version) {
        Optional<String> optionalSchema = nodeSchemaService.fetch(version.toString());
        if (optionalSchema.isPresent()) {
            return getCRC32Checksum(optionalSchema.get().getBytes());
        }
        return 0L;
    }

    private long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

}
