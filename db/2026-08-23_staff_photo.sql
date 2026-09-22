-- Personel fotografi (opsiyonel). Hoca istemezse bos kalir, arayuz bas harfleri gosterir.
-- Kolonlari Hibernate (ddl-auto=update) kendisi ekler; bu script elle kurulum icin.
-- Gorsel POST /api/cms/media ile yuklenir, donen url photo_url'e yazilir.

BEGIN;

ALTER TABLE staff ADD COLUMN IF NOT EXISTS photo_url VARCHAR(255);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS photo_alt VARCHAR(255);

COMMIT;

-- Kontrol: kac personelin fotografi var
--   SELECT count(*) FILTER (WHERE photo_url IS NOT NULL) AS fotografli, count(*) AS toplam FROM staff;
