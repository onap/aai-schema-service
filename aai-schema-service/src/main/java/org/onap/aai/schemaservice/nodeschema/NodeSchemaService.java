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

package org.onap.aai.schemaservice.nodeschema;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.onap.aai.logging.LogFormatTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NodeSchemaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeSchemaResource.class);

    private SchemaVersions schemaVersions;

    private NodeIngestor nodeIngestor;

    private Map<String, String> versionMap = new HashMap<>();

    public NodeSchemaService(NodeIngestor nodeIngestor, SchemaVersions schemaVersions) {
        this.nodeIngestor = nodeIngestor;
        this.schemaVersions = schemaVersions;
    }

    @PostConstruct
    public void initialize() {

        schemaVersions.getVersions().forEach((schemaVersion -> {

            TransformerFactory tf = TransformerFactory.newInstance();

            Transformer transformer = null;
            try {
                transformer = tf.newTransformer();
            } catch (TransformerConfigurationException e) {
                LOGGER.warn("Encountered an transformer configuration exception"
                    + "during node schema service startup ", LogFormatTools.getStackTop(e));
            }
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try {
                DOMSource domSource = new DOMSource(nodeIngestor.getSchema(schemaVersion));

                StreamResult streamResult =
                    new StreamResult(new OutputStreamWriter(buffer, "UTF-8"));
                transformer.transform(domSource, streamResult);
                versionMap.put(schemaVersion.toString(), buffer.toString("UTF-8"));
            } catch (TransformerException | UnsupportedEncodingException e) {
                LOGGER.warn("Encountered an transformer or unsupported encoding exception "
                    + "during node schema service startup ", LogFormatTools.getStackTop(e));
            }

        }));
    }

    public Optional<String> fetch(String version) {
        return Optional.ofNullable(versionMap.get(version));
    }

}
