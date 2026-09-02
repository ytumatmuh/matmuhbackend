package com.matmuh.matmuhsite.core.dtos.cms.response;

import com.matmuh.matmuhsite.entities.cms.FieldType;

import java.util.List;

public record FieldDefinition(
        String name,
        FieldType type,
        String label,
        boolean required,
        boolean readOnly,
        boolean computed,
        boolean filterable,
        boolean sortable,
        boolean searchable,
        ChoiceSource source,
        Boolean allowCustom,
        List<FieldDefinition> itemFields,
        String help
) {
    public static FieldDefinition of(String name, FieldType type, String label) {
        return new FieldDefinition(name, type, label, false, false, false, false, false, false, null, null, null, null);
    }

    public static FieldDefinition required(String name, FieldType type, String label) {
        return new FieldDefinition(name, type, label, true, false, false, false, false, false, null, null, null, null);
    }

    public static FieldDefinition readOnly(String name, FieldType type, String label) {
        return new FieldDefinition(name, type, label, false, true, false, false, false, false, null, null, null, null);
    }

    public static FieldDefinition select(String name, String label, ChoiceSource source) {
        return new FieldDefinition(name, FieldType.SELECT, label, false, false, false, false, false, false, source, null, null, null);
    }

    public static FieldDefinition requiredSelect(String name, String label, ChoiceSource source) {
        return new FieldDefinition(name, FieldType.SELECT, label, true, false, false, false, false, false, source, null, null, null);
    }

    public FieldDefinition asFilterable() {
        return new FieldDefinition(name, type, label, required, readOnly, computed, true, sortable, searchable, source, allowCustom, itemFields, help);
    }

    public FieldDefinition asSortable() {
        return new FieldDefinition(name, type, label, required, readOnly, computed, filterable, true, searchable, source, allowCustom, itemFields, help);
    }

    public FieldDefinition asSearchable() {
        return new FieldDefinition(name, type, label, required, readOnly, computed, filterable, sortable, true, source, allowCustom, itemFields, help);
    }

    public FieldDefinition asComputed() {
        return new FieldDefinition(name, type, label, required, true, true, filterable, sortable, searchable, source, allowCustom, itemFields, help);
    }

    public FieldDefinition asReadOnly() {
        return new FieldDefinition(name, type, label, required, true, computed, filterable, sortable, searchable, source, allowCustom, itemFields, help);
    }

    public FieldDefinition withSource(ChoiceSource source) {
        return new FieldDefinition(name, type, label, required, readOnly, computed, filterable, sortable, searchable, source, allowCustom, itemFields, help);
    }

    public FieldDefinition allowingCustom() {
        return new FieldDefinition(name, type, label, required, readOnly, computed, filterable, sortable, searchable, source, true, itemFields, help);
    }

    public FieldDefinition withItemFields(List<FieldDefinition> itemFields) {
        return new FieldDefinition(name, type, label, required, readOnly, computed, filterable, sortable, searchable, source, allowCustom, List.copyOf(itemFields), help);
    }

    public FieldDefinition withHelp(String help) {
        return new FieldDefinition(name, type, label, required, readOnly, computed, filterable, sortable, searchable, source, allowCustom, itemFields, help);
    }
}
