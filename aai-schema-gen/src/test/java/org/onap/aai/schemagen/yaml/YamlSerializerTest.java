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

package org.onap.aai.schemagen.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The layout rules of the swagger documents, stated as tests.
 *
 * <p>
 * These are the rules that used to be spread over every emitter as indent literals and, after phase
 * two, as level arguments. Now they hold in one place, so this is where they are pinned - including
 * the two anomalies that only exist because the generated documents are not allowed to change yet.
 */
public class YamlSerializerTest {

    @Test
    public void scalarEntrySharesItsKeysLine() {
        YamlMapping mapping = new YamlMapping().entry("type", "string");

        assertEquals("type: string\n", YamlSerializer.serialize(mapping));
    }

    @Test
    public void nestedMappingIndentsOneLevelBelowItsKey() {
        YamlMapping mapping =
            new YamlMapping().entry("schema", new YamlMapping().entry("type", "string"));

        assertEquals("schema:\n  type: string\n", YamlSerializer.serialize(mapping));
    }

    @Test
    public void depthComesFromNestingAlone() {
        // the point of the tree: three levels of structure produce three levels of indentation
        // without anybody stating a number
        YamlMapping mapping = new YamlMapping().entry("paths",
            new YamlMapping().entry("/business/customers", new YamlMapping().entry("get",
                new YamlMapping().entry("operationId", "getBusinessCustomers"))));

        assertEquals("paths:\n" + "  /business/customers:\n" + "    get:\n"
            + "      operationId: getBusinessCustomers\n", YamlSerializer.serialize(mapping));
    }

    @Test
    public void sequenceIndicatorSitsOneLevelBelowItsKey() {
        // this is the rule stock snakeyaml will not produce: the "-" is indented past "produces:",
        // not aligned with it
        YamlMapping mapping = new YamlMapping().entry("produces",
            new YamlSequence().item("application/json").item("application/xml"));

        assertEquals("produces:\n" + "  - application/json\n" + "  - application/xml\n",
            YamlSerializer.serialize(mapping));
    }

    @Test
    public void mappingAsSequenceItemSharesTheIndicatorLine() {
        YamlMapping parameter =
            new YamlMapping().entry("name", "body").entry("in", "body").entry("required", "true");

        assertEquals("- name: body\n" + "  in: body\n" + "  required: true\n",
            YamlSerializer.serialize(new YamlSequence().item(parameter)));
    }

    @Test
    public void nestedValueOfASequenceItemIndentsFromTheItem() {
        YamlMapping parameter = new YamlMapping().entry("name", "body").entry("schema",
            new YamlMapping().entry("$ref", "\"#/definitions/pserver\""));

        assertEquals("- name: body\n" + "  schema:\n" + "    $ref: \"#/definitions/pserver\"\n",
            YamlSerializer.serialize(new YamlSequence().item(parameter)));
    }

    @Test
    public void sequenceItemMappingMustOpenWithAScalarEntry() {
        // the first key has to fit on the indicator line, so a nested value cannot come first
        YamlSequence sequence = new YamlSequence()
            .item(new YamlMapping().entry("schema", new YamlMapping().entry("type", "string")));

        assertThrows(IllegalArgumentException.class, () -> YamlSerializer.serialize(sequence));
    }

    @Test
    public void blockScalarContentIndentsOneLevelBelowItsKey() {
        YamlBlock description = new YamlBlock().line("Update an existing pserver").line("#")
            .line("Other differences between PUT and PATCH are:");

        assertEquals(
            "description: |\n" + "  Update an existing pserver\n" + "  #\n"
                + "  Other differences between PUT and PATCH are:\n",
            YamlSerializer.serialize(new YamlMapping().entry("description", description)));
    }

    @Test
    public void blockScalarTrailingTextIsEmittedVerbatim() {
        YamlBlock description =
            new YamlBlock().line("relationship-list").trailingRaw("      ###### Related Nodes\n");

        assertEquals("description: |\n" + "  relationship-list\n" + "      ###### Related Nodes\n",
            YamlSerializer.serialize(new YamlMapping().entry("description", description)));
    }

    @Test
    public void fragmentIsIndentedAndSuppliesItsOwnLineEnding() {
        YamlMapping mapping = new YamlMapping().entry("\"default\"",
            new YamlFragment("description: Response codes are uniform across all endpoints.\n"));

        assertEquals(
            "\"default\":\n" + "  description: Response codes are uniform across all endpoints.\n",
            YamlSerializer.serialize(mapping));
    }

    @Test
    public void aNullFragmentRendersAsTheFourCharactersNull() {
        // not a defect to fix here: getResponsesUrl() returns null when the yamlresponses_url
        // property is unset, and the checked-in documents contain "null" under every "default"
        // response as a result. Suppressing it would change 25 published documents.
        YamlMapping mapping = new YamlMapping().entry("\"default\"", new YamlFragment(null));

        assertEquals("\"default\":\n  null", YamlSerializer.serialize(mapping));
    }

    @Test
    public void rawTextIsNeitherIndentedNorTerminated() {
        // an inherited parameter list arrives already indented and without a final newline, so the
        // next sequence item continues on its last line - exactly as the current documents have it
        YamlMapping operation = new YamlMapping().entry("parameters",
            new YamlMapping().raw("        - name: vnf-id\n          example: __VNF-ID__")
                .raw("        - name: body\n"));

        assertEquals(
            "parameters:\n" + "        - name: vnf-id\n"
                + "          example: __VNF-ID__        - name: body\n",
            YamlSerializer.serialize(operation));
    }

    @Test
    public void extraDepthPushesAValueFurtherDown() {
        // the GET response $ref anomaly: two levels below "schema:" rather than one
        YamlMapping schema = new YamlMapping().entryAtShiftedDepth("schema",
            new YamlMapping().entry("$ref", "\"#/definitions/pserver\""), 1);

        assertEquals("schema:\n" + "    $ref: \"#/definitions/pserver\"\n",
            YamlSerializer.serialize(schema));
    }

    @Test
    public void aPaddedKeyKeepsItsTrailingSpaces() {
        // the items: anomaly of an array property
        YamlMapping mapping = new YamlMapping().entryWithPaddedKey("items",
            new YamlMapping().entry("$ref", "\"#/definitions/activity\""), 10);

        assertEquals("items:          \n  $ref: \"#/definitions/activity\"\n",
            YamlSerializer.serialize(mapping));
    }

    @Test
    public void aFragmentStartsAtTheGivenLevel() {
        // for the emitters that still return a string to be spliced into a larger document
        YamlMapping operation = new YamlMapping().entry("tags", new YamlSequence().item("Network"));

        assertEquals("      tags:\n        - Network\n", YamlSerializer.serialize(operation, 3));
    }

    @Test
    public void negativeLevelIsRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> YamlSerializer.serialize(new YamlMapping().entry("a", "b"), -1));
    }

    @Test
    public void repeatedKeysAreKeptInOrder() {
        // duplicate keys are invalid YAML but the generator emits them; order is what matters here
        YamlMapping mapping = new YamlMapping().entry("type", "string").entry("type", "integer");

        assertEquals("type: string\ntype: integer\n", YamlSerializer.serialize(mapping));
    }

    @Test
    public void emptyNodesReportThemselvesEmpty() {
        assertTrue(new YamlMapping().isEmpty());
        assertTrue(new YamlSequence().isEmpty());
        assertTrue(new YamlBlock().isEmpty());
        assertTrue(new YamlScalar("").isEmpty());
        assertTrue(new YamlRaw(null).isEmpty());
        assertTrue(new YamlFragment("").isEmpty());

        assertFalse(new YamlMapping().entry("a", "b").isEmpty());
        assertFalse(new YamlSequence().item("a").isEmpty());
        assertFalse(new YamlBlock().line("a").isEmpty());
        assertFalse(new YamlScalar("a").isEmpty());
        assertFalse(new YamlRaw("a").isEmpty());
        assertFalse(new YamlFragment("a").isEmpty());
    }

    @Test
    public void emptySequenceItemMappingEmitsNothing() {
        assertEquals("", YamlSerializer.serialize(new YamlSequence().item(new YamlMapping())));
    }
}
