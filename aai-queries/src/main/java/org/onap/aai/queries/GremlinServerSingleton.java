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

package org.onap.aai.queries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import javax.annotation.PostConstruct;

import org.onap.aai.logging.LogFormatTools;
import org.onap.aai.util.AAIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class GremlinServerSingleton {

    private static Logger logger = LoggerFactory.getLogger(GremlinServerSingleton.class);

    private boolean timerSet;
    private Timer timer;

    private GetCustomQueryConfig queryConfig;

    @Value("${schema.queries.location}")
    private String storedQueriesLocation;

    /**
     * Initializes the gremlin server singleton
     * Loads the configuration of the gremlin server and creates a cluster
     * Loads the gremlin query file into the properties object
     * Then creates a file watcher to watch the file every ten seconds
     * and if there is a change in the file, then reloads the file into
     * the properties object
     *
     */
    @PostConstruct
    public void init() {

        try {
            String filepath =
                storedQueriesLocation + AAIConstants.AAI_FILESEP + "stored-queries.json";
            Path path = Paths.get(filepath);
            String customQueryConfigJson = new String(Files.readAllBytes(path));

            queryConfig = new GetCustomQueryConfig(customQueryConfigJson);
        } catch (IOException e) {
            logger.error("Error occurred during the processing of query json file: "
                + LogFormatTools.getStackTop(e));
        }

    }

    /**
     * Gets the query using CustomQueryConfig
     *
     * @param key the config key
     * @return the stored query
     */
    public String getStoredQueryFromConfig(String key) {
        CustomQueryConfig customQueryConfig = queryConfig.getStoredQuery(key);
        if (customQueryConfig == null) {
            return null;
        }
        return customQueryConfig.getQuery();
    }

    public CustomQueryConfig getCustomQueryConfig(String key) {
        return queryConfig.getStoredQuery(key);
    }

}
