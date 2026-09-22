-- Ders katalogu icin eklenen alanlar: ders turu, YOK kategorisi, haftalik program,
-- sinav agirliklari ve teori/uygulama/laboratuvar saat bilesenleri.
-- Kolonlari ve lecture_syllabus tablosunu Hibernate (ddl-auto=update) kendisi olusturur;
-- bu script elle kurulum ve dogrulama icin.

BEGIN;

ALTER TABLE lectures ADD COLUMN IF NOT EXISTS type VARCHAR(20);
ALTER TABLE lectures ADD COLUMN IF NOT EXISTS category VARCHAR(32);
ALTER TABLE lectures ADD COLUMN IF NOT EXISTS midterm_weight INTEGER;
ALTER TABLE lectures ADD COLUMN IF NOT EXISTS final_weight INTEGER;
ALTER TABLE lectures ADD COLUMN IF NOT EXISTS theory_hours INTEGER;
ALTER TABLE lectures ADD COLUMN IF NOT EXISTS practice_hours INTEGER;
ALTER TABLE lectures ADD COLUMN IF NOT EXISTS lab_hours INTEGER;

CREATE TABLE IF NOT EXISTS lecture_syllabus (
    lecture_id UUID NOT NULL REFERENCES lectures(id),
    week INTEGER NOT NULL,
    topic VARCHAR(1000) NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_lecture_syllabus_lecture ON lecture_syllabus (lecture_id);

COMMIT;

-- Kontrol: katalog yuklendikten sonra
--   SELECT type, count(*) FROM lectures GROUP BY type;
--   SELECT category, count(*) FROM lectures GROUP BY category;
--   SELECT count(DISTINCT lecture_id) AS programi_olan_ders, count(*) AS toplam_satir FROM lecture_syllabus;
