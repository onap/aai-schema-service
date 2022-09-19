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
