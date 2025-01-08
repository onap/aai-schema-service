package org.onap.aai.schemagen.model.swagger;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class Operation {
  List<Tag> tags;
  String summary;
  String description;
  String operationId;
  List<String> produces;
  Map<String, Response> responses;
}
