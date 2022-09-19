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

package org.onap.aai.schemaservice.versions;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VersionService {

    @Value("#{'${schema.version.list}'.split(',')}")
    private List<String> schemaVersionList;

    @Value("${schema.version.depth.start}")
    private String depthStart;

    @Value("${schema.version.related.link.start}")
    private String relatedLinkStart;

    @Value("${schema.version.app.root.start}")
    private String appRootStart;

    @Value("${schema.version.namespace.change.start}")
    private String namespaceChangeStart;

    @Value("${schema.version.edge.label.start}")
    private String edgeLabelStart;

    @Value("${schema.version.api.default}")
    private String defaultApi;

    public Version getVersionInfo() {

        Version schemaVersion = new Version();
        schemaVersion.setVersions(schemaVersionList);
        schemaVersion.setDepthVersion(depthStart);
        schemaVersion.setRelatedLinkVersion(relatedLinkStart);
        schemaVersion.setAppRootVersion(appRootStart);
        schemaVersion.setNamespaceChangeVersion(namespaceChangeStart);
        schemaVersion.setEdgeVersion(edgeLabelStart);
        schemaVersion.setDefaultVersion(defaultApi);

        return schemaVersion;
    }
}
