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
package org.onap.aai.schemaservice.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class QueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryService.class);

    private String queryLocation;

    private String storedQueriesContent;

    public QueryService(@Value("${schema.query.location}") String queryLocation){
        this.queryLocation = queryLocation;
    }

    @PostConstruct
    public void initialize() throws IOException {

        String fileName = queryLocation + File.separator + "stored-queries.json";
        LOGGER.debug("Loading the following stored queries file {}", fileName);

        StringBuilder contentBuilder = new StringBuilder();

        try(Stream<String> stream = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)){
            stream.forEach(s -> contentBuilder.append(s));
        }

        storedQueriesContent = contentBuilder.toString();

        LOGGER.trace("Contents of the stored query file {}", storedQueriesContent);
    }

    public String getStoredQueries(){
        return storedQueriesContent;
    }
}
