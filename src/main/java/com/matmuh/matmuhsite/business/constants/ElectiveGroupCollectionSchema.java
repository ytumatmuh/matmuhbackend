package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.cms.FieldType;

import java.util.Arrays;
import java.util.List;

public final class ElectiveGroupCollectionSchema {

    private ElectiveGroupCollectionSchema() {}

    public static final String KEY = "elective-groups";
    public static final String SLUG_SOURCE_FIELD = "code";

    public static final String FIELD_CODE = "code";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TERM = "term";
    public static final String FIELD_SEMESTER = "semester";
    public static final String FIELD_DEGREE_LEVELS = "degreeLevels";
    public static final String FIELD_OPTION_LECTURE_IDS = "optionLectureIds";

    private static final String[] DEGREE_LEVEL_OPTIONS =
            Arrays.stream(DegreeLevel.values()).map(Enum::name).toArray(String[]::new);

    private static final List<FieldDefinition> OPTION_FIELDS = List.of(
            FieldDefinition.of("code", FieldType.SHORT_TEXT, "Ders kodu"),
            FieldDefinition.of("name", FieldType.SHORT_TEXT, "Ders adı"),
            FieldDefinition.of("localCredit", FieldType.NUMBER, "Yerel kredi"),
            FieldDefinition.of("ects", FieldType.NUMBER, "AKTS")
    );

    public static final CollectionSchema SCHEMA = new CollectionSchema(List.of(
            FieldDefinition.required(FIELD_CODE, FieldType.SHORT_TEXT, "Slot kodu")
                    .asFilterable()
                    .withHelp("Bologna müfredatındaki seçmeli slot kodu, ör. MES2-3G. Sayfa adresi bu koddan üretilir."),
            FieldDefinition.required(FIELD_NAME, FieldType.SHORT_TEXT, "Slot adı")
                    .asFilterable()
                    .withHelp("Müfredatta görünen ad, ör. Mesleki Seçmeli 2."),
            FieldDefinition.of("about", FieldType.LONG_TEXT, "Açıklama"),
            FieldDefinition.of(FIELD_TERM, FieldType.NUMBER, "Yarıyıl").asFilterable(),
            FieldDefinition.of(FIELD_SEMESTER, FieldType.SHORT_TEXT, "Dönem")
                    .asFilterable()
                    .withOptions("FALL", "SPRING", "SUMMER"),
            FieldDefinition.of(FIELD_DEGREE_LEVELS, FieldType.STRING_ARRAY, "Öğrenim düzeyi")
                    .withOptions(DEGREE_LEVEL_OPTIONS)
                    .asFilterable()
                    .withHelp("Boş bırakılırsa seçenek derslerin düzeylerinden alınır."),
            FieldDefinition.of("weeklyHours", FieldType.NUMBER, "Haftalık saat"),
            FieldDefinition.of("localCredit", FieldType.NUMBER, "Yerel kredi"),
            FieldDefinition.of("ects", FieldType.NUMBER, "AKTS"),
            FieldDefinition.of("selectionCount", FieldType.NUMBER, "Seçilecek ders sayısı")
                    .withHelp("Öğrencinin bu slottan kaç ders seçeceği. Varsayılan 1."),
            FieldDefinition.of(FIELD_OPTION_LECTURE_IDS, FieldType.STRING_ARRAY, "Seçenek ders ID'leri")
                    .withHelp("Bu slot yerine sayılabilecek dersler. Listeyi her kaydedişte baştan yazar: çıkarmak istediğiniz dersi listeden silin."),
            FieldDefinition.of("optionCount", FieldType.NUMBER, "Seçenek sayısı")
                    .asComputed()
                    .withHelp("Havuzdaki ders sayısı. Otomatik hesaplanır."),
            FieldDefinition.of("options", FieldType.OBJECT_ARRAY, "Seçenek dersler")
                    .asReadOnly()
                    .withItemFields(OPTION_FIELDS)
                    .withHelp("Seçenek dersler ders koduna göre sıralı döner, buradan düzenlenemez.")
    ));
}
