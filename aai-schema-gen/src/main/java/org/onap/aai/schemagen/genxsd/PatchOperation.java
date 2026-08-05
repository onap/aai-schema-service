/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.schemagen.genxsd;

import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;

import java.util.List;

import org.onap.aai.setup.SchemaVersion;

/**
 * The PATCH of a CRUD endpoint: it updates some fields of the object the path addresses.
 *
 * <p>
 * A PATCH payload is a partial object, so it has a definition of its own - the same properties with
 * none of them required, and without the server-owned {@code resource-version}. The generator names
 * those definitions with a prefix it passes to {@link #setPrefixForPatchRef}.
 */
public class PatchOperation {

    private final String useOpId;
    private final String xmlRootElementName;
    private final String tag;
    private final String path;
    private final List<Parameter> pathParams;
    private final SchemaVersion version;
    private final String basePath;
    private String prefixForPatch = "";

    public PatchOperation(String useOpId, String xmlRootElementName, String tag, String path,
        List<Parameter> pathParams, SchemaVersion v, String basePath) {
        this.useOpId = useOpId;
        this.xmlRootElementName = xmlRootElementName;
        this.tag = tag;
        this.path = path;
        this.pathParams = pathParams == null ? List.of() : List.copyOf(pathParams);
        this.version = v;
        this.basePath = basePath;
    }

    public void setPrefixForPatchRef(String prefixForPatchRef) {
        this.prefixForPatch = prefixForPatchRef;
    }

    /** This endpoint's PATCH, or null when the endpoint does not have one. */
    public Operation build() {
        if (isFilteredOut()) {
            return null;
        }
        Operation patch = new Operation();
        patch.addTag(tag);
        patch.setSummary("update an existing " + xmlRootElementName);
        // the trailing newline is what makes this render as a literal block, not as one line
        patch.setDescription(String.join("\n", "Update an existing " + xmlRootElementName, "#",
            "Note:  Endpoints that are not devoted to object relationships support both PUT and PATCH operations.",
            "The PUT operation will entirely replace an existing object.",
            "The PATCH operation sends a \"description of changes\" for an existing object.  The entire set of changes must be applied.  An error result means no change occurs.",
            "#", "Other differences between PUT and PATCH are:", "#",
            "- For PATCH, you can send any of the values shown in sample REQUEST body.  There are no required values.",
            "- For PATCH, resource-id which is a required REQUEST body element for PUT, must not be sent.",
            "- PATCH cannot be used to update relationship elements; there are dedicated PUT operations for this.",
            ""));
        patch.setOperationId("Update" + useOpId);
        // a patch document has no XML form here
        patch.setConsumes(OperationDefaults.JSON_ONLY);
        patch.setProduces(OperationDefaults.JSON_ONLY);
        patch.addResponse("default", OperationDefaults.uniformResponse());
        pathParams.forEach(patch::addParameter);
        patch.addParameter(bodyParameter());
        return patch;
    }

    private BodyParameter bodyParameter() {
        BodyParameter body = new BodyParameter();
        body.setName("body");
        body.setDescription(
            xmlRootElementName + " object that needs to be updated." + "[See Examples](apidocs"
                + basePath + "/relations/" + version.toString() + "/" + useOpId + ".json)");
        body.setRequired(true);
        body.setSchema(new RefModel(prefixForPatch + xmlRootElementName));
        return body;
    }

    /**
     * A PATCH exists where a single object is addressed. Unlike the PUT there is none on a
     * relationship endpoint: relationships are replaced whole.
     */
    private boolean isFilteredOut() {
        return OperationFilter.hasNoTag(tag) || OperationFilter.isRelationshipChildPath(path)
            || OperationFilter.isRelationshipListPath(path) || OperationFilter.isSearchPath(path)
            || path.endsWith("/" + PutOperation.RELATIONSHIP) || !path.endsWith("}");
    }
}
