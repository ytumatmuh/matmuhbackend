package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.cms.FieldType;

import java.util.Arrays;
import java.util.List;

public final class LectureCollectionSchema {

    private LectureCollectionSchema() {}

    public static final String KEY = "lectures";
    public static final String SLUG_SOURCE_FIELD = "code";

    public static final String FIELD_TERM = "term";
    public static final String FIELD_SEMESTER = "semester";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_DEGREE_LEVELS = "degreeLevels";

    private static final String[] DEGREE_LEVEL_OPTIONS =
            Arrays.stream(DegreeLevel.values()).map(Enum::name).toArray(String[]::new);

    private static final List<FieldDefinition> STAFF_FIELDS = List.of(
            FieldDefinition.of("firstName", FieldType.SHORT_TEXT, "Ad"),
            FieldDefinition.of("lastName", FieldType.SHORT_TEXT, "Soyad"),
            FieldDefinition.of("academicTitle", FieldType.SHORT_TEXT, "Unvan"),
            FieldDefinition.of("email", FieldType.SHORT_TEXT, "E-posta"),
            FieldDefinition.of("office", FieldType.SHORT_TEXT, "Ofis"),
            FieldDefinition.of("avesisLink", FieldType.URL, "AVESİS")
    );

    public static final CollectionSchema SCHEMA = new CollectionSchema(List.of(
            FieldDefinition.required("name", FieldType.SHORT_TEXT, "Ders adı"),
            FieldDefinition.required(FIELD_CODE, FieldType.SHORT_TEXT, "Ders kodu")
                    .asFilterable()
                    .withHelp("Slug bu koddan üretilir. Filtre olarak kullanıldığında ad, kod ve içerikte arama yapar."),
            FieldDefinition.of("language", FieldType.SHORT_TEXT, "Dil"),
            FieldDefinition.of("about", FieldType.LONG_TEXT, "Ders içeriği"),
            FieldDefinition.of("gradingPolicy", FieldType.LONG_TEXT, "Değerlendirme"),
            FieldDefinition.of("resources", FieldType.LONG_TEXT, "Kaynaklar"),
            FieldDefinition.of(FIELD_DEGREE_LEVELS, FieldType.STRING_ARRAY, "Öğrenim düzeyi")
                    .withOptions(DEGREE_LEVEL_OPTIONS)
                    .asFilterable()
                    .withHelp("Bir ders birden fazla programda okutulabilir; lisansüstü seçmeli havuzu hem yüksek lisansta hem doktorada geçerlidir. Boş bırakılırsa ders kodundan türetilir."),
            FieldDefinition.of(FIELD_TERM, FieldType.NUMBER, "Yarıyıl").asFilterable(),
            FieldDefinition.of(FIELD_SEMESTER, FieldType.SHORT_TEXT, "Dönem")
                    .asFilterable()
                    .withOptions("FALL", "SPRING", "SUMMER"),
            FieldDefinition.of("weeklyHours", FieldType.NUMBER, "Haftalık saat"),
            FieldDefinition.of("localCredit", FieldType.NUMBER, "Yerel kredi"),
            FieldDefinition.of("ects", FieldType.NUMBER, "AKTS"),
            FieldDefinition.of("bolognaLink", FieldType.URL, "Bologna sayfası"),
            FieldDefinition.of("notesLink", FieldType.URL, "Not bağlantısı"),
            FieldDefinition.of("noteCount", FieldType.NUMBER, "Not sayısı")
                    .asComputed()
                    .withHelp("Onaylanmış ders notu sayısı, okuma anında hesaplanır."),
            FieldDefinition.of("statisticsTermCount", FieldType.NUMBER, "İstatistik dönem sayısı")
                    .asComputed()
                    .withHelp("Sınav istatistiği bulunan dönem sayısı, okuma anında hesaplanır."),
            FieldDefinition.of("staff", FieldType.OBJECT_ARRAY, "Personel")
                    .asReadOnly()
                    .withItemFields(STAFF_FIELDS)
                    .withHelp("Personel ataması dönem kayıtlarından gelir, buradan düzenlenemez.")
    ));
}
