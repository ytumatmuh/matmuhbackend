-- Ders yariyili artik "bilinmiyor" degerini tasiyabiliyor (entity'de int -> Integer).
-- Onceden ilkel tip yuzunden yariyili girilmemis dersler 0 olarak kaydediliyordu ve
-- guncelleme dogrulamasi (@Min(1)) bu satirlari reddediyordu: CMS'ten hic duzenlenemiyorlardi.
-- Bu script eski 0 degerlerini NULL yapar. Yariyillar 1-8 arasi oldugu icin 0 zaten
-- "girilmemis" anlamina geliyordu, bilgi kaybi yok.
--
-- Deploy SONRASI bir kez calistirilmali, tekrar calistirilabilir.

BEGIN;

UPDATE lectures SET term = NULL WHERE term = 0;

COMMIT;

-- Kontrol: kac ders yariyilsiz kaldi
--   SELECT count(*) FROM lectures WHERE term IS NULL;
