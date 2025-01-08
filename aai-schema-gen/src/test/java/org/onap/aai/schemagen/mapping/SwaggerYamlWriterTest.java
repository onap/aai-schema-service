package org.onap.aai.schemagen.mapping;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.onap.aai.schemagen.model.swagger.GetOperation;
import org.onap.aai.schemagen.model.swagger.Path;
import org.onap.aai.schemagen.model.swagger.Paths;
import org.onap.aai.schemagen.model.swagger.Response;
import org.onap.aai.schemagen.model.swagger.SwaggerDocument;
import org.onap.aai.schemagen.model.swagger.SwaggerDocument.Info;
import org.onap.aai.schemagen.model.swagger.SwaggerDocument.License;
import org.onap.aai.schemagen.model.swagger.Tag;

public class SwaggerYamlWriterTest {

  private SwaggerYamlWriter writer = new SwaggerYamlWriter();

  @Test
  void thatSwaggerDocumentCanBeCreated() {
    List<Tag> tags = List.of(
      new Tag("Business"),
      new Tag("CloudInfrastructure"),
      new Tag("Topology"),
      new Tag("ServiceDesignAndCreation"),
      new Tag("Operations"),
      new Tag("Networks")
      );
    Info info = new Info();
    info.setDescription("[Differences versus the previous schema version](apidocs/aai/aai_swagger_v30.diff)\r\n" + //
            "\r\n" + //
            "    This document is best viewed with Firefox or Chrome. Nodes can be found by opening the models link below and finding the node-type. Edge definitions can be found with the node definitions.");
    info.setVersion("v30");
    info.setTitle("Active and Available Inventory REST API");
    info.setLicense(new License("Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0.html"));

    Paths paths = new Paths();
    GetOperation getOperation = new GetOperation();
    getOperation.setDescription("returns customer");
    getOperation.setSummary("returns customer");
    getOperation.setOperationId("getBusinessCustomersCustomer");
    getOperation.setProduces(List.of("application/json","application/xml"));
    getOperation.setTags(List.of(new Tag("Business")));
    getOperation.setResponses(Map.of("200", new Response("successful operation")));
    Path path = new Path(getOperation);
    paths.setPath(List.of(path));

    SwaggerDocument doc = new SwaggerDocument();
    doc.setSwagger("2.0");
    doc.setInfo(info);
    doc.setHost("localhost");
    doc.setBasePath("/aai/v30");
    doc.setSchemes(List.of("http"));
    doc.setTags(tags);

    doc.setPaths(paths);

    String result = writer.generateDocument(doc);
    assertNotNull(result);

  }
}
