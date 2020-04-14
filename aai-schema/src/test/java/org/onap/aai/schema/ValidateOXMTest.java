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


import javafx.beans.binding.BooleanBinding;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.Test;
import org.junit.Ignore;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ValidateOXMTest {

    private String DBEDGERULES_RULES = "rules";
    private String DBEDGERULES_FROM = "from";
    private String DBEDGERULES_TO = "to";
    private String DBEDGERULES_DIRECTION = "direction";
    private String DBEDGERULES_CONTAINS_OTHER_V = "contains-other-v";
    private String DBEDGERULES_OUT = "OUT";
    private String DBEDGERULES_IN = "IN";
    private String XMLROOTELEMENT = "xml-root-element";
    private String ECOMP = "ecomp";
    private String NARAD = "narad";
    private String ONAP = "onap";

    @Test
    public void testFindXmlPropContainingSpace() throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {
        boolean foundIssue = false;
        List<File> fileList = getLatestFiles();

        StringBuilder msg = new StringBuilder();
        for (File file : fileList) {
            msg.append(file.getAbsolutePath().replaceAll(".*aai-schema", ""));
            msg.append("\n");
            Document xmlDocument = getDocument(file);
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "/xml-bindings/java-types/java-type/xml-properties/xml-property[@name!='description' and contains(@value,' ')]";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                foundIssue = true;
                msg.append("\t");
                msg.append(nodeList.item(i).getParentNode().getParentNode().getAttributes().getNamedItem("name").getNodeValue());
                msg.append("\n");
                msg.append("\t");
                msg.append("\n");
            }

        }

        if (foundIssue) {
            System.out.println(msg.toString());
            fail("Node type xml-property should have space.");
        }

    }

    /**
     * Verifies that all of the node types in the oxm's have their uri templates.
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    @Test
    public void allNodeTypesHaveAAIUriTemplate() throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {
        boolean foundIssue = false;
        List<File> fileList = getFiles();

        StringBuilder msg = new StringBuilder();
        for (File file : fileList) {
            msg.append(file.getAbsolutePath().replaceAll(".*aai-schema", ""));
            msg.append("\n");
            Document xmlDocument = getDocument(file);
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "/xml-bindings/java-types/java-type[" +
                "(" +
                "count(xml-properties/xml-property[@name='container']) > 0 " +
                "or count(xml-properties/xml-property[@name='dependentOn']) > 0" +
                ") " +
                "and count(xml-properties/xml-property[@name='uriTemplate']) = 0 " +
                "]";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                String name = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                if (name.equals("InstanceFilter") || name.equals("InventoryResponseItems") || name.equals("InventoryResponseItem")) {
                    continue;
                }
                foundIssue = true;
                msg.append("\t");
                msg.append(name);
                msg.append("\n");
            }
        }
        if (foundIssue) {
            System.out.println(msg.toString());
            fail("Missing uriTemplate in oxm.");
        }

    }


    /**
     * Verifies that all specified properties are indexed
     * Currently set to check that "model-invariant-id","model-version-id" which are aliased are indexed
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    @Test
    public void aliasedIndexedPropsAreInIndexedListWithPropName() throws XPathExpressionException, IOException, SAXException, ParserConfigurationException {

        final List<String> props = Arrays.asList("model-invariant-id","model-version-id");

        boolean foundIssue = false;
        List<File> fileList = getLatestFiles();
        StringBuilder msg = new StringBuilder();

        for (File file : fileList) {
            msg.append(file.getAbsolutePath().replaceAll(".*aai-schema", ""));
            msg.append("\n");
            for (String prop : props) {
                Document xmlDocument = getDocument(file);
                XPath xPath = XPathFactory.newInstance().newXPath();
                String expression = "/xml-bindings/java-types/java-type[" +
                    "(" +
                    "count(xml-properties/xml-property[@name='container']) > 0 " +
                    "or count(xml-properties/xml-property[@name='dependentOn']) > 0" +
                    ") " +
                    "and count(xml-properties/xml-property[@name='indexedProps' and not(contains(@value,'" + prop + "'))]) > 0 " + //prop is not in indexed props list
                    "and count(java-attributes/xml-element[@name='" + prop + "']) > 0 " + // prop is a property on obj
                    "]";

                NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

                if (nodeList.getLength() > 0) {
                    msg.append("\t")
                        .append(prop)
                        .append("\n");
                }
                for (int i = 0; i < nodeList.getLength(); i++) {
                    String name = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                    if (name.equals("InstanceFilter") || name.equals("InventoryResponseItems") || name.equals("InventoryResponseItem")) {
                        continue;
                    }
                    foundIssue = true;
                    msg.append("\t\t")
                        .append(name)
                        .append("\n");
                }
            }
        }

        if (foundIssue) {
            System.out.println(msg.toString());
            fail("Missing index entry in oxm.");
        }

    }

    /**
     * Check schema versions against their respective dbEdgeRules file and check if the
     * dependentOn relationship matches what is listed in the edge rules.
     *
     */
    @Test
    @Ignore
    public void testSchemaValidationAgainstEdgeRules() throws  XPathExpressionException, IOException, SAXException, ParserConfigurationException, ParseException {
        Path currentRelativePath = Paths.get("../aai-schema/src/main/resources/").toAbsolutePath();
        List<File> subDirs = Arrays.asList(currentRelativePath.toFile().listFiles(File::isDirectory));
        boolean success = true;
        for (File subDir : subDirs) {
            List<String> oxmSchemaList = new ArrayList<>();
            List<String> dbEdgeRulesList = new ArrayList<>();
            String oxm = subDir.getAbsolutePath() + "/oxm";
            File[] oxms = new File(oxm).listFiles(File::isDirectory);
            Arrays.stream(oxms).map(File::getAbsolutePath).max(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return extractInt(o1) - extractInt(o2);
                }

                int extractInt(String s) {
                    String num = s.replaceAll("\\D", "");
                    return num.isEmpty() ? 0 : Integer.parseInt(num);
                }
            }).ifPresent(oxmSchemaList::add);

            String edgeRule = subDir.getAbsolutePath() + "/dbedgerules";
            File[] edgeRules = new File(edgeRule).listFiles(File::isDirectory);
            Arrays.stream(edgeRules).map(File::getAbsolutePath).max(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return extractInt(o1) - extractInt(o2);
                }

                int extractInt(String s) {
                    String num = s.replaceAll("\\D", "");
                    return num.isEmpty() ? 0 : Integer.parseInt(num);
                }
            }).ifPresent(dbEdgeRulesList::add);


            List<File> oxmSchemaFileList = new ArrayList<>();
            List<File> dbEdgeRulesFileList = new ArrayList<>();
            oxmSchemaList.forEach(s ->
                FileUtils.listFiles(
                    new File(s),
                    new RegexFileFilter(".*\\.xml"),
                    DirectoryFileFilter.DIRECTORY)
                    .stream()
                    .filter(file -> file.getAbsolutePath().contains("oxm"))
                    .forEach(oxmSchemaFileList::add));

            dbEdgeRulesList.forEach(s ->
                FileUtils.listFiles(
                    new File(s),
                    new RegexFileFilter(".*\\.json"),
                    DirectoryFileFilter.DIRECTORY)
                    .stream()
                    .filter(file -> file.getAbsolutePath().contains("DbEdgeRules"))
                    .forEach(dbEdgeRulesFileList::add));

            // Map the dbEdgeRules json file into a HashMap for reference
            Map<String, Set<String>> dbEdgeRules = new HashMap<>();
            JSONParser jsonParser = new JSONParser();
            for (File file : dbEdgeRulesFileList) {
                FileReader reader = new FileReader(file);
                // Read JSON file. Expecting JSON file to read an object with a JSONArray names "rules"
                JSONObject jsonObj = (JSONObject) jsonParser.parse(reader);
                JSONArray rules = (JSONArray) jsonObj.get(DBEDGERULES_RULES);
                for (int i = 0; i < rules.size(); i++) {
                    JSONObject rule = (JSONObject) rules.get(i);
                    String fromNode = rule.get(DBEDGERULES_FROM).toString();
                    String toNode = rule.get(DBEDGERULES_TO).toString();
                    String direction = rule.get(DBEDGERULES_DIRECTION).toString();
                    String containsOtherV = rule.get(DBEDGERULES_CONTAINS_OTHER_V).toString();

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
                    // If this value is none, the edge rule is for cousin nodes. Ignore.
                    else if (containsOtherV.equals("NONE"))
                        continue;
                    dbEdgeRulesMapPut(dbEdgeRules, fromNode, toNode, direction);
                }
            }

            // Iterate through the most recent oxm schema files. Map the parent child relationships
            Map<String, Set<String>> oxmSchemaFile = new HashMap<>();
            for (File file : oxmSchemaFileList) {
                Document xmlDocument = getDocument(file);
                XPath xPath = XPathFactory.newInstance().newXPath();
                String parentNodeExpression = "/xml-bindings/java-types/java-type/xml-properties/xml-property[@name='dependentOn']";
                NodeList parentNodeList = (NodeList) xPath.compile(parentNodeExpression).evaluate(xmlDocument, XPathConstants.NODESET);
                String childNodeExpression = "/xml-bindings/java-types/java-type[" +
                    "(" +
                    "count(xml-properties/xml-property[@name='dependentOn']) > 0" +
                    ")]";
                NodeList childNodeList = (NodeList) xPath.compile(childNodeExpression).evaluate(xmlDocument, XPathConstants.NODESET);

                for (int i = 0; i < parentNodeList.getLength(); i++) {

                    // Obtain the xml-root-element field by tracing the childNodes from the java-type parent node
                    for (int j = 0; j < childNodeList.item(i).getChildNodes().getLength(); j++) {
                        if (childNodeList.item(i).getChildNodes().item(j).getNodeName().equals(XMLROOTELEMENT)) {

                            // The parent node
                            String dependentOn = parentNodeList.item(i).getAttributes().getNamedItem("value").getNodeValue();

                            // The child node
                            String xmlRootElement = childNodeList.item(i).getChildNodes().item(j).getAttributes().getNamedItem("name").getNodeValue();

                            Set<String> childSet;
                            String[] parents = dependentOn.split(",");
                            for (int k = 0; k < parents.length; k++) {
                                String parent = parents[k];
                                if (oxmSchemaFile.containsKey(parent)) {
                                    childSet = oxmSchemaFile.get(parent);
                                } else {
                                    childSet = new HashSet<>();
                                }
                                childSet.add(xmlRootElement);
                                oxmSchemaFile.put(parent, childSet);
                            }

                        }
                    }
                }
            }

            // Compare the OXM file against the dbEdgeRules file. check what is missing in dbEdgeRules from the oxm files.
            Set<String> oxmKeySet = oxmSchemaFile.keySet();
            for (String key : oxmKeySet) {
                Set<String> oxmChildren = oxmSchemaFile.get(key);
                Set<String> dbEdgeRulesChildren = dbEdgeRules.get(key);

                // Check if the parent vertex exists at all in the dbEdgeRules file
                if (dbEdgeRulesChildren == null || dbEdgeRulesChildren.isEmpty()) {
                    for (String oxmChild : oxmChildren) {
                        System.out.println("ERROR: dbEdgeRules under directory '" + subDir.toString() + "' does not contain parent '" + key + "' and child '" + oxmChild + "' relationship");
                    }
                    success = false;
                    continue;
                }

                // Compare both parent-child relationships between both files
                if (!oxmChildren.equals(dbEdgeRulesChildren)) {
                    for (String oxmChild : oxmChildren) {
                        if (!dbEdgeRulesChildren.contains(oxmChild)) {
                            System.out.println("ERROR: dbEdgeRules under directory '" + subDir.toString() + "' does not contain parent '" + key + "' and child '" + oxmChild + "' relationship");
                            success = false;
                        }
                    }
                }
            }

            // Compare the dbEdgeRules against the OXM File
            Set<String> dbEdgeRuleKeySet = dbEdgeRules.keySet();
            for (String key : dbEdgeRuleKeySet) {
                Set<String> dbEdgeRulesChildren = dbEdgeRules.get(key);
                Set<String> oxmChildren = oxmSchemaFile.get(key);

                // Check if the parent vertex exists at all in the dbEdgeRules file
                if (oxmChildren == null || oxmChildren.isEmpty()) {
                    for (String dbEdgeRuleChild : dbEdgeRulesChildren) {
                        System.out.println("ERROR: oxms under directory '" + subDir.toString() + "' do not contain parent '" + key + "' and child '" + dbEdgeRuleChild + "' relationship");
                    }
                    success = false;
                    continue;
                }

                // Compare both parent-child relationships between both files
                if (!dbEdgeRulesChildren.equals(oxmChildren)) {
                    for (String dbEdgeRuleChild : dbEdgeRulesChildren) {
                        if (!oxmChildren.contains(dbEdgeRuleChild)) {
                            System.out.println("ERROR: oxms under directory '" + subDir.toString() + "' do not contain parent '" + key + "' and child '" + dbEdgeRuleChild + "' relationship");
                            success = false;
                        }
                    }
                }
            }
        }
        assertTrue(success);
    }

    /**
     * Null check for strings
     * @param s
     * @return
     */
    private boolean isStringEmpty(String s) {
        return (s == null || s.isEmpty()) ? true : false;
    }

    /**
     * Creating a hashmap to map what child nodes are associated to which parent nodes
     * according to the dbEdgeRules json files. A HashMap was chosen for the value of the map for O(1) lookup time.
     * @param from this variable will act as the key or value depending on the direction
     * @param to this variable will act as the key or value depending on the direction
     * @param direction dictates the direction of which vertex is dependent on which
     * @return The map returned will act as a dictionary to keep track of the parent nodes. Value of the map is a hashmap to help handle collision of multiple children to one parent
     */
    private Map<String, Set<String>> dbEdgeRulesMapPut(Map<String, Set<String>> dbEdgeRules, String from, String to, String direction) {
        if (isStringEmpty(from) || isStringEmpty(to) || isStringEmpty(direction))
            return dbEdgeRules;

        // Assigning the strings to parent and child for readability
        String parent = "", child = "";
        if (direction.equals(DBEDGERULES_OUT)) {
            parent = from;
            child = to;
        } else if (direction.equals(DBEDGERULES_IN)) {
            parent = to;
            child = from;
        }
        // Add to the dbEdgeRules mapping
        Set<String> children;
        if (!dbEdgeRules.containsKey(parent)) {
            children = new HashSet<>();
            children.add(child);
            dbEdgeRules.put(parent, children);
        } else {
            children = dbEdgeRules.get(parent);
            children.add(child);
            dbEdgeRules.put(parent, children);
        }
        return dbEdgeRules;
    }

    private List<File> getFiles() {
        Path currentRelativePath = Paths.get("../aai-schema/src/main/resources/").toAbsolutePath();
        return FileUtils.listFiles(
            currentRelativePath.toFile(),
            new RegexFileFilter(".*\\.xml"),
            DirectoryFileFilter.DIRECTORY)
            .stream()
            .filter(file -> file.getAbsolutePath().contains("oxm"))
            .filter(file -> !file.getAbsolutePath().contains("onap")) // skips onap for checks
            .collect(Collectors.toList());
    }

    private List<File> getAaiSchemaOxmFiles() {
        Path currentRelativePath = Paths.get("../aai-schema/src/main/resources/").toAbsolutePath();
        return FileUtils.listFiles(
            currentRelativePath.toFile(),
            new RegexFileFilter(".*\\.xml"),
            DirectoryFileFilter.DIRECTORY)
            .stream()
            .filter(file -> file.getAbsolutePath().contains("oxm"))
            .filter(file -> !file.getAbsolutePath().contains("onap")) // skips onap for checks
            .collect(Collectors.toList());
    }

    private List<File> getDbEdgeRulesFiles() {
        Path currentRelativePath = Paths.get("../aai-schema/src/main/resources/").toAbsolutePath();
        return FileUtils.listFiles(
            currentRelativePath.toFile(),
            new RegexFileFilter(".*\\.json"),
            DirectoryFileFilter.DIRECTORY)
            .stream()
            .filter(file -> file.getAbsolutePath().contains("DbEdgeRules"))
            .filter(file -> !file.getAbsolutePath().contains("onap")) // skips onap for checks
            .collect(Collectors.toList());
    }

    /**
     * Finds all of the oxm files for the latest version.
     * @return list of the latest version of the oxm files.
     */
    private List<File> getLatestDbEdgeRulesFiles(String fileDirectory) {
        List<String> latest = new ArrayList<>();
        String currentRelativePath = Paths.get("../aai-schema/src/main/resources/" + fileDirectory + "/dbedgerules").toAbsolutePath().toString();
        File[] oxms = new File(currentRelativePath).listFiles(File::isDirectory);
        Arrays.stream(oxms).map(File::getAbsolutePath).max(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return extractInt(o1) - extractInt(o2);
            }
            int extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        }).ifPresent(latest::add);

        List<File> latestFiles = new ArrayList<>();
        latest.forEach(s ->
            FileUtils.listFiles(
                new File(s),
                new RegexFileFilter(".*\\.json"),
                DirectoryFileFilter.DIRECTORY)
                .stream()
                .filter(file -> file.getAbsolutePath().contains("DbEdgeRules"))
                .forEach(latestFiles::add));

        return latestFiles;
    }

    /**
     * Finds all of the oxm files for the latest version.
     * @return list of the latest version of the oxm files.
     */
    private List<File> getLatestFiles() {
        List<String> latest = new ArrayList<>();
        Path currentRelativePath = Paths.get("../aai-schema/src/main/resources/").toAbsolutePath();
        List<File> subDirs = Arrays.asList(currentRelativePath.toFile().listFiles(File::isDirectory));
        for (File subDir : subDirs) {
            String oxm = subDir.getAbsolutePath() + "/oxm";
            File[] oxms = new File(oxm).listFiles(File::isDirectory);
            Arrays.stream(oxms).map(File::getAbsolutePath).max(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return extractInt(o1) - extractInt(o2);
                }
                int extractInt(String s) {
                    String num = s.replaceAll("\\D", "");
                    return num.isEmpty() ? 0 : Integer.parseInt(num);
                }
            }).ifPresent(latest::add);
        }

        List<File> latestFiles = new ArrayList<>();
        latest.forEach(s ->
            FileUtils.listFiles(
                new File(s),
                new RegexFileFilter(".*\\.xml"),
                DirectoryFileFilter.DIRECTORY)
                .stream()
                .filter(file -> file.getAbsolutePath().contains("oxm"))
                .forEach(latestFiles::add));

        return latestFiles;
    }

    /**
     * Finds all of the oxm files for the latest version.
     * @return list of the latest version of the oxm files.
     */
    private List<File> getLatestFiles(String fileDirectory) {
        List<String> latest = new ArrayList<>();
        String currentRelativePath = Paths.get("../aai-schema/src/main/resources/" + fileDirectory + "/oxm").toAbsolutePath().toString();
        File[] oxms = new File(currentRelativePath).listFiles(File::isDirectory);
        Arrays.stream(oxms).map(File::getAbsolutePath).max(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return extractInt(o1) - extractInt(o2);
            }
            int extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        }).ifPresent(latest::add);

        List<File> latestFiles = new ArrayList<>();
        latest.forEach(s ->
            FileUtils.listFiles(
                new File(s),
                new RegexFileFilter(".*\\.xml"),
                DirectoryFileFilter.DIRECTORY)
                .stream()
                .filter(file -> file.getAbsolutePath().contains("oxm"))
                .forEach(latestFiles::add));

        return latestFiles;
    }

    //TODO test that all oxm xml are valid xml



    public String printNodeList(NodeList nodeList, Document doc) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < nodeList.getLength(); i++) {
            stringBuilder.append(printNode(nodeList.item(i), doc)).append("\n");
        }
        return stringBuilder.toString();
    }

    public String printNode(Node node, Document document) throws IOException {
        StringWriter stringWriter = new StringWriter();
        return stringWriter.toString();

    }

    private Document getDocument(File file) throws ParserConfigurationException, SAXException, IOException {
        InputStream fileIS = new FileInputStream(file);
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        return builder.parse(fileIS);
    }

}
