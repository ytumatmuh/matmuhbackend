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
        List<String> options,
        List<FieldDefinition> itemFields,
        String help
) {
    public static FieldDefinition of(String name, FieldType type, String label) {
        return new FieldDefinition(name, type, label, false, false, false, null, null, null);
    }

    public static FieldDefinition required(String name, FieldType type, String label) {
        return new FieldDefinition(name, type, label, true, false, false, null, null, null);
    }

    public FieldDefinition asFilterable() {
        return new FieldDefinition(name, type, label, required, readOnly, true, options, itemFields, help);
    }
}
