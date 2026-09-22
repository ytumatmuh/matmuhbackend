package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.entities.cms.FieldType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionSchemaValidatorTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private static final CollectionSchema WITH_FILE = CollectionSchema.of(
            FieldDefinition.of("brochure", FieldType.FILE, "Broşür"));

    private static final CollectionSchema WITH_ATTACHMENTS = CollectionSchema.of(
            FieldDefinition.of("attachments", FieldType.OBJECT_ARRAY, "Ekler")
                    .withItemFields(List.of(FieldDefinition.required("file", FieldType.FILE, "Dosya"))));

    private static tools.jackson.databind.JsonNode json(String text) {
        return MAPPER.readTree(text);
    }

    private static List<String> errorsOf(CollectionSchema schema, String text) {
        var ex = assertThrows(CmsValidationException.class,
                () -> CollectionSchemaValidator.validateAndStrip(schema, json(text)));
        return ex.getErrors();
    }

    // SDK'nın yazdığı şekil: { url, name, mime, size }. mime bilinmeyen uzantıda boş gelir.
    @Test
    void acceptsTheSdkFileShape() {
        var cleaned = CollectionSchemaValidator.validateAndStrip(WITH_FILE, json("""
                {"brochure": {"url": "https://x/api/uploads/public/a.pdf", "name": "a.pdf", "mime": "", "size": 1234}}
                """));
        var file = cleaned.get("brochure");
        assertEquals("a.pdf", file.get("name").asText());
        assertEquals(1234, file.get("size").asInt());
        assertEquals("", file.get("mime").asText());
    }

    // SDK ba804fc: elle yazılan adres (kurumsal sunucu, video) mime "" ve size null taşır.
    @Test
    void acceptsATypedAddressWithoutMimeOrSize() {
        var cleaned = CollectionSchemaValidator.validateAndStrip(WITH_FILE, json("""
                {"brochure": {"url": "https://mtm.yildiz.edu.tr/form.docx", "name": "Form", "mime": "", "size": null}}
                """));
        var file = cleaned.get("brochure");
        assertEquals("Form", file.get("name").asText());
        assertFalse(file.has("size"));
    }

    // previewUrl backend'in alanı: editörden gelen değer atılır, kaydetmede enricher yazar.
    @Test
    void dropsClientSuppliedPreviewUrlWithoutComplaining() {
        var cleaned = CollectionSchemaValidator.validateAndStrip(WITH_FILE, json("""
                {"brochure": {"url": "https://x/f.docx", "name": "f.docx", "previewUrl": "https://evil/other.pdf"}}
                """));
        assertFalse(cleaned.get("brochure").has("previewUrl"));
    }

    @Test
    void urlAndNameAreRequiredOnceTheFileExists() {
        var errors = errorsOf(WITH_FILE, """
                {"brochure": {"mime": "application/pdf", "size": 1}}
                """);
        assertTrue(errors.contains("Field 'brochure.url' is required."), errors.toString());
        assertTrue(errors.contains("Field 'brochure.name' is required."), errors.toString());
    }

    @Test
    void rejectsUnknownKeysInsideTheFile() {
        var errors = errorsOf(WITH_FILE, """
                {"brochure": {"url": "https://x/f.pdf", "name": "f.pdf", "type": "pdf"}}
                """);
        assertEquals(List.of("Unknown field 'brochure.type'."), errors);
    }

    // SDK boş File'ı null olarak yollar; opsiyonel alanda bu "yok" demektir.
    @Test
    void nullFileIsAbsence() {
        var cleaned = CollectionSchemaValidator.validateAndStrip(WITH_FILE, json("""
                {"brochure": null}
                """));
        assertFalse(cleaned.has("brochure"));
    }

    @Test
    void walksIntoObjectArrayRowsWithIndexedPaths() {
        var errors = errorsOf(WITH_ATTACHMENTS, """
                {"attachments": [
                    {"file": {"url": "https://x/a.pdf", "name": "a.pdf"}},
                    {"file": {"name": "eksik.pdf"}},
                    {}
                ]}
                """);
        assertEquals(List.of(
                "Field 'attachments[1].file.url' is required.",
                "Field 'attachments[2].file' is required."), errors);
    }

    // Eski düz biçim ({url, name, ...} doğrudan satırda) artık kabul edilmez;
    // migration bunu {file: {...}} yapmalı.
    @Test
    void legacyFlatAttachmentRowIsRejected() {
        var errors = errorsOf(WITH_ATTACHMENTS, """
                {"attachments": [{"url": "https://x/a.pdf", "name": "a.pdf"}]}
                """);
        assertTrue(errors.contains("Field 'attachments[0].file' is required."), errors.toString());
        assertTrue(errors.contains("Unknown field 'attachments[0].url'."), errors.toString());
    }

    // Bologna haftalık konuları 300-500 karakterlik cümleler; SHORT_TEXT 255'te kesiyordu ve
    // bot 299 dersin yarısını yazamıyordu (Egehan, 22 Eylül).
    @Test
    void longTextFieldsAreNotCappedAt255() {
        var schema = CollectionSchema.of(
                FieldDefinition.of("syllabus", FieldType.OBJECT_ARRAY, "Haftalık program")
                        .withItemFields(List.of(
                                FieldDefinition.required("week", FieldType.NUMBER, "Hafta"),
                                FieldDefinition.required("topic", FieldType.LONG_TEXT, "Konu"))));
        var topic = "Konu ".repeat(120).trim();

        var cleaned = CollectionSchemaValidator.validateAndStrip(schema, json("""
                {"syllabus": [{"week": 1, "topic": "%s"}]}
                """.formatted(topic)));

        assertEquals(topic, cleaned.get("syllabus").get(0).get("topic").asText());
    }
}
