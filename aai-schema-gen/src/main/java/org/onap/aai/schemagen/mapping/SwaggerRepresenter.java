package org.onap.aai.schemagen.mapping;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.representer.Representer;

/**
 * The order of the fields should be fixed in the swagger document
 */
public class SwaggerRepresenter extends Representer {

  final List<String> orderedPropertyList = List.of(
    "swagger",
    "info",
    "host",
    "basePath",
    "schemes",
    "tags",
    "paths");

  public SwaggerRepresenter(DumperOptions options) {
    super(options);
  }

  @Override
  protected Set<Property> getProperties(Class<? extends Object> type) {
    final Set<Property> result = new TreeSet<>(
      Comparator.comparingInt(a -> orderedPropertyList.indexOf(a.getName())));
    result.addAll(super.getProperties(type)
                        .stream()
                        .filter(property ->
                          orderedPropertyList.contains(property.getName()))
                        .collect(Collectors.toSet()));
    return result;
  }

}
