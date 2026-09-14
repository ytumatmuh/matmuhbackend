package com.matmuh.matmuhsite.core.utilities.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
@Transactional
public class CmsAttachmentMigration implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(CmsAttachmentMigration.class);

    private static final String TABLES_EXIST = """
            SELECT to_regclass('collection_items') IS NOT NULL
               AND to_regclass('collection_drafts') IS NOT NULL
               AND to_regclass('media') IS NOT NULL
            """;

    private static final String WRAP_ATTACHMENTS = """
            UPDATE %1$s t
            SET %2$s = jsonb_set(t.%2$s, '{attachments}', (
                SELECT jsonb_agg(
                    CASE
                        WHEN jsonb_exists(att, 'file') OR NOT jsonb_exists(att, 'url') THEN att
                        ELSE jsonb_build_object('file', jsonb_strip_nulls(jsonb_build_object(
                            'url', att->'url',
                            'name', COALESCE(att->'name', to_jsonb(regexp_replace(att->>'url', '^.*/', ''))),
                            'mime', att->'type',
                            'size', att->'size',
                            'previewUrl', att->'previewUrl')))
                    END)
                FROM jsonb_array_elements(t.%2$s->'attachments') AS att))
            WHERE t.collection_key IN ('announcements', 'news')
              AND jsonb_typeof(t.%2$s->'attachments') = 'array'
              AND EXISTS (SELECT 1 FROM jsonb_array_elements(t.%2$s->'attachments') a
                          WHERE jsonb_exists(a, 'url') AND NOT jsonb_exists(a, 'file'))
            """;

    // Adresi olmayan ek, ek değildir: editörde "Boş öğe" olarak durur, Dosya zorunlu olduğu için
    // kayıt tıkanır, sitede de çöp olarak çizilir. Prod'da böyle "ad var, url yok" eski satırlar çıktı.
    private static final String LIST_URLLESS = """
            SELECT t.slug, COALESCE(att->'file'->>'name', att->>'name', '?') AS name
            FROM %1$s t, jsonb_array_elements(t.%2$s->'attachments') att
            WHERE t.collection_key IN ('announcements', 'news')
              AND jsonb_typeof(t.%2$s->'attachments') = 'array'
              AND COALESCE(att->'file'->>'url', '') = ''
            """;

    private static final String DROP_URLLESS = """
            UPDATE %1$s t
            SET %2$s = jsonb_set(t.%2$s, '{attachments}', COALESCE((
                SELECT jsonb_agg(att)
                FROM jsonb_array_elements(t.%2$s->'attachments') att
                WHERE COALESCE(att->'file'->>'url', '') <> ''), '[]'::jsonb))
            WHERE t.collection_key IN ('announcements', 'news')
              AND jsonb_typeof(t.%2$s->'attachments') = 'array'
              AND EXISTS (SELECT 1 FROM jsonb_array_elements(t.%2$s->'attachments') a
                          WHERE COALESCE(a->'file'->>'url', '') = '')
            """;

    private static final String BACKFILL_MEDIA = """
            INSERT INTO media (id, media_type, file_name, file_type, file_url, file_size, preview_url, is_deleted, created_at)
            SELECT gen_random_uuid(), 'FILE', name, mime, key, size, preview_key, false, now()
            FROM (
                SELECT DISTINCT ON (key)
                    substring(f->>'url' FROM '/api/uploads/(.*)$')        AS key,
                    substring(f->>'previewUrl' FROM '/api/uploads/(.*)$') AS preview_key,
                    f->>'name'                                            AS name,
                    f->>'mime'                                            AS mime,
                    NULLIF(f->>'size', '')::numeric::bigint               AS size
                FROM collection_items ci,
                     jsonb_array_elements(ci.data->'attachments') att,
                     LATERAL (SELECT att->'file' AS f) x
                WHERE ci.collection_key IN ('announcements', 'news')
                  AND jsonb_typeof(ci.data->'attachments') = 'array'
                  AND f->>'url' LIKE '%/api/uploads/%'
                ORDER BY key, jsonb_exists(f, 'previewUrl') DESC
            ) legacy
            WHERE NOT EXISTS (SELECT 1 FROM media m WHERE m.file_url = legacy.key)
            """;

    private final JdbcTemplate jdbcTemplate;

    public CmsAttachmentMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    // Hata yutulmaz: göç etmemiş veriyle ayakta kalan uygulama ilk kaydetmede ekleri kaybeder,
    // başarısız deploy bundan daha güvenli.
    @Override
    public void run(ApplicationArguments args) {
        var result = migrate();
        if (result.changed()) {
            logger.info("CMS attachment migration: {} items, {} drafts, {} media rows", result.items(), result.drafts(), result.media());
        } else {
            logger.debug("CMS attachment migration: nothing to do");
        }
    }


    public Result migrate() {
        if (!Boolean.TRUE.equals(jdbcTemplate.queryForObject(TABLES_EXIST, Boolean.class))) {
            return new Result(0, 0, 0);
        }
        int items = jdbcTemplate.update(WRAP_ATTACHMENTS.formatted("collection_items", "data"))
                + dropUrlless("collection_items", "data");
        int drafts = jdbcTemplate.update(WRAP_ATTACHMENTS.formatted("collection_drafts", "payload"))
                + dropUrlless("collection_drafts", "payload");
        int media = jdbcTemplate.update(BACKFILL_MEDIA);
        return new Result(items, drafts, media);
    }

    private int dropUrlless(String table, String column) {
        var urlless = jdbcTemplate.query(LIST_URLLESS.formatted(table, column),
                (rs, i) -> rs.getString("slug") + " → " + rs.getString("name"));
        if (urlless.isEmpty()) {
            return 0;
        }
        logger.warn("Dropping {} attachment(s) without a url from {}: {}", urlless.size(), table, urlless);
        return jdbcTemplate.update(DROP_URLLESS.formatted(table, column));
    }


    public record Result(int items, int drafts, int media) {

        public boolean changed() {
            return items + drafts + media > 0;
        }
    }
}
