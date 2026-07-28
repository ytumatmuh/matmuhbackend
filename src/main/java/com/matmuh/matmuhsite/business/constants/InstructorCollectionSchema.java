package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.cms.FieldType;

import java.util.List;

public final class InstructorCollectionSchema {

    private InstructorCollectionSchema() {}

    public static final String KEY = "instructors";
    public static final String SLUG_SOURCE_FIELD = "lastName";

    public static final String FIELD_FIRST_NAME = "firstName";
    public static final String FIELD_LAST_NAME = "lastName";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_AVESIS_LINK = "avesisLink";

    public static final CollectionSchema SCHEMA = new CollectionSchema(List.of(
            FieldDefinition.readOnly("id", FieldType.SHORT_TEXT, "ID"),
            FieldDefinition.readOnly("slug", FieldType.SHORT_TEXT, "Slug"),
            FieldDefinition.required(FIELD_FIRST_NAME, FieldType.SHORT_TEXT, "Ad").asFilterable(),
            FieldDefinition.required(FIELD_LAST_NAME, FieldType.SHORT_TEXT, "Soyad").asFilterable(),
            FieldDefinition.of("academicTitle", FieldType.SHORT_TEXT, "Akademik Ünvan"),
            FieldDefinition.of(FIELD_EMAIL, FieldType.SHORT_TEXT, "E-posta").asFilterable(),
            FieldDefinition.of("office", FieldType.SHORT_TEXT, "Ofis"),
            FieldDefinition.of(FIELD_AVESIS_LINK, FieldType.URL, "AVESİS").asFilterable()
    ));
}
