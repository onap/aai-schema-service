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

package org.onap.aai.schemagen.genxsd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Arrays;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeleteFootnoteSetTest {
    String targetNode;

    DeleteFootnoteSet footnotes = null;

    public static Collection<String[]> testConditions() {
        String inputs[][] = {
            {"vserver", "(1)", "\n      -(1) IF this VSERVER node is deleted, this FROM node is DELETED also\n"},
            {"ctag-pool", "(2)", "\n      -(2) IF this CTAG-POOL node is deleted, this TO node is DELETED also\n"},
            {"pserver", "(3)", "\n      -(3) IF this FROM node is deleted, this PSERVER is DELETED also\n"},
            {"oam-network", "(4)", "\n      -(4) IF this TO node is deleted, this OAM-NETWORK is DELETED also\n"},
            {"dvs-switch", "(1)", "\n      -(1) IF this DVS-SWITCH node is deleted, this FROM node is DELETED also\n"},
            {"availability-zone", "(3)", "\n      -(3) IF this FROM node is deleted, this AVAILABILITY-ZONE is DELETED also\n"}};
        return Arrays.asList(inputs);
    }

    @BeforeEach
    public void setUp() throws Exception {
        footnotes = new DeleteFootnoteSet(this.targetNode);
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testDeleteFootnoteSet(String targetNode, String flavor, String result) {
        DeleteFootnoteSet footnoteSet = new DeleteFootnoteSet(targetNode);
        assertEquals(targetNode, footnoteSet.targetNode);
        // initDeleteFootnoteSetTest(targetNode, flavor, result);
        // assertThat(footnotes.targetNode, is(this.targetNode));
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testAddCondition4(String targetNode, String flavor, String result) {
        DeleteFootnoteSet footnoteSet = new DeleteFootnoteSet(targetNode);
        footnoteSet.add("(4)");
        assertEquals(1, footnoteSet.footnotes.size());
        assertEquals("\n      -(4) IF this TO node is deleted, this " + targetNode.toUpperCase() + " is DELETED also\n",
            footnoteSet.toString());
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testAddTargetNodeInString(String targetNode, String flavor, String result) {
        DeleteFootnoteSet footnoteSet = new DeleteFootnoteSet(targetNode);
        footnoteSet.add(targetNode.toUpperCase()); // Test string containing target node
        assertEquals(1, footnoteSet.footnotes.size());
        assertEquals("\n      -" + targetNode.toUpperCase() + "\n", footnoteSet.toString());
    }

    @ParameterizedTest
    @MethodSource("testConditions")
    public void testAddNoMatchingCondition(String targetNode, String flavor, String result) {
        DeleteFootnoteSet footnoteSet = new DeleteFootnoteSet(targetNode);
        footnoteSet.add("non-matching condition"); // Test string that doesn't match any condition
        assertEquals(0, footnoteSet.footnotes.size());
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToString(String targetNode, String flavor, String result) {
        DeleteFootnoteSet footnoteSet = new DeleteFootnoteSet(targetNode);
        footnoteSet.add(flavor);
        assertEquals(result, footnoteSet.toString());
    }

    @MethodSource("testConditions")
    @ParameterizedTest
    public void testToStringWithFootnotes(String targetNode, String flavor, String result) {
        DeleteFootnoteSet footnoteSet = new DeleteFootnoteSet(targetNode);
        footnoteSet.add(flavor);  // This adds a footnote
        // Assert that footnote is properly added with the format
        assertEquals(result, footnoteSet.toString());
    }

    @ParameterizedTest
    @MethodSource("testConditions")
    public void testToStringEmptyFootnotes(String targetNode, String flavor, String result) {
        DeleteFootnoteSet footnoteSet = new DeleteFootnoteSet(targetNode);
        // No footnotes added, so the output should be empty
        assertEquals("\n", footnoteSet.toString());
    }
}
