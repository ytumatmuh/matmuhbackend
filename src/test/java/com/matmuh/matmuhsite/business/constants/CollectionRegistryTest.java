package com.matmuh.matmuhsite.business.constants;

import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.entities.cms.FieldType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertFalse(lectureFields.get("nameEn").required());

        var groupFields = registry.resolve(ElectiveGroupCollectionSchema.KEY).schema().fields().stream()
                .collect(java.util.stream.Collectors.toMap(FieldDefinition::name, f -> f));
        assertEquals(FieldType.SHORT_TEXT, groupFields.get("nameEn").type());
        assertFalse(groupFields.get("nameEn").required());
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
}
