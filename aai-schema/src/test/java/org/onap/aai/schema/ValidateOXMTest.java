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
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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

import static org.junit.Assert.fail;

public class ValidateOXMTest {

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
