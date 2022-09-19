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

import com.google.gson.annotations.SerializedName;

public class EdgeRule {

    private String from;
    private String to;
    private String label;

    private String direction;
    private String multiplicity;
    private String description;

    @SerializedName("contains-other-v")
    private String containsOtherV;

    @Override
    public String toString() {
        return "EdgeRule{" + "from='" + from + '\'' + ", to='" + to + '\'' + ", label='" + label
            + '\'' + ", direction='" + direction + '\'' + ", multiplicity='" + multiplicity + '\''
            + ", description='" + description + '\'' + ", containsOtherV='" + containsOtherV + '\''
            + ", deleteOtherV='" + deleteOtherV + '\'' + ", preventDelete='" + preventDelete + '\''
            + ", privateEdge=" + privateEdge + ", isDefaultEdge=" + defaultEdge + '}';
    }

    @SerializedName("delete-other-v")
    private String deleteOtherV;
    @SerializedName("prevent-delete")
    private String preventDelete;

    @SerializedName("private")
    private String privateEdge;

    @SerializedName("default")
    private String defaultEdge;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(String multiplicity) {
        this.multiplicity = multiplicity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContainsOtherV() {
        return containsOtherV;
    }

    public void setContainsOtherV(String containsOtherV) {
        this.containsOtherV = containsOtherV;
    }

    public String getDeleteOtherV() {
        return deleteOtherV;
    }

    public void setDeleteOtherV(String deleteOtherV) {
        this.deleteOtherV = deleteOtherV;
    }

    public String getPreventDelete() {
        return preventDelete;
    }

    public void setPreventDelete(String preventDelete) {
        this.preventDelete = preventDelete;
    }

    public String getPrivateEdge() {
        return privateEdge;
    }

    public void setPrivateEdge(String privateEdge) {
        this.privateEdge = privateEdge;
    }

    public String getDefaultEdge() {
        return defaultEdge;
    }

    public void setDefaultEdge(String defaultEdge) {
        this.defaultEdge = defaultEdge;
    }

}
