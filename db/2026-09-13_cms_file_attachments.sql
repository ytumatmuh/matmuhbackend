-- inscribed 4.4.0 File alan tipi: duyuru/haber ekleri {url, name, previewUrl, type, size}
-- düzünden {file: {url, name, mime, size, previewUrl}} biçimine taşınır.
-- Tekrar çalıştırılabilir: yalnız 'url' taşıyıp 'file' taşımayan satırlar dönüştürülür.
-- Sıra önemli değil; ama content_blocks.block_type için 2026-09-03_block_type_check.sql
-- de çalıştırılmalı (FILE eklendi), yoksa sayfa manifest'i File blok taşıyınca sync 400 döner.
--
-- Uygulama 2, 3 ve 4 numaralı blokları her açılışta kendisi koşturuyor
-- (core/utilities/schema/CmsAttachmentMigration, ApplicationRunner). Bu dosya belge ve
-- elle çalıştırma yedeği olarak duruyor; deploy tek başına yeterli.

-- 1) CMS yüklemelerinin dizin kaydı ve önizleme anahtarı.
--    Hibernate (ddl-auto=update) kolonu kendisi de ekler; blok çalışmazsa bir şey bozulmaz.
ALTER TABLE media ADD COLUMN IF NOT EXISTS preview_url VARCHAR(255);

-- 2) Yayınlanmış kayıtlar.
UPDATE collection_items ci
SET data = jsonb_set(ci.data, '{attachments}', (
    SELECT jsonb_agg(
        CASE
            WHEN att ? 'file' OR NOT (att ? 'url') THEN att
            ELSE jsonb_build_object('file', jsonb_strip_nulls(jsonb_build_object(
                'url', att->'url',
                'name', COALESCE(att->'name', to_jsonb(regexp_replace(att->>'url', '^.*/', ''))),
                'mime', att->'type',
                'size', att->'size',
                'previewUrl', att->'previewUrl')))
        END)
    FROM jsonb_array_elements(ci.data->'attachments') AS att))
WHERE ci.collection_key IN ('announcements', 'news')
  AND jsonb_typeof(ci.data->'attachments') = 'array'
  AND EXISTS (SELECT 1 FROM jsonb_array_elements(ci.data->'attachments') a
              WHERE a ? 'url' AND NOT (a ? 'file'));

-- 3) Taslaklar: aynı dönüşüm, yoksa editör taslağı açınca eski satırlar boş görünür
--    ve kaydedince sessizce düşer.
UPDATE collection_drafts cd
SET payload = jsonb_set(cd.payload, '{attachments}', (
    SELECT jsonb_agg(
        CASE
            WHEN att ? 'file' OR NOT (att ? 'url') THEN att
            ELSE jsonb_build_object('file', jsonb_strip_nulls(jsonb_build_object(
                'url', att->'url',
                'name', COALESCE(att->'name', to_jsonb(regexp_replace(att->>'url', '^.*/', ''))),
                'mime', att->'type',
                'size', att->'size',
                'previewUrl', att->'previewUrl')))
        END)
    FROM jsonb_array_elements(cd.payload->'attachments') AS att))
WHERE cd.collection_key IN ('announcements', 'news')
  AND jsonb_typeof(cd.payload->'attachments') = 'array'
  AND EXISTS (SELECT 1 FROM jsonb_array_elements(cd.payload->'attachments') a
              WHERE a ? 'url' AND NOT (a ? 'file'));

-- 3b) Adresi olmayan ekler düşer (kayıt + taslak): editörde "Boş öğe" olarak durup kaydı tıkıyorlardı.
UPDATE collection_items t
SET data = jsonb_set(t.data, '{attachments}', COALESCE((
    SELECT jsonb_agg(att) FROM jsonb_array_elements(t.data->'attachments') att
    WHERE COALESCE(att->'file'->>'url', '') <> ''), '[]'::jsonb))
WHERE t.collection_key IN ('announcements', 'news')
  AND jsonb_typeof(t.data->'attachments') = 'array'
  AND EXISTS (SELECT 1 FROM jsonb_array_elements(t.data->'attachments') a WHERE COALESCE(a->'file'->>'url', '') = '');

UPDATE collection_drafts t
SET payload = jsonb_set(t.payload, '{attachments}', COALESCE((
    SELECT jsonb_agg(att) FROM jsonb_array_elements(t.payload->'attachments') att
    WHERE COALESCE(att->'file'->>'url', '') <> ''), '[]'::jsonb))
WHERE t.collection_key IN ('announcements', 'news')
  AND jsonb_typeof(t.payload->'attachments') = 'array'
  AND EXISTS (SELECT 1 FROM jsonb_array_elements(t.payload->'attachments') a WHERE COALESCE(a->'file'->>'url', '') = '');

-- 4) Eski CMS yüklemelerine media satırı. /api/cms/media bugüne kadar veritabanına
--    yazmıyordu; previewUrl artık kaydetmede media tablosundan bulunduğu için, elle
--    yazılmış eski önizlemeler ancak burada bir satırı varsa yeniden kaydetmeyi atlatır.
--    Dış adresler (kurumsal sunucu, video) atlanır: onların dizin kaydı yok.
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
    ORDER BY key, (f ? 'previewUrl') DESC
) legacy
WHERE NOT EXISTS (SELECT 1 FROM media m WHERE m.file_url = legacy.key);

-- Doğrulama: eski biçimde kalan satır olmamalı.
SELECT ci.collection_key, ci.slug
FROM collection_items ci, jsonb_array_elements(ci.data->'attachments') a
WHERE ci.collection_key IN ('announcements', 'news')
  AND jsonb_typeof(ci.data->'attachments') = 'array'
  AND a ? 'url' AND NOT (a ? 'file');

SELECT count(*) AS onizlemeli_cms_dosyasi FROM media WHERE media_type = 'FILE' AND preview_url IS NOT NULL;
