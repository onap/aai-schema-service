/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2026 Deutsche Telekom.
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

package org.onap.aai.schemagen.genxsd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.util.StringQuotingChecker;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Responses;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

import java.util.List;

/**
 * Serializes a {@link Swagger} document.
 *
 * <p>
 * The generators build a {@link Swagger} model - the Swagger 2.0 specification's own document
 * model - and this class renders it. There is no per-line emission anywhere: no indent arithmetic,
 * no quoting decisions at the call site, no {@code "\n"} in a generator. That is the point of
 * modelling the document rather than assembling its text, and it is why {@link #toJson} costs a
 * method instead of a second emitter.
 *
 * <p>
 * The configuration below exists only to keep the published artefacts close to the documents this
 * replaced: unquoted descriptions, quoted response keys, literal blocks for the multi-line
 * markdown, and unfolded long lines. It is a writer setting per convention, not code that knows
 * about AAI.
 */
public final class SwaggerWriter {

    /**
     * Swagger 2.0 orders an operation's members; Jackson would otherwise order them by declaration.
     * {@code responsesObject} is a second view of {@code responses} the library keeps for its own
     * parser, and it would serialize as a duplicate.
     */
    @JsonPropertyOrder({"tags", "summary", "description", "operationId", "consumes", "produces",
        "responses", "parameters"})
    private abstract static class OperationOrder {
        @JsonIgnore
        abstract Responses getResponsesObject();
    }

    /**
     * A definition puts its description before its required list.
     *
     * <p>
     * {@code AbstractModel.getRequired()} does not return the list that was set: it rebuilds one
     * from the properties whose own {@code required} flag is set, and sorts it. That drops entries
     * and reorders the rest, so the field is serialized directly instead.
     */
    @JsonPropertyOrder({"type", "description", "required", "properties"})
    private abstract static class ModelOrder {
        @JsonProperty("required")
        List<String> required;

        @JsonIgnore
        abstract List<String> getRequired();
    }

    /**
     * A response holds its schema in one of two fields depending on which setter was used, and
     * publishes both - each converting from the other's field when its own is unset. The model form
     * is the one that is not deprecated; the specification calls the member {@code schema} either
     * way.
     */
    @JsonPropertyOrder({"description", "schema", "examples", "headers"})
    private abstract static class ResponseSchema {
        @JsonProperty("schema")
        abstract Model getResponseSchema();

        @JsonIgnore
        abstract Property getSchema();
    }

    /** The order the AAI documents list a path's operations in. */
    @JsonPropertyOrder({"get", "put", "patch", "delete", "post", "head", "options", "parameters"})
    private abstract static class PathOrder {
    }

    /**
     * A body parameter is named before it is placed, like every other parameter. Only
     * {@code BodyParameter} lacks the library's own ordering annotation, so it would otherwise
     * serialize the two the other way round.
     */
    @JsonPropertyOrder({"name", "in", "description", "required", "schema"})
    private abstract static class BodyParameterOrder {
    }

    /** {@code originalRef} is a library-internal convenience, not part of the specification. */
    private abstract static class HideOriginalRef {
        @JsonIgnore
        abstract String getOriginalRef();
    }

    /**
     * The quoting rules of the AAI swagger documents. Jackson's default quotes any scalar that
     * merely <em>could</em> be mistaken for structure - one containing a bracket or a comma, which
     * most AAI descriptions do. YAML only requires quoting when a value would actually parse as
     * something else, so this checker quotes exactly that, and response keys, which the documents
     * quote by convention.
     */
    private static final class AaiQuotingChecker extends StringQuotingChecker {

        /** Characters that change a scalar's meaning when they open it. */
        private static final String LEADING_INDICATORS = "-?:,[]{}#&*!|>'\"%@`";

        @Override
        public boolean needToQuoteName(String name) {
            if (name.isEmpty()) {
                return true;
            }
            if ("default".equals(name)) {
                return true;
            }
            return _isReservedKeyword(name.charAt(0), name)
                || _looksLikeYAMLNumber(name.charAt(0), name);
        }

        @Override
        public boolean needToQuoteValue(String value) {
            if (value.isEmpty()) {
                return true;
            }
            return _isReservedKeyword(value.charAt(0), value)
                || _looksLikeYAMLNumber(value.charAt(0), value)
                || LEADING_INDICATORS.indexOf(value.charAt(0)) >= 0 || value.startsWith(" ")
                || value.endsWith(" ")
                // " #" opens a comment and ": " a mapping, mid-scalar as much as at the start
                || value.contains(" #") || value.contains(": ") || value.endsWith(":");
        }
    }

    private static final ObjectMapper YAML_MAPPER = yamlMapper();
    private static final ObjectMapper JSON_MAPPER = jsonMapper();

    private SwaggerWriter() {
    }

    /** The document as Swagger 2.0 YAML. */
    public static String toYaml(Swagger document) {
        return write(YAML_MAPPER, document);
    }

    /** The same document as Swagger 2.0 JSON, from the same model. */
    public static String toJson(Swagger document) {
        return write(JSON_MAPPER, document);
    }

    private static String write(ObjectMapper mapper, Swagger document) {
        try {
            return mapper.writeValueAsString(document);
        } catch (JsonProcessingException e) {
            // the model is built in-process from OXM; a failure here is a bug, not bad input
            throw new IllegalStateException("Unable to serialize the swagger document", e);
        }
    }

    private static ObjectMapper yamlMapper() {
        YAMLFactory factory = YAMLFactory.builder().stringQuotingChecker(new AaiQuotingChecker())
            // a swagger document is the whole file, so it needs no "---"
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            // ... except "2.0" and "v29", which would otherwise read as a number
            .enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
            // the descriptions are markdown; a literal block keeps them readable
            .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
            .enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
            // AAI paths run well past 128 characters and are keys
            .enable(YAMLGenerator.Feature.ALLOW_LONG_KEYS)
            // folding a description would change the text a reader sees
            .disable(YAMLGenerator.Feature.SPLIT_LINES).build();
        return configure(new ObjectMapper(factory));
    }

    private static ObjectMapper jsonMapper() {
        return configure(new ObjectMapper()).enable(SerializationFeature.INDENT_OUTPUT);
    }

    private static ObjectMapper configure(ObjectMapper mapper) {
        // an unset member is absent; an empty list would otherwise render as "parameters: []"
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.addMixIn(Operation.class, OperationOrder.class);
        mapper.addMixIn(ModelImpl.class, ModelOrder.class);
        mapper.addMixIn(Response.class, ResponseSchema.class);
        mapper.addMixIn(Path.class, PathOrder.class);
        mapper.addMixIn(BodyParameter.class, BodyParameterOrder.class);
        mapper.addMixIn(RefModel.class, HideOriginalRef.class);
        mapper.addMixIn(RefProperty.class, HideOriginalRef.class);
        return mapper;
    }
}
