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

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class Version {

    private List<String> versions;

    @SerializedName("edge-version")
    private String edgeVersion;
    @SerializedName("default-version")
    private String defaultVersion;
    @SerializedName("depth-version")
    private String depthVersion;
    @SerializedName("app-root-version")
    private String appRootVersion;
    @SerializedName("related-link-version")
    private String relatedLinkVersion;
    @SerializedName("namespace-change-version")
    private String namespaceChangeVersion;

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public String getEdgeVersion() {
        return edgeVersion;
    }

    public void setEdgeVersion(String edgeVersion) {
        this.edgeVersion = edgeVersion;
    }

    public String getDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(String defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public String getDepthVersion() {
        return depthVersion;
    }

    public void setDepthVersion(String depthVersion) {
        this.depthVersion = depthVersion;
    }

    public String getAppRootVersion() {
        return appRootVersion;
    }

    public void setAppRootVersion(String appRootVersion) {
        this.appRootVersion = appRootVersion;
    }

    public String getRelatedLinkVersion() {
        return relatedLinkVersion;
    }

    public void setRelatedLinkVersion(String relatedLinkVersion) {
        this.relatedLinkVersion = relatedLinkVersion;
    }

    public String getNamespaceChangeVersion() {
        return namespaceChangeVersion;
    }

    public void setNamespaceChangeVersion(String namespaceChangeVersion) {
        this.namespaceChangeVersion = namespaceChangeVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Version that = (Version) o;
        return Objects.equals(versions, that.versions)
            && Objects.equals(edgeVersion, that.edgeVersion)
            && Objects.equals(defaultVersion, that.defaultVersion)
            && Objects.equals(depthVersion, that.depthVersion)
            && Objects.equals(appRootVersion, that.appRootVersion)
            && Objects.equals(relatedLinkVersion, that.relatedLinkVersion)
            && Objects.equals(namespaceChangeVersion, that.namespaceChangeVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versions, edgeVersion, defaultVersion, depthVersion, appRootVersion,
            relatedLinkVersion, namespaceChangeVersion);
    }

    @Override
    public String toString() {
        return "Version{" + "versions=" + versions + ", edgeVersion='" + edgeVersion + '\''
            + ", defaultVersion='" + defaultVersion + '\'' + ", depthVersion='" + depthVersion
            + '\'' + ", appRootVersion='" + appRootVersion + '\'' + ", relatedLinkVersion='"
            + relatedLinkVersion + '\'' + ", namespaceChangeVersion='" + namespaceChangeVersion
            + '\'' + '}';
    }

}
