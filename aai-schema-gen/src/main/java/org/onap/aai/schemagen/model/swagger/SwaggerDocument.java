package org.onap.aai.schemagen.model.swagger;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class SwaggerDocument {

  String swagger;
  Info info;
  String host;
  String basePath;
  List<String> schemes;
  List<Tag> tags;
  Paths paths;

  @Data
  public static class Info {
    String description;
    String version;
    String title;
    License license;
  }

  @Data
  @AllArgsConstructor
  public static class License {
    String name;
    String url;
  }

}
