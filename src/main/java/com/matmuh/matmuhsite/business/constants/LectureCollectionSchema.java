package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.ChoiceSource;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.LectureCategory;
import com.matmuh.matmuhsite.entities.LectureType;
import com.matmuh.matmuhsite.entities.cms.FieldType;

import java.util.Arrays;
import java.util.List;

public final class LectureCollectionSchema {

    private LectureCollectionSchema() {}

    public static final String KEY = "lectures";
    public static final String SLUG_SOURCE_FIELD = "code";
    public static final String DISPLAY_FIELD = "name";

    public static final String FIELD_TERM = "term";
    public static final String FIELD_SEMESTER = "semester";
    public static final String FIELD_CODE = "code";
    public static final String FIELD_DEGREE_LEVELS = "degreeLevels";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_CATEGORY = "category";

    private static final String[] TYPE_OPTIONS =
            Arrays.stream(LectureType.values()).map(Enum::name).toArray(String[]::new);

    private static final String[] CATEGORY_OPTIONS =
            Arrays.stream(LectureCategory.values()).map(Enum::name).toArray(String[]::new);

    private static final List<FieldDefinition> SYLLABUS_FIELDS = List.of(
            FieldDefinition.required("week", FieldType.NUMBER, "Hafta"),
            FieldDefinition.required("topic", FieldType.SHORT_TEXT, "Konu")
    );

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
                    .withHelp("Sayfa adresi bu koddan üretilir. Filtre olarak kullanıldığında ad, kod ve içerikte arama yapar."),
            FieldDefinition.of("language", FieldType.SHORT_TEXT, "Dil"),
            FieldDefinition.of("about", FieldType.LONG_TEXT, "Ders içeriği"),
            FieldDefinition.of("gradingPolicy", FieldType.LONG_TEXT, "Değerlendirme"),
            FieldDefinition.of("resources", FieldType.LONG_TEXT, "Kaynaklar"),
            FieldDefinition.of(FIELD_DEGREE_LEVELS, FieldType.STRING_ARRAY, "Öğrenim düzeyi")
                    .withSource(ChoiceSource.ofValues(DEGREE_LEVEL_OPTIONS))
                    .asFilterable()
                    .withHelp("Bir ders birden fazla programda okutulabilir; lisansüstü seçmeli havuzu hem yüksek lisansta hem doktorada geçerlidir. Boş bırakılırsa ders kodundan türetilir."),
            FieldDefinition.select(FIELD_TYPE, "Ders türü", ChoiceSource.ofValues(TYPE_OPTIONS))
                    .asFilterable()
                    .withHelp("Müfredatta zorunlu ve seçmeli bloklarını ayırmak için kullanılır. Bir ders seçmeli bir slota eklendiğinde boşsa otomatik Seçmeli olur."),
            FieldDefinition.select(FIELD_CATEGORY, "Ders kategorisi", ChoiceSource.ofValues(CATEGORY_OPTIONS))
                    .asFilterable()
                    .withHelp("YÖK ders kategorisi; kategori bazlı kredi dağılımı buradan çıkarılır."),
            FieldDefinition.of(FIELD_TERM, FieldType.NUMBER, "Yarıyıl").asFilterable(),
            FieldDefinition.select(FIELD_SEMESTER, "Dönem", ChoiceSource.ofValues("FALL", "SPRING", "SUMMER"))
                    .asFilterable(),
            FieldDefinition.of("syllabus", FieldType.OBJECT_ARRAY, "Haftalık program")
                    .withItemFields(SYLLABUS_FIELDS)
                    .withHelp("Her satır bir haftanın konusu. Hafta numarasına göre sıralı döner."),
            FieldDefinition.of("midtermWeight", FieldType.NUMBER, "Vize ağırlığı (%)"),
            FieldDefinition.of("finalWeight", FieldType.NUMBER, "Final ağırlığı (%)"),
            FieldDefinition.of("theoryHours", FieldType.NUMBER, "Teori saati"),
            FieldDefinition.of("practiceHours", FieldType.NUMBER, "Uygulama saati"),
            FieldDefinition.of("labHours", FieldType.NUMBER, "Laboratuvar saati"),
            FieldDefinition.of("weeklyHours", FieldType.NUMBER, "Haftalık saat (toplam)")
                    .withHelp("Boş bırakılırsa teori + uygulama + laboratuvar toplamından hesaplanır."),
            FieldDefinition.of("localCredit", FieldType.NUMBER, "Yerel kredi"),
            FieldDefinition.of("ects", FieldType.NUMBER, "AKTS"),
            FieldDefinition.of("bolognaLink", FieldType.URL, "Bologna sayfası"),
            FieldDefinition.of("notesLink", FieldType.URL, "Not bağlantısı"),
            FieldDefinition.of("noteCount", FieldType.NUMBER, "Not sayısı")
                    .asComputed()
                    .withHelp("Onaylanmış ders notu sayısı. Otomatik hesaplanır."),
            FieldDefinition.of("electiveGroupCount", FieldType.NUMBER, "Seçmeli grup sayısı")
                    .asComputed()
                    .withHelp("Bu dersin seçenek olarak yer aldığı seçmeli slot sayısı. Ders türü Seçmeli ise burası 0 olmamalı."),
            FieldDefinition.of("statisticsTermCount", FieldType.NUMBER, "İstatistik dönem sayısı")
                    .asComputed()
                    .withHelp("Sınav istatistiği bulunan dönem sayısı. Otomatik hesaplanır."),
            FieldDefinition.of("staff", FieldType.OBJECT_ARRAY, "Personel")
                    .asReadOnly()
                    .withItemFields(STAFF_FIELDS)
                    .withHelp("Personel ataması dönem kayıtlarından gelir. Düzenlemek için Dönem Kayıtları koleksiyonunu açın.")
    ));
}
