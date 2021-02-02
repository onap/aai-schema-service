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
package org.onap.aai.schema;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

public class ValidateEdgeRulesTest {

    private static final String DBEDGERULES_RULES = "rules";
    private static final String DBEDGERULES_FROM = "from";
    private static final String DBEDGERULES_TO = "to";
    private static final String DBEDGERULES_DIRECTION = "direction";
    private static final String DBEDGERULES_LABEL = "label";
    private static final String DBEDGERULES_CONTAINS_OTHER_V = "contains-other-v";
    private static final String DBEDGERULES_OUT = "OUT";
    private static final String DBEDGERULES_IN = "IN";

    @Test
    public void testOnlyOneDefaultPerEdgeRuleBetweenTwoNodetypes() throws IOException, ParseException {
        Path currentRelativePath = Paths.get("../aai-schema/src/main/resources/").toAbsolutePath();
        List<File> subDirs = Arrays.asList(currentRelativePath.toFile().listFiles(File::isDirectory));
        List<String> multipleDefaultsPerList = new ArrayList<>();
        for (File subDir : subDirs) {

            String edgeRule = subDir.getAbsolutePath() + "/dbedgerules";
            File[] edgeRules = new File(edgeRule).listFiles(File::isDirectory);
            List<String> dbEdgeRulesList = Arrays.stream(edgeRules).map(File::getAbsolutePath).collect(Collectors.toList());


            List<File> dbEdgeRulesFileList = new ArrayList<>();

            dbEdgeRulesList.forEach(s ->
                FileUtils.listFiles(new File(s), new RegexFileFilter(".*\\.json"), DirectoryFileFilter.DIRECTORY)
                    .stream()
                    .filter(file -> file.getAbsolutePath().contains("DbEdgeRules"))
                    .forEach(dbEdgeRulesFileList::add));

            JSONParser jsonParser = new JSONParser();

            for (File file : dbEdgeRulesFileList) {
                FileReader reader = new FileReader(file);
                // Map the dbEdgeRules json file into a HashMap for reference
                Map<String, Integer> dbEdgeRules = new HashMap<>();
                // Read JSON file. Expecting JSON file to read an object with a JSONArray names "rules"
                JSONObject jsonObj = (JSONObject) jsonParser.parse(reader);
                JSONArray rules = (JSONArray) jsonObj.get(DBEDGERULES_RULES);
                for (int i = 0; i < rules.size(); i++) {
                    JSONObject rule = (JSONObject) rules.get(i);
                    String fromNode = rule.get(DBEDGERULES_FROM).toString();
                    String toNode = rule.get(DBEDGERULES_TO).toString();
                    String direction = rule.get(DBEDGERULES_DIRECTION).toString();
                    String label = rule.get(DBEDGERULES_LABEL).toString();
                    String containsOtherV = rule.get(DBEDGERULES_CONTAINS_OTHER_V).toString();
                    String isDefault = (rule.get("default") != null) ? rule.get("default").toString() : "false";

                    // special case - cvlan-tag should be replaced with cvlan-tag-entry
                    if (fromNode.equals("cvlan-tag"))
                        fromNode = "cvlan-tag-entry";
                    if (toNode.equals("cvlan-tag"))
                        toNode = "cvlan-tag-entry";
                    if (containsOtherV.equals("!${direction}")) {
                        if (direction.equals(DBEDGERULES_IN)) {
                            direction = DBEDGERULES_OUT;
                        } else if (direction.equals(DBEDGERULES_OUT)) {
                            direction = DBEDGERULES_IN;
                        }
                    }
                    dbEdgeRulesMapPut(dbEdgeRules, fromNode, toNode, direction, label, isDefault);
                }

                for(Map.Entry<String, Integer> entry : dbEdgeRules.entrySet()){
                    String key = entry.getKey();
                    if(entry.getValue() > 1){
                        multipleDefaultsPerList.add("\n" + file.getAbsoluteFile() + " " + key + " count: " + entry.getValue());
                    }
                }
            }

        }

        if(!multipleDefaultsPerList.isEmpty()){
            fail(multipleDefaultsPerList.stream().collect(Collectors.joining()));
        }
    }

    /**
     * Creating a hashmap to map what child nodes are associated to which parent nodes
     * according to the dbEdgeRules json files. A HashMap was chosen for the value of the map for O(1) lookup time.
     * @param from this variable will act as the key or value depending on the direction
     * @param to this variable will act as the key or value depending on the direction
     * @param direction dictates the direction of which vertex is dependent on which
     * @return The map returned will act as a dictionary to keep track of the parent nodes. Value of the map is a hashmap to help handle collision of multiple children to one parent
     */
    private Map<String, Integer> dbEdgeRulesMapPut(Map<String, Integer> dbEdgeRules, String from, String to, String direction, String label, String isDefault) {
        if (isStringEmpty(from) || isStringEmpty(to) || isStringEmpty(direction))
            return dbEdgeRules;


        String temp;

        if(from.compareTo(to) > 0){
            temp = from;
            from = to;
            to = temp;
        }

        String edgeInfo = String.format("%s%s%s[%s]%s", from, ((direction.equals("OUT")) ? "->": "<-"), to, label, isDefault);

        if ("false".equals(isDefault)) {
            return dbEdgeRules;
        }

        if(dbEdgeRules.containsKey(edgeInfo)){
            dbEdgeRules.put(edgeInfo, dbEdgeRules.get(edgeInfo) + 1);
        } else {
            dbEdgeRules.put(edgeInfo, 1);
        }

        return dbEdgeRules;
    }

    private boolean isStringEmpty(String s) {
        return (s == null || s.isEmpty()) ? true : false;
    }
}
