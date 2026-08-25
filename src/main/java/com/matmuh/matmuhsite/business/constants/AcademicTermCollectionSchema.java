package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.cms.FieldType;

import java.util.List;

public final class AcademicTermCollectionSchema {

    private AcademicTermCollectionSchema() {}

    public static final String KEY = "academic-terms";
    public static final String SLUG_SOURCE_FIELD = "academicYear";

    public static final String FIELD_ACADEMIC_YEAR = "academicYear";
    public static final String FIELD_SEMESTER = "semester";
    public static final String FIELD_START_DATE = "startDate";
    public static final String FIELD_END_DATE = "endDate";

    public static final CollectionSchema SCHEMA = new CollectionSchema(List.of(
            FieldDefinition.required(FIELD_ACADEMIC_YEAR, FieldType.SHORT_TEXT, "Akademik yıl")
                    .asFilterable()
                    .withHelp("2026-2027 biçiminde yazın."),
            FieldDefinition.required(FIELD_SEMESTER, FieldType.SHORT_TEXT, "Yarıyıl")
                    .asFilterable()
                    .withOptions("FALL", "SPRING", "SUMMER"),
            FieldDefinition.required(FIELD_START_DATE, FieldType.DATE, "Başlangıç")
                    .withHelp("Derslerin başladığı ilk gün. Haftalık ders programı bu aralıkta tarihlere açılır."),
            FieldDefinition.required(FIELD_END_DATE, FieldType.DATE, "Bitiş")
                    .withHelp("Derslerin bittiği son gün.")
    ));
}
