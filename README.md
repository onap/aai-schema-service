# AAI Schema Service

## Development

### Local setup
In order to start the service locally, here is what needs to be done
``` bash
mvn clean install
```

Above command only needs to be run the first time.

The command below actually starts the microservice
``` bash
mvn -pl aai-schema-service -PrunAjsc
```

### Run code formatter
``` bash
mvn formatter:format spotless:apply process-sources
```

## Adjusting the schema

This repo contains the schema that is used throughout AAI. The whole project is based on
[EclipseLink MOXy](https://eclipse.dev/eclipselink/documentation/2.7/moxy/runtime003.htm) to load object representations of the schema that is defined in `./aai-schema/src/main/resources/onap/oxm` into memory during the runtime.

Based on these files, artifacts for other tools are generated in this project as well.
The `oxm/aai_oxm_vX.xml` files are converted to `aai_schema/aai_schema_vXX.xsd`'s that can be consumed by
jaxb and to `aai_swagger_yaml/aai_swagger_vXX.yaml`'s that can be used for OpenApi generation.

Having all this in mind, the schema can be adjusted by adjusting the `*.xsd` definitions to contain further endpoints
and/or new objects.
Running a `mvn install` will then trigger the `exec-maven-plugin` to invoke the `org.onap.aai.schemagen.GenerateXsd` class to generate the derived artifacts described above.
