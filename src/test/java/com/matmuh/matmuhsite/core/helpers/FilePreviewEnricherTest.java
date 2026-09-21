package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.FieldDefinition;
import com.matmuh.matmuhsite.dataAccess.abstracts.FileDao;
import com.matmuh.matmuhsite.entities.File;
import com.matmuh.matmuhsite.entities.cms.FieldType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FilePreviewEnricherTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final String API = "https://matmuh.example";

    private static final CollectionSchema SCHEMA = CollectionSchema.of(
            FieldDefinition.required("title", FieldType.SHORT_TEXT, "Başlık"),
            FieldDefinition.of("brochure", FieldType.FILE, "Broşür"),
            FieldDefinition.of("attachments", FieldType.OBJECT_ARRAY, "Ekler")
                    .withItemFields(List.of(FieldDefinition.required("file", FieldType.FILE, "Dosya"))));

    private final FileDao fileDao = mock(FileDao.class);
    private final StorageUrlResolver resolver = new StorageUrlResolver("https://cdn.example", API);
    private final FilePreviewEnricher enricher = new FilePreviewEnricher(fileDao, resolver);

    private static ObjectNode data(String text) {
        return (ObjectNode) MAPPER.readTree(text);
    }

    private static File stored(String key, String previewKey) {
        var file = new File();
        file.setFileUrl(key);
        file.setPreviewUrl(previewKey);
        return file;
    }

    @Test
    void writesPreviewUrlFromTheMediaRow() {
        when(fileDao.findByFileUrlIn(any())).thenReturn(List.of(stored("public/a.docx", "public/b.pdf")));

        var out = enricher.enrich(SCHEMA, data("""
                {"title": "t", "brochure": {"url": "%s/api/uploads/public/a.docx", "name": "a.docx"}}
                """.formatted(API)));

        assertEquals(API + "/api/uploads/public/b.pdf", out.get("brochure").get("previewUrl").asText());
    }

    // SDK yeniden adlandırmada nesneyi sıfırdan kuruyor; önizleme editörün gönderdiğine
    // değil, tabloya bakılarak yazılır. Tabloda yoksa alan da olmaz.
    @Test
    void removesStalePreviewWhenTheRowHasNone() {
        when(fileDao.findByFileUrlIn(any())).thenReturn(List.of(stored("public/a.docx", null)));

        var out = enricher.enrich(SCHEMA, data("""
                {"title": "t", "brochure": {"url": "%s/api/uploads/public/a.docx", "name": "a", "previewUrl": "https://x/old.pdf"}}
                """.formatted(API)));

        assertFalse(out.get("brochure").has("previewUrl"));
    }

    @Test
    void externalUrlsNeverHitTheDatabase() {
        var out = enricher.enrich(SCHEMA, data("""
                {"title": "t", "brochure": {"url": "https://mtm.yildiz.edu.tr/form.docx", "name": "form"}}
                """));

        verify(fileDao, never()).findByFileUrlIn(any());
        assertFalse(out.get("brochure").has("previewUrl"));
    }

    @Test
    void noFileFieldMeansNoQuery() {
        enricher.enrich(CollectionSchema.of(FieldDefinition.required("title", FieldType.SHORT_TEXT, "Başlık")),
                data("{\"title\": \"t\"}"));
        verify(fileDao, never()).findByFileUrlIn(any());
    }

    // Liste uçlarındaki kural burada da geçerli: ek başına değil, kayıt başına tek sorgu.
    @Test
    void resolvesEveryAttachmentInOneQuery() {
        when(fileDao.findByFileUrlIn(any())).thenReturn(List.of(
                stored("public/1.docx", "public/1p.pdf"),
                stored("public/2.xlsx", "public/2p.pdf")));

        var out = enricher.enrich(SCHEMA, data("""
                {"title": "t", "attachments": [
                    {"file": {"url": "%1$s/api/uploads/public/1.docx", "name": "1"}},
                    {"file": {"url": "%1$s/api/uploads/public/2.xlsx", "name": "2"}},
                    {"file": {"url": "%1$s/api/uploads/public/3.pdf", "name": "3"}},
                    {"file": {"url": "https://elsewhere/4.pdf", "name": "4"}}
                ]}
                """.formatted(API)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> asked = ArgumentCaptor.forClass(Collection.class);
        verify(fileDao).findByFileUrlIn(asked.capture());
        assertEquals(Set.of("public/1.docx", "public/2.xlsx", "public/3.pdf"), Set.copyOf(asked.getValue()));

        var rows = out.get("attachments");
        assertEquals(API + "/api/uploads/public/1p.pdf", rows.get(0).get("file").get("previewUrl").asText());
        assertEquals(API + "/api/uploads/public/2p.pdf", rows.get(1).get("file").get("previewUrl").asText());
        assertFalse(rows.get(2).get("file").has("previewUrl"));
        assertFalse(rows.get(3).get("file").has("previewUrl"));
        assertTrue(out.get("title").isTextual());
    }
}
