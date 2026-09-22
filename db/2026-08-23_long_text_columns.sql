-- Semada LongText olarak tanimli alanlar varchar(255) olarak olusmustu; ders icerigi
-- gibi uzun metinler kaydedilirken "value too long for type character varying(255)"
-- hatasi veriyordu. Kolonlari TEXT'e cikarir.
--
-- Deploy SONRASI bir kez calistirilmali. Hibernate mevcut kolonun tipini kendiliginden
-- genisletmiyor, o yuzden bu script gerekli. Tekrar calistirilabilir.

BEGIN;

ALTER TABLE lectures ALTER COLUMN about TYPE TEXT;
ALTER TABLE lectures ALTER COLUMN grading_policy TYPE TEXT;
ALTER TABLE lectures ALTER COLUMN resources TYPE TEXT;

ALTER TABLE elective_groups ALTER COLUMN about TYPE TEXT;

COMMIT;

-- Kontrol: artik sinirsiz mi
--   SELECT table_name, column_name, character_maximum_length
--     FROM information_schema.columns
--     WHERE (table_name, column_name) IN
--           (('lectures','about'),('lectures','grading_policy'),('lectures','resources'),
--            ('elective_groups','about'));
