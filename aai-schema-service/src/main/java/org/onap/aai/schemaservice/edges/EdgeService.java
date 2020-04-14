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
package org.onap.aai.schemaservice.edges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.onap.aai.schemaservice.nodeschema.SchemaVersion;
import org.onap.aai.schemaservice.nodeschema.SchemaVersions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EdgeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeService.class);

    private static final String FILESEP = System.getProperty("file.separator");

    private SchemaVersions schemaVersions;
    private String edgesLocation;

    private Map<String, EdgeRules> rulesMap;

    @Autowired
    public EdgeService(SchemaVersions schemaVersions,
                       @Value("${schema.edges.location}") String edgesLocation){
        this.schemaVersions = schemaVersions;
        this.edgesLocation  = edgesLocation;
        this.rulesMap       = new HashMap<>();
    }

    @PostConstruct
    public void initialize() throws IOException {

        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();

        for (SchemaVersion schemaVersion : schemaVersions.getVersions()) {

            String edgeRuleVersionPath = edgesLocation + FILESEP + schemaVersion.toString();

            LOGGER.debug("For the version {} looking for edge rules in folder {}", schemaVersion, edgeRuleVersionPath);

            try (Stream<Path> pathStream = Files.walk(Paths.get(edgeRuleVersionPath))){

                List<Path> jsonFiles = pathStream
                    .filter((path) -> path.toString().endsWith(".json"))
                    .collect(Collectors.toList());

                if(jsonFiles.isEmpty()){
                    LOGGER.error("Unable to find any edge rules json files in folder {}", edgeRuleVersionPath);
                } else {
                    LOGGER.trace("Found the following edge rules {}", jsonFiles);
                }

                List<EdgeRule> rules = new ArrayList<>();
                for(Path path : jsonFiles){
                    File edgeRuleFile = path.toFile();
                    try (JsonReader jsonReader = new JsonReader(new FileReader(edgeRuleFile))){
                        EdgeRules edgeRules = gson.fromJson(jsonReader, EdgeRules.class);
                        rules.addAll(edgeRules.getRules());
                    }
                }

                rulesMap.put(schemaVersion.toString(), new EdgeRules(rules));
            }
        }

    }

    public Optional<EdgeRules> findRules(String version){
        return Optional.ofNullable(rulesMap.get(version));
    }
}
