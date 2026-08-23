package com.matmuh.matmuhsite.core.dtos.cms.response;

import com.matmuh.matmuhsite.entities.cms.FieldType;

import java.util.List;

public record FieldDefinition(
        String name,
        FieldType type,
        String label,
        boolean required,
        boolean readOnly,
        boolean filterable,
        boolean sortable,
        boolean searchable,
        List<String> options,
        List<FieldDefinition> itemFields,
        String help
) {
    public static FieldDefinition of(String name, FieldType type, String label) {
        return new FieldDefinition(name, type, label, false, false, false, false, false, null, null, null);
    }

    public static FieldDefinition required(String name, FieldType type, String label) {
        return new FieldDefinition(name, type, label, true, false, false, false, false, null, null, null);
    }

    public static FieldDefinition readOnly(String name, FieldType type, String label) {
        return new FieldDefinition(name, type, label, false, true, false, false, false, null, null, null);
    }

    public FieldDefinition asFilterable() {
        return new FieldDefinition(name, type, label, required, readOnly, true, sortable, searchable, options, itemFields, help);
    }

    public FieldDefinition asSortable() {
        return new FieldDefinition(name, type, label, required, readOnly, filterable, true, searchable, options, itemFields, help);
    }

    public FieldDefinition asSearchable() {
        return new FieldDefinition(name, type, label, required, readOnly, filterable, sortable, true, options, itemFields, help);
    }

    public FieldDefinition asReadOnly() {
        return new FieldDefinition(name, type, label, required, true, filterable, sortable, searchable, options, itemFields, help);
    }

    public FieldDefinition withOptions(String... options) {
        return new FieldDefinition(name, type, label, required, readOnly, filterable, sortable, searchable, List.of(options), itemFields, help);
    }

    public FieldDefinition withItemFields(List<FieldDefinition> itemFields) {
        return new FieldDefinition(name, type, label, required, readOnly, filterable, sortable, searchable, options, List.copyOf(itemFields), help);
    }

    public FieldDefinition withHelp(String help) {
        return new FieldDefinition(name, type, label, required, readOnly, filterable, sortable, searchable, options, itemFields, help);
    }
}
