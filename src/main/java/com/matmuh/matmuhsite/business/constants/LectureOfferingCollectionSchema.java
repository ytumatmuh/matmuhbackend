package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.ExamType;
import com.matmuh.matmuhsite.entities.cms.FieldType;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;

public final class LectureOfferingCollectionSchema {

    private LectureOfferingCollectionSchema() {}

    public static final String KEY = "lecture-offerings";
    public static final String SLUG_SOURCE_FIELD = "lectureCode";

    public static final String FIELD_LECTURE_CODE = "lectureCode";
    public static final String FIELD_ACADEMIC_YEAR = "academicYear";
    public static final String FIELD_SEMESTER = "semester";
    public static final String FIELD_GROUP_NUMBER = "groupNumber";
    public static final String FIELD_STAFF_SLUG = "staffSlug";
    public static final String FIELD_INSTRUCTOR_RAW_NAME = "instructorRawName";
    public static final String FIELD_LANGUAGE = "language";
    public static final String FIELD_EXAM_WEIGHTS = "examWeights";
    public static final String FIELD_SCHEDULE_SLOTS = "scheduleSlots";

    private static final String[] DAY_OPTIONS =
            Arrays.stream(DayOfWeek.values()).map(Enum::name).toArray(String[]::new);

    private static final String[] EXAM_TYPE_OPTIONS =
            Arrays.stream(ExamType.values()).map(Enum::name).toArray(String[]::new);

    private static final List<FieldDefinition> SLOT_FIELDS = List.of(
            FieldDefinition.required("dayOfWeek", FieldType.SHORT_TEXT, "Gün").withOptions(DAY_OPTIONS),
            FieldDefinition.required("startTime", FieldType.SHORT_TEXT, "Başlangıç")
                    .withHelp("HH:mm biçiminde, ör. 09:00."),
            FieldDefinition.required("endTime", FieldType.SHORT_TEXT, "Bitiş"),
            FieldDefinition.of("classroom", FieldType.SHORT_TEXT, "Derslik"),
            FieldDefinition.of("online", FieldType.BOOL, "Çevrimiçi")
                    .withHelp("İşaretlenirse derslik kaydedilmez.")
    );

    private static final List<FieldDefinition> EXAM_WEIGHT_FIELDS = List.of(
            FieldDefinition.required("examType", FieldType.SHORT_TEXT, "Sınav").withOptions(EXAM_TYPE_OPTIONS),
            FieldDefinition.required("weightPercent", FieldType.NUMBER, "Ağırlık (%)")
    );

    public static final CollectionSchema SCHEMA = new CollectionSchema(List.of(
            FieldDefinition.required(FIELD_LECTURE_CODE, FieldType.SHORT_TEXT, "Ders kodu")
                    .asFilterable().asSearchable()
                    .withHelp("Katalogdaki ders kodu, ör. MTM1501. Slug bu koddan, yıldan, dönemden ve grup numarasından üretilir."),
            FieldDefinition.required(FIELD_ACADEMIC_YEAR, FieldType.SHORT_TEXT, "Akademik yıl")
                    .asFilterable()
                    .withHelp("2026-2027 biçiminde."),
            FieldDefinition.required(FIELD_SEMESTER, FieldType.SHORT_TEXT, "Yarıyıl")
                    .asFilterable()
                    .withOptions("FALL", "SPRING", "SUMMER"),
            FieldDefinition.required(FIELD_GROUP_NUMBER, FieldType.NUMBER, "Grup")
                    .asFilterable()
                    .withHelp("Aynı dersin aynı dönemdeki farklı grupları için."),
            FieldDefinition.of(FIELD_STAFF_SLUG, FieldType.SHORT_TEXT, "Eğitmen (personel)")
                    .asFilterable()
                    .withHelp("Personel dizinindeki slug, ör. muslum-hoca. Personel kaydı yoksa boş bırakıp aşağıdaki ham adı doldurun."),
            FieldDefinition.of(FIELD_INSTRUCTOR_RAW_NAME, FieldType.SHORT_TEXT, "Eğitmen (ham ad)")
                    .withHelp("Personel dizininde olmayan hocalar için, ör. Erasmus veya başka bölümden gelen. İkisinden en az biri dolu olmalı."),
            FieldDefinition.readOnly("instructorName", FieldType.SHORT_TEXT, "Görünen eğitmen adı")
                    .asComputed()
                    .withHelp("Personel kaydı varsa adı, yoksa ham ad. Okuma anında türetilir."),
            FieldDefinition.of(FIELD_LANGUAGE, FieldType.SHORT_TEXT, "Öğretim dili")
                    .withOptions("TURKISH", "ENGLISH"),
            FieldDefinition.of(FIELD_EXAM_WEIGHTS, FieldType.OBJECT_ARRAY, "Sınav ağırlıkları")
                    .withItemFields(EXAM_WEIGHT_FIELDS)
                    .withHelp("Bu hocanın bu dönemdeki değerlendirme yüzdeleri. Dersteki Bologna varsayılanından bağımsızdır."),
            FieldDefinition.of(FIELD_SCHEDULE_SLOTS, FieldType.OBJECT_ARRAY, "Ders saatleri")
                    .withItemFields(SLOT_FIELDS)
                    .withHelp("Haftalık ders saatleri. Aynı derslik veya aynı hoca çakışırsa kayıt reddedilir ve çakışan satır bildirilir.")
    ));
}
