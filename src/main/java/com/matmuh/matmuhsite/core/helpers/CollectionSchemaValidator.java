package com.matmuh.matmuhsite.core.helpers;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.cms.FieldType;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;

import java.util.Map;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class CollectionSchemaValidator {

    private CollectionSchemaValidator() {}

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private static final java.util.Set<String> ENVELOPE_KEYS = java.util.Set.of("id", "slug");

    private static final int SHORT_TEXT_MAX_LENGTH = 255;

    private static final List<FieldDefinition> IMAGE_FIELDS = List.of(
            FieldDefinition.required("src", FieldType.URL, "Src"),
            FieldDefinition.required("alt", FieldType.SHORT_TEXT, "Alt")
    );

    public static ObjectNode validateAndStrip(CollectionSchema schema, JsonNode data) {
        return validateAndStrip(schema, data, false);
    }

    public static ObjectNode validateAndStrip(CollectionSchema schema, JsonNode data, boolean isDraft) {
        var errors = new ArrayList<String>();
        var result = validateObject(schema.fields(), data, isDraft, errors, null);
        if (!errors.isEmpty()) {
            throw new CmsValidationException(errors);
        }
        return result;
    }

    private static ObjectNode validateObject(List<FieldDefinition> fields, JsonNode data,
                                             boolean isDraft, List<String> errors, String path) {
        var result = NODES.objectNode();

        if (data == null || !data.isObject()) {
            errors.add((path == null ? "Data" : "Field '" + path + "'") + " must be a JSON object.");
            return result;
        }

        var fieldNames = new HashSet<String>();

        for (var field : fields) {
            fieldNames.add(field.name());
            if (field.readOnly()) continue;

            var fieldPath = path == null ? field.name() : path + "." + field.name();
            var value = data.get(field.name());
            var hasValue = value != null && !value.isNull();

            if (!hasValue) {
                if (field.required() && !isDraft) {
                    errors.add("Field '" + fieldPath + "' is required.");
                }
                continue;
            }

            if (field.type() == FieldType.OBJECT_ARRAY) {
                result.set(field.name(), validateObjectArray(field, value, isDraft, errors, fieldPath));
                continue;
            }

            if (field.type() == FieldType.IMAGE) {
                result.set(field.name(), validateObject(IMAGE_FIELDS, value, isDraft, errors, fieldPath));
                continue;
            }

            var typeError = typeErrorFor(value, field);
            if (typeError != null) {
                errors.add("Field '" + fieldPath + "': " + typeError);
                continue;
            }

            result.set(field.name(), value.deepCopy());
        }

        for (Map.Entry<String, JsonNode> prop : data.properties()) {
            if (fieldNames.contains(prop.getKey())) {
                continue;
            }

            if (path == null && ENVELOPE_KEYS.contains(prop.getKey())) {
                continue;
            }
            var unknownPath = path == null ? prop.getKey() : path + "." + prop.getKey();
            errors.add("Unknown field '" + unknownPath + "'.");
        }

        return result;
    }

    private static ArrayNode validateObjectArray(FieldDefinition field, JsonNode value,
                                                 boolean isDraft, List<String> errors, String path) {
        var cleaned = NODES.arrayNode();

        if (!value.isArray()) {
            errors.add("Field '" + path + "': expected array.");
            return cleaned;
        }

        var itemFields = field.itemFields() == null ? List.<FieldDefinition>of() : field.itemFields();
        for (int i = 0; i < value.size(); i++) {
            cleaned.add(validateObject(itemFields, value.get(i), isDraft, errors, path + "[" + i + "]"));
        }
        return cleaned;
    }

    private static String typeErrorFor(JsonNode value, FieldDefinition field) {
        switch (field.type()) {
            case SHORT_TEXT, URL -> {
                if (!value.isTextual()) return "expected string.";
                if (field.options() != null && !field.options().isEmpty()
                        && !field.options().contains(value.asText())) {
                    return "value not in allowed options.";
                }

                if (value.asText().length() > SHORT_TEXT_MAX_LENGTH) {
                    return "must be at most " + SHORT_TEXT_MAX_LENGTH + " characters, got " + value.asText().length() + ".";
                }
                return null;
            }
            case LONG_TEXT, RICH_TEXT -> {
                if (!value.isTextual()) return "expected string.";
                if (field.options() != null && !field.options().isEmpty()
                        && !field.options().contains(value.asText())) {
                    return "value not in allowed options.";
                }
                return null;
            }
            case BOOL -> {
                return value.isBoolean() ? null : "expected boolean.";
            }
            case NUMBER -> {
                return value.isNumber() ? null : "expected number.";
            }
            case DATE -> {
                if (!value.isTextual() || !isParsableDate(value.asText())) {
                    return "expected ISO date string.";
                }
                return null;
            }
            case STRING_ARRAY -> {
                if (!value.isArray()) return "expected array.";
                for (JsonNode item : value) {
                    if (!item.isTextual()) return "expected array of strings.";
                }
                return null;
            }
            default -> {
                return "unsupported type " + field.type() + ".";
            }
        }
    }

    private static boolean isParsableDate(String text) {
        try { Instant.parse(text); return true; } catch (Exception ignored) {}
        try { OffsetDateTime.parse(text); return true; } catch (Exception ignored) {}
        try { LocalDateTime.parse(text); return true; } catch (Exception ignored) {}
        try { LocalDate.parse(text); return true; } catch (Exception ignored) {}
        return false;
    }
}