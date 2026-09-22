package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.ChoiceSource;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.cms.FieldType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionRegistryTest {

    private final CollectionRegistry registry = new CollectionRegistry();

    // 4.4.0 displayName: editör sekmede "elective-groups" değil "Seçmeli Grupları" görür.
    @Test
    void everyCollectionHasAHumanName() {
        for (var def : registry.all()) {
            assertFalse(def.displayName() == null || def.displayName().isBlank(), def.key());
            assertFalse(def.displayName().contains("-"), def.key() + " anahtar gibi görünüyor");
        }
    }

    // İngilizce alanlar opsiyonel düz alanlar: koleksiyon localized değil, çeviri tablosu yok.
    @Test
    void lectureAndElectiveGroupExposeOptionalEnglishFields() {
        var lectureFields = registry.resolve(LectureCollectionSchema.KEY).schema().fields().stream()
                .collect(java.util.stream.Collectors.toMap(FieldDefinition::name, f -> f));
        assertEquals(FieldType.SHORT_TEXT, lectureFields.get("nameEn").type());
        assertEquals(FieldType.LONG_TEXT, lectureFields.get("aboutEn").type());
        assertEquals(FieldType.LONG_TEXT, lectureFields.get("gradingPolicyEn").type());
        assertEquals(FieldType.LONG_TEXT, lectureFields.get("resourcesEn").type());
        assertFalse(lectureFields.get("nameEn").required());
        var syllabusRow = lectureFields.get("syllabus").itemFields().stream()
                .collect(java.util.stream.Collectors.toMap(FieldDefinition::name, f -> f));
        assertEquals(FieldType.LONG_TEXT, syllabusRow.get("topicEn").type());
        assertEquals(FieldType.LONG_TEXT, syllabusRow.get("topic").type());
        assertFalse(syllabusRow.get("topicEn").required());

        var groupFields = registry.resolve(ElectiveGroupCollectionSchema.KEY).schema().fields().stream()
                .collect(java.util.stream.Collectors.toMap(FieldDefinition::name, f -> f));
        assertEquals(FieldType.SHORT_TEXT, groupFields.get("nameEn").type());
        assertEquals(FieldType.LONG_TEXT, groupFields.get("aboutEn").type());
        assertFalse(groupFields.get("nameEn").required());
    }

    // Eğitim dili ders düzeyinde çoklu: editör iki dilde verilen derste ikisini de işaretler,
    // liste ?languages=ENGLISH ile süzülür. Dönem kaydının tek değerli dili ayrı bir alan.
    @Test
    void lectureLanguagesAreAConstrainedFilterableList() {
        var languages = registry.resolve(LectureCollectionSchema.KEY).schema().fields().stream()
                .filter(f -> f.name().equals("languages"))
                .findFirst().orElseThrow();

        assertEquals(FieldType.STRING_ARRAY, languages.type());
        assertTrue(languages.filterable());
        assertFalse(languages.required());
        assertEquals(ChoiceSource.STATIC, languages.source().kind());
        assertEquals(List.of("TURKISH", "ENGLISH"), languages.source().values());
        assertTrue(registry.resolve(LectureCollectionSchema.KEY).schema().fields().stream()
                .noneMatch(f -> f.name().equals("language")));
    }

    @Test
    void attachmentsAreASingleFileField() {
        for (var key : List.of(AnnouncementCollectionSchema.KEY, NewsCollectionSchema.KEY)) {
            var attachments = registry.resolve(key).schema().fields().stream()
                    .filter(f -> f.name().equals("attachments"))
                    .findFirst().orElseThrow();
            assertEquals(FieldType.OBJECT_ARRAY, attachments.type());
            assertEquals(List.of("file"), attachments.itemFields().stream().map(FieldDefinition::name).toList(), key);
            var file = attachments.itemFields().get(0);
            assertEquals(FieldType.FILE, file.type());
            assertTrue(file.required());
        }
    }

    // Sağlayıcılı koleksiyon CMS'ten silinemez; 400 nereye gidileceğini söylemeli (Egehan
    // academic-terms'i CMS'ten silmeye çalışıp boş bir 400 aldı, 22 Eylül).
    @Test
    void providerCollectionsNameTheirRestDeleteEndpoint() {
        for (var def : registry.all()) {
            var jsonbBacked = List.of(AnnouncementCollectionSchema.KEY, NewsCollectionSchema.KEY).contains(def.key());
            if (jsonbBacked) {
                assertNull(def.restDeletePath(), def.key());
            } else {
                assertTrue(def.restDeletePath() != null && def.restDeletePath().startsWith("DELETE /api/"), def.key());
            }
        }
        assertEquals("DELETE /api/calendar-admin/terms/{id}",
                registry.resolve(AcademicTermCollectionSchema.KEY).restDeletePath());
    }
}
