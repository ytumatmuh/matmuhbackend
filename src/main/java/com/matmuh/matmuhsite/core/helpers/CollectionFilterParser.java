package com.matmuh.matmuhsite.core.helpers;

import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CollectionFilterParser {

    private CollectionFilterParser() {}

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    public static ObjectNode build(CollectionSchema schema, Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) return null;

        var errors = new ArrayList<String>();
        var result = NODES.objectNode();

        Map<String, FieldDefinition> fieldsByName = schema.fields().stream()
                .collect(Collectors.toMap(FieldDefinition::name, Function.identity()));

        for (var entry : filters.entrySet()) {
            var name = entry.getKey();
            var value = entry.getValue();

            var field = fieldsByName.get(name);
            if (field == null) {
                errors.add("Unknown filter field '" + name + "'.");
                continue;
            }
            if (!field.filterable()) {
                errors.add("Field '" + name + "' is not filterable.");
                continue;
            }

            switch (field.type()) {
                case STRING_ARRAY -> result.set(name, NODES.arrayNode().add(value));
                case BOOL -> {
                    if (value.equalsIgnoreCase("true")) result.put(name, true);
                    else if (value.equalsIgnoreCase("false")) result.put(name, false);
                    else errors.add("Field '" + name + "': invalid value '" + value + "' for type Bool.");
                }
                case NUMBER -> {
                    try {
                        result.put(name, Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        errors.add("Field '" + name + "': invalid value '" + value + "' for type Number.");
                    }
                }
                default -> result.put(name, value);
            }
        }

        if (!errors.isEmpty()) {
            throw new CmsValidationException(errors);
        }
        return result;
    }
}