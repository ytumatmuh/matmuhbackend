package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CollectionSortParser {

    public record CollectionSort(String column, String dataField, boolean descending) {

        public static final CollectionSort DEFAULT = new CollectionSort("slug", null, false);

        public boolean isDataField() {
            return dataField != null;
        }
    }

    private static final Map<String, String> COLUMNS = Map.of(
            "slug", "slug",
            "createdat", "created_at",
            "updatedat", "updated_at"
    );

    public static final int MAX_SORT_FIELDS = 3;

    private CollectionSortParser() {}

    public static List<CollectionSort> parse(CollectionSchema schema, String value) {
        if (value == null || value.isBlank()) {
            return List.of(CollectionSort.DEFAULT);
        }

        var terms = new ArrayList<String>();
        for (var raw : value.split(",", -1)) {
            var term = raw.trim();
            if (!term.isEmpty()) {
                terms.add(term);
            }
        }

        if (terms.isEmpty()) {
            return List.of(CollectionSort.DEFAULT);
        }

        if (terms.size() > MAX_SORT_FIELDS) {
            throw new CmsValidationException(
                    "Too many sort fields (" + terms.size() + "); at most " + MAX_SORT_FIELDS + " are allowed.");
        }

        var sorts = new ArrayList<CollectionSort>();
        var seen = new LinkedHashSet<String>();

        for (var term : terms) {
            var sort = parseOne(schema, term);
            var identity = sort.isDataField() ? "data:" + sort.dataField() : "column:" + sort.column();
            if (!seen.add(identity)) {
                throw new CmsValidationException("Duplicate sort field '" + term + "'.");
            }
            sorts.add(sort);
        }

        return List.copyOf(sorts);
    }

    private static CollectionSort parseOne(CollectionSchema schema, String value) {
        var parts = value.split(":", -1);
        if (parts.length > 2) {
            throw new CmsValidationException(
                    "Invalid sort '" + value + "'; expected 'field' or 'field:asc|desc'.");
        }

        var descending = false;
        if (parts.length == 2) {
            descending = switch (parts[1].trim().toLowerCase()) {
                case "desc" -> true;
                case "asc" -> false;
                default -> throw new CmsValidationException(
                        "Unknown sort direction '" + parts[1] + "'; expected 'asc' or 'desc'.");
            };
        }

        var requested = parts[0].trim();
        var column = COLUMNS.get(requested.toLowerCase());
        if (column != null) {
            return new CollectionSort(column, null, descending);
        }

        var field = schema.fields().stream()
                .filter(candidate -> candidate.name().equals(requested))
                .findFirst()
                .orElseThrow(() -> new CmsValidationException(
                        "Unknown sort field '" + requested + "'. Available: " + available(schema) + "."));

        if (!field.sortable()) {
            throw new CmsValidationException(
                    "Field '" + field.name() + "' is not sortable. Available: " + available(schema) + ".");
        }

        return new CollectionSort(null, field.name(), descending);
    }

    private static String available(CollectionSchema schema) {
        return Stream.concat(
                        Stream.of("slug", "createdAt", "updatedAt"),
                        sortableFields(schema).stream())
                .collect(Collectors.joining(", "));
    }

    private static List<String> sortableFields(CollectionSchema schema) {
        return schema.fields().stream()
                .filter(FieldDefinition::sortable)
                .map(FieldDefinition::name)
                .toList();
    }
}
