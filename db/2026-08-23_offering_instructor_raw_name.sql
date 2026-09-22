-- Donem kaydinda egitmen artik zorunlu degil. OBS'de bazi kayitlarda egitmen bos
-- geliyor (Erasmus dersleri, "Atanmamis Egitmen") ve bazilari baska bolumlerin
-- hocalarina ait (ATA, MDB, ITB, TDB) - onlari Matematik Muhendisligi personel
-- dizinine eklemek yanlis olurdu, /personel sayfasi o koleksiyondan besleniyor.
-- Karsiligi olmayan isim serbest metin olarak instructor_raw_name'de duruyor.
--
-- Kolonu Hibernate (ddl-auto=update) kendisi ekler; bu script elle kurulum icin.
-- staff_id zaten nullable oldugu icin ayrica ALTER gerekmiyor.

BEGIN;

ALTER TABLE lecture_offerings ADD COLUMN IF NOT EXISTS instructor_raw_name VARCHAR(255);

COMMIT;

-- Kontrol: kac kayit personel eslesmesi olmadan duruyor
--   SELECT count(*) FILTER (WHERE staff_id IS NOT NULL) AS eslesen,
--          count(*) FILTER (WHERE staff_id IS NULL AND instructor_raw_name IS NOT NULL) AS serbest_metin,
--          count(*) FILTER (WHERE staff_id IS NULL AND instructor_raw_name IS NULL) AS egitmensiz
--     FROM lecture_offerings;
