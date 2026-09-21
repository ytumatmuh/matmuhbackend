package com.matmuh.matmuhsite.core.utilities.schema;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CmsAttachmentMigrationTest {

    private static final String PREFIX = "cms-attachment-migration-test-";
    private static final String UPLOADS = "https://matmuh.example/api/uploads/public/" + PREFIX;
    private static final String EXTERNAL = "https://mtm.yildiz.edu.tr/docs/" + PREFIX + "form.pdf";

    @Autowired
    private CmsAttachmentMigration migration;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM collection_items WHERE slug LIKE ?", PREFIX + "%");
        jdbc.update("DELETE FROM collection_drafts WHERE slug LIKE ?", PREFIX + "%");
        jdbc.update("DELETE FROM media WHERE file_url LIKE ?", "public/" + PREFIX + "%");
    }


    @Test
    void wrapsLegacyRowsBackfillsMediaAndIsIdempotent() {
        insertItem("announcements", "full", """
                [{"url": "%1$saaa.pdf", "name": "Dilekçe.pdf", "previewUrl": "%1$saaa-preview.pdf",
                  "type": "application/pdf", "size": 1234}]""".formatted(UPLOADS));
        insertItem("announcements", "nameless", """
                [{"url": "%1$sbbb.docx", "type": "application/vnd.openxmlformats-officedocument.wordprocessingml.document"}]"""
                .formatted(UPLOADS));
        insertItem("news", "external", """
                [{"url": "%s", "name": "form.pdf", "type": "application/pdf", "size": 99}]""".formatted(EXTERNAL));
        insertItem("announcements", "already-new", """
                [{"file": {"url": "%1$sddd.pdf", "name": "d.pdf", "mime": "application/pdf", "size": 10}}]"""
                .formatted(UPLOADS));
        insertItem("announcements", "empty", "[]");
        insertItem("lectures", "lecture", """
                [{"url": "%1$sfff.pdf", "name": "f.pdf"}]""".formatted(UPLOADS));
        insertItem("news", "duplicate-key", """
                [{"url": "%1$saaa.pdf", "name": "other.pdf", "type": "application/pdf", "size": 1234}]"""
                .formatted(UPLOADS));
        insertDraft("announcements", "full", """
                [{"url": "%1$saaa.pdf", "name": "Dilekçe.pdf", "type": "application/pdf", "size": 1234}]"""
                .formatted(UPLOADS));
        insertItem("announcements", "urlless", """
                [{"name": "ve ekler", "type": "exe", "size": 2651719460595747840},
                 {"url": null, "name": "null-url"},
                 {"file": {"name": "already-wrapped-but-empty", "mime": ""}},
                 {"url": "%1$sggg.pdf", "name": "kalan.pdf"}]""".formatted(UPLOADS));
        insertDraft("announcements", "urlless", """
                [{"name": "ve ekler"}]""");

        var first = migration.migrate();

        assertEquals(6, first.items());
        assertEquals(2, first.drafts());
        assertEquals(4, first.media());

        assertAttachments("collection_items", "data", "full", """
                [{"file": {"url": "%1$saaa.pdf", "name": "Dilekçe.pdf", "previewUrl": "%1$saaa-preview.pdf",
                           "mime": "application/pdf", "size": 1234}}]""".formatted(UPLOADS));
        assertAttachments("collection_items", "data", "nameless", """
                [{"file": {"url": "%1$sbbb.docx", "name": "%2$sbbb.docx",
                           "mime": "application/vnd.openxmlformats-officedocument.wordprocessingml.document"}}]"""
                .formatted(UPLOADS, PREFIX));
        assertAttachments("collection_items", "data", "external", """
                [{"file": {"url": "%s", "name": "form.pdf", "mime": "application/pdf", "size": 99}}]""".formatted(EXTERNAL));
        assertAttachments("collection_items", "data", "already-new", """
                [{"file": {"url": "%1$sddd.pdf", "name": "d.pdf", "mime": "application/pdf", "size": 10}}]"""
                .formatted(UPLOADS));
        assertAttachments("collection_items", "data", "empty", "[]");
        assertAttachments("collection_items", "data", "lecture", """
                [{"url": "%1$sfff.pdf", "name": "f.pdf"}]""".formatted(UPLOADS));
        assertAttachments("collection_items", "data", "urlless", """
                [{"file": {"url": "%1$sggg.pdf", "name": "kalan.pdf"}}]""".formatted(UPLOADS));
        assertAttachments("collection_drafts", "payload", "urlless", "[]");
        assertAttachments("collection_drafts", "payload", "full", """
                [{"file": {"url": "%1$saaa.pdf", "name": "Dilekçe.pdf", "mime": "application/pdf", "size": 1234}}]"""
                .formatted(UPLOADS));

        var full = mediaRow("aaa.pdf");
        assertEquals("FILE", full.get("media_type"));
        assertEquals("Dilekçe.pdf", full.get("file_name"));
        assertEquals("application/pdf", full.get("file_type"));
        assertEquals(1234L, ((Number) full.get("file_size")).longValue());
        assertEquals("public/" + PREFIX + "aaa-preview.pdf", full.get("preview_url"));
        assertEquals(false, full.get("is_deleted"));

        var nameless = mediaRow("bbb.docx");
        assertEquals(PREFIX + "bbb.docx", nameless.get("file_name"));
        assertNull(nameless.get("file_size"));
        assertNull(nameless.get("preview_url"));

        assertEquals("d.pdf", mediaRow("ddd.pdf").get("file_name"));
        assertFalse(mediaExists("fff.pdf"));
        assertEquals(4, jdbc.queryForObject("SELECT count(*) FROM media WHERE file_url LIKE ?", Integer.class, "public/" + PREFIX + "%"));

        var second = migration.migrate();

        assertFalse(second.changed());
        assertEquals(4, jdbc.queryForObject("SELECT count(*) FROM media WHERE file_url LIKE ?", Integer.class, "public/" + PREFIX + "%"));
    }


    private void insertItem(String collectionKey, String slugSuffix, String attachments) {
        jdbc.update("""
                INSERT INTO collection_items (id, collection_key, slug, data, updated_by, is_archived, version, created_at, updated_at)
                VALUES (?, ?, ?, ?::jsonb, 'test', false, 1, now(), now())
                """, UUID.randomUUID(), collectionKey, PREFIX + slugSuffix, data(attachments));
    }


    private void insertDraft(String collectionKey, String slugSuffix, String attachments) {
        jdbc.update("""
                INSERT INTO collection_drafts (id, collection_key, slug, user_id, is_new, payload, created_at, updated_at)
                VALUES (?, ?, ?, 'test', false, ?::jsonb, now(), now())
                """, UUID.randomUUID(), collectionKey, PREFIX + slugSuffix, data(attachments));
    }


    private static String data(String attachments) {
        return "{\"title\": \"t\", \"attachments\": " + attachments + "}";
    }


    private void assertAttachments(String table, String column, String slugSuffix, String expected) {
        var actual = jdbc.queryForObject(
                "SELECT " + column + "->'attachments' FROM " + table + " WHERE slug = ?", String.class, PREFIX + slugSuffix);
        var equal = jdbc.queryForObject("SELECT ?::jsonb = ?::jsonb", Boolean.class, actual, expected);
        assertTrue(Boolean.TRUE.equals(equal), () -> slugSuffix + " expected " + expected + " but was " + actual);
    }


    private Map<String, Object> mediaRow(String keySuffix) {
        return jdbc.queryForMap("SELECT * FROM media WHERE file_url = ?", "public/" + PREFIX + keySuffix);
    }


    private boolean mediaExists(String keySuffix) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM media WHERE file_url = ?)", Boolean.class, "public/" + PREFIX + keySuffix));
    }
}
