package org.onap.aai.schemagen.mapping;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.onap.aai.schemagen.model.swagger.SwaggerDocument;
import org.onap.aai.schemagen.model.swagger.Tag;
import org.yaml.snakeyaml.Yaml;
import org.onap.aai.schemagen.model.swagger.SwaggerDocument.Info;
import org.onap.aai.schemagen.model.swagger.SwaggerDocument.License;

public class SwaggerYamlWriter {
  public String generateDocument(SwaggerDocument swaggerDocument) {
    // PrintWriter writer = new PrintWriter(new File("./src/main/resources/output.yml"));
    StringWriter writer = new StringWriter();
    Yaml yaml = new Yaml();
    yaml.dump(swaggerDocument, writer);
    return writer.toString();
  }
}
