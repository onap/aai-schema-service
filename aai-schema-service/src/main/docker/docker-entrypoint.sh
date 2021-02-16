###
# ============LICENSE_START=======================================================
# org.onap.aai
# ================================================================================
# Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================
###

APP_HOME=/opt/app/aai-schema-service;
RESOURCES_HOME=${APP_HOME}/resources/;

export SERVER_PORT=${SERVER_PORT:-8452};

if [[ ! -h "${APP_HOME}/scripts" ]]; then

  ln -s ${APP_HOME}/bin ${APP_HOME}/scripts;
  ln -s /opt/aai/logroot/AAI-SS ${APP_HOME}/logs;

fi

scriptName=$1;

if [ ! -z $scriptName ]; then

    if [ -f ${APP_HOME}/bin/${scriptName} ]; then
        shift 1;
        ${APP_HOME}/bin/${scriptName} "$@" || {
            echo "Failed to run the ${scriptName}";
            exit 1;
        }
    else
        echo "Unable to find the script ${scriptName} in ${APP_HOME}/bin";
        exit 1;
    fi;

    exit 0;
fi;

mkdir -p /opt/app/aai-schema-service/logs/gc
mkdir -p /opt/app/aai-schema-service/logs/heap-dumps

if [ -f ${APP_HOME}/resources/aai-schema-service-swm-vars.sh ]; then
    source ${APP_HOME}/resources/aai-schema-service-swm-vars.sh;
fi;

JAVA_CMD="exec java";

JVM_OPTS="${PRE_JVM_ARGS}";
JVM_OPTS="${JVM_OPTS} -Dsun.net.inetaddr.ttl=180";
JVM_OPTS="${JVM_OPTS} ${POST_JVM_ARGS}";
JAVA_OPTS="${PRE_JAVA_OPTS} -DAJSC_HOME=$APP_HOME";
JAVA_OPTS="${JAVA_OPTS} -Dserver.port=${SERVER_PORT}";
JAVA_OPTS="${JAVA_OPTS} -DBUNDLECONFIG_DIR=./resources";
JAVA_OPTS="${JAVA_OPTS} -Dserver.local.startpath=${RESOURCES_HOME}";
JAVA_OPTS="${JAVA_OPTS} -DAAI_CHEF_ENV=${AAI_CHEF_ENV}";
JAVA_OPTS="${JAVA_OPTS} -DSCLD_ENV=${SCLD_ENV}";
JAVA_OPTS="${JAVA_OPTS} -DAFT_ENVIRONMENT=${AFT_ENVIRONMENT}";
JAVA_OPTS="${JAVA_OPTS} -DlrmName=com.att.ajsc.aai-schema-service";
JAVA_OPTS="${JAVA_OPTS} -DAAI_BUILD_VERSION=${AAI_BUILD_VERSION}";
JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom";
JAVA_OPTS="${JAVA_OPTS} -Dlogback.configurationFile=./resources/logback.xml";
JAVA_OPTS="${JAVA_OPTS} -Dloader.path=$APP_HOME/resources";
JAVA_OPTS="${JAVA_OPTS} -Dgroovy.use.classvalue=true";
JAVA_OPTS="${JAVA_OPTS} ${POST_JAVA_OPTS}";

JAVA_MAIN_JAR=$(ls lib/aai-schema-service*.jar);

${JAVA_CMD} ${JVM_OPTS} ${JAVA_OPTS} -jar ${JAVA_MAIN_JAR};
