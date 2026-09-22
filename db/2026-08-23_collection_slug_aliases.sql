-- Slug yeniden adlandirma icin alias tablosu. Eski slug silinmiyor, item'a isaret eden
-- bir alias olarak kaliyor; boylece yayinlanmis linkler ve arama motoru indeksi kirilmiyor.
-- Tabloyu Hibernate (ddl-auto=update) kendisi olusturur, bu script sadece elle kurulum icin.
--
-- Alias'lar koleksiyon basina benzersizdir ve zincirlenmez: a -> b -> c yeniden adlandirmasi
-- iki ayri alias birakir, ikisi de dogrudan item'a bakar.

BEGIN;

CREATE TABLE IF NOT EXISTS collection_slug_aliases (
    id UUID PRIMARY KEY,
    collection_key VARCHAR(100) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    item_id UUID NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_collection_slug_alias
    ON collection_slug_aliases (collection_key, slug);

CREATE INDEX IF NOT EXISTS ix_collection_slug_alias_item
    ON collection_slug_aliases (item_id);

COMMIT;

-- Kontrol: hangi eski adresler hangi item'a bakiyor
--   SELECT a.collection_key, a.slug AS eski, i.slug AS guncel
--     FROM collection_slug_aliases a JOIN collection_items i ON i.id = a.item_id
--     ORDER BY a.collection_key, a.slug;
