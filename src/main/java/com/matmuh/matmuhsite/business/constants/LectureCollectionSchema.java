package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.cms.FieldType;

import java.util.List;

public final class LectureCollectionSchema {

    private LectureCollectionSchema() {}

    public static final String KEY = "lectures";
    public static final String SLUG_SOURCE_FIELD = "code";

    public static final String FIELD_TERM = "term";
    public static final String FIELD_SEMESTER = "semester";
    public static final String FIELD_CODE = "code";

    private static final List<FieldDefinition> INSTRUCTOR_FIELDS = List.of(
            FieldDefinition.of("firstName", FieldType.SHORT_TEXT, "Ad"),
            FieldDefinition.of("lastName", FieldType.SHORT_TEXT, "Soyad"),
            FieldDefinition.of("academicTitle", FieldType.SHORT_TEXT, "Unvan"),
            FieldDefinition.of("email", FieldType.SHORT_TEXT, "E-posta"),
            FieldDefinition.of("office", FieldType.SHORT_TEXT, "Ofis"),
            FieldDefinition.of("avesisLink", FieldType.URL, "AVESİS")
    );

    public static final CollectionSchema SCHEMA = new CollectionSchema(List.of(
            FieldDefinition.readOnly("id", FieldType.SHORT_TEXT, "ID"),
            FieldDefinition.readOnly("slug", FieldType.SHORT_TEXT, "Slug"),
            FieldDefinition.required("name", FieldType.SHORT_TEXT, "Ders adı"),
            FieldDefinition.required(FIELD_CODE, FieldType.SHORT_TEXT, "Ders kodu")
                    .asFilterable()
                    .withHelp("Slug bu koddan üretilir. Filtre olarak kullanıldığında ad, kod ve içerikte arama yapar."),
            FieldDefinition.of("language", FieldType.SHORT_TEXT, "Dil"),
            FieldDefinition.of("about", FieldType.LONG_TEXT, "Ders içeriği"),
            FieldDefinition.of("gradingPolicy", FieldType.LONG_TEXT, "Değerlendirme"),
            FieldDefinition.of("resources", FieldType.LONG_TEXT, "Kaynaklar"),
            FieldDefinition.of(FIELD_TERM, FieldType.NUMBER, "Yarıyıl").asFilterable(),
            FieldDefinition.of(FIELD_SEMESTER, FieldType.SHORT_TEXT, "Dönem")
                    .asFilterable()
                    .withOptions("FALL", "SPRING", "SUMMER"),
            FieldDefinition.of("weeklyHours", FieldType.NUMBER, "Haftalık saat"),
            FieldDefinition.of("localCredit", FieldType.NUMBER, "Yerel kredi"),
            FieldDefinition.of("ects", FieldType.NUMBER, "AKTS"),
            FieldDefinition.of("bolognaLink", FieldType.URL, "Bologna sayfası"),
            FieldDefinition.of("notesLink", FieldType.URL, "Not bağlantısı"),
            FieldDefinition.of("instructors", FieldType.OBJECT_ARRAY, "Akademisyenler")
                    .asReadOnly()
                    .withItemFields(INSTRUCTOR_FIELDS)
                    .withHelp("Akademisyen ataması dönem kayıtlarından gelir, buradan düzenlenemez.")
    ));
}
