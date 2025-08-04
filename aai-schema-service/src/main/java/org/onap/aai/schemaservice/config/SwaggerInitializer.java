package org.onap.aai.schemaservice.config;

import io.swagger.v3.jaxrs2.ReaderListener;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;

import javax.annotation.PostConstruct;
import javax.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Provider
@Component
@OpenAPIDefinition(info = @Info(title = "AAI Schema Service API", version = "1.0", description = "AAI Schema Service API"))
public class SwaggerInitializer implements ReaderListener {

    private static final Logger log = Logger.getLogger(SwaggerInitializer.class.getName());

    @Value("${schema.uri.base.path}")
    private String schemaBasePath;

    @PostConstruct
    public void initSwagger() {
        try {
            SwaggerConfiguration config = new SwaggerConfiguration()
                    .resourcePackages(Set.of("org.onap.aai.schemaservice"))
                    .prettyPrint(true)
                    .cacheTTL(0L);

            new JaxrsOpenApiContextBuilder<>()
                    .openApiConfiguration(config)
                    .buildContext(true)
                    .init();

            log.info("Swagger initialized with base path: " + schemaBasePath);

        } catch (Exception e) {
            log.severe("Failed to initialize Swagger: " + e.getMessage());
        }
    }

    @Override
    public void beforeScan(OpenApiReader reader, OpenAPI openAPI) {
        log.info("Swagger path in beforeScan : " + schemaBasePath);

        String effectiveBasePath = (schemaBasePath == null || schemaBasePath.isBlank())
                ? "/aai/schema-service"
                : schemaBasePath;
        openAPI.setServers(List.of(new Server()
                .url(effectiveBasePath)
                .description("Base path")));
    }

    @Override
    public void afterScan(OpenApiReader reader, OpenAPI openAPI) {
        Parameter fromAppId = new Parameter()
                .in("header")
                .name("X-FromAppId")
                .required(true)
                .description("ID of the calling application")
                .example("aai-schema-client");

        Parameter transactionId = new Parameter()
                .in("header")
                .name("X-TransactionId")
                .required(true)
                .description("Transaction ID for tracing")
                .example("1234567890");

        if (openAPI.getPaths() != null) {
            openAPI.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                List<Parameter> parameters = operation.getParameters();
                if (parameters != null) {
                    boolean hasFromAppId = parameters.stream().anyMatch(p -> "X-FromAppId".equals(p.getName()));
                    boolean hasTransactionId = parameters.stream().anyMatch(p -> "X-TransactionId".equals(p.getName()));

                    if (!hasFromAppId)
                        parameters.add(fromAppId);
                    if (!hasTransactionId)
                        parameters.add(transactionId);
                } else {
                    operation.addParametersItem(fromAppId);
                    operation.addParametersItem(transactionId);
                }
            }));
        }
    }
}