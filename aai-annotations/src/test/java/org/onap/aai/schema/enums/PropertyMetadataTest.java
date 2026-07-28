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

package org.onap.aai.schema.enums;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.onap.aai.annotations.Metadata;

/**
 * Guards the two invariants that let an OXM {@code <xml-property>} name be used as a constraint
 * facet:
 *
 * <ul>
 * <li>the name resolves to a {@link PropertyMetadata} constant after the LOWER_CAMEL to
 * UPPER_UNDERSCORE conversion that {@code MoxyStrategy.getPropertyMetadata} applies - an
 * unrecognised name throws {@link IllegalArgumentException} on the first introspection of a node
 * type that carries it</li>
 * <li>the name is also a member of {@link Metadata}, because the generated XSD copies every
 * {@code <xml-property>} name verbatim into {@code @Metadata(name="value")} and an unknown member
 * fails the xjc compile of that XSD</li>
 * </ul>
 */
public class PropertyMetadataTest {

    /** The constraint facets added on top of the original presence-only {@code required}. */
    private static final List<String> FACETS =
        Arrays.asList("minLength", "maxLength", "pattern", "allowedValues", "minimum", "maximum");

    @Test
    public void facetsAreResolvableAsPropertyMetadata() {
        for (String facet : FACETS) {
            PropertyMetadata resolved =
                assertDoesNotThrow(() -> PropertyMetadata.valueOf(toUpperUnderscore(facet)),
                    facet + " is not a PropertyMetadata constant - MoxyStrategy would throw"
                        + " IllegalArgumentException on the first introspection of a node type"
                        + " using it");
            assertEquals(facet, resolved.toString());
        }
    }

    @Test
    public void facetsAreMetadataAnnotationMembers() throws Exception {
        for (String facet : FACETS) {
            assertEquals(String.class, Metadata.class.getDeclaredMethod(facet).getReturnType(),
                facet + " must be a String member of @Metadata, otherwise the generated XSD that"
                    + " carries it fails the xjc compile");
        }
    }

    @Test
    public void allFacetsAreDeclared() {
        List<String> declared =
            Arrays.stream(PropertyMetadata.values()).map(PropertyMetadata::toString).toList();
        assertTrue(declared.containsAll(FACETS),
            "expected all constraint facets in PropertyMetadata, found " + declared);
    }

    /**
     * The conversion {@code MoxyStrategy.getPropertyMetadata} performs on every mapping property
     * name before {@code PropertyMetadata.valueOf} (Guava's
     * {@code CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, key)}), reimplemented here so
     * this module keeps its dependency-free test scope.
     */
    private static String toUpperUnderscore(String lowerCamel) {
        return lowerCamel.replaceAll("([A-Z])", "_$1").toUpperCase();
    }
}
