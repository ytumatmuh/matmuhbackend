-- Ders notlarına tür alanı. Yeni enum kolonu, ddl-auto=update dolu tabloya NOT NULL
-- kolon ekleyemediği için deploy'DAN ÖNCE çalıştırılmalı. Tekrar çalıştırılabilir.
-- Mevcut kayıtlar OTHER'a düşer: türleri bilinmiyor, uydurmak yanlış olur.

ALTER TABLE lecture_notes ADD COLUMN IF NOT EXISTS type VARCHAR(32);

UPDATE lecture_notes SET type = 'OTHER' WHERE type IS NULL;

ALTER TABLE lecture_notes ALTER COLUMN type SET DEFAULT 'OTHER';
ALTER TABLE lecture_notes ALTER COLUMN type SET NOT NULL;

ALTER TABLE lecture_notes DROP CONSTRAINT IF EXISTS lecture_notes_type_check;
ALTER TABLE lecture_notes ADD CONSTRAINT lecture_notes_type_check
    CHECK (type::text = ANY (ARRAY['LECTURE_NOTE','SUMMARY','PAST_EXAM','SAMPLE_QUESTION','SOLUTION','HOMEWORK','PROJECT','LAB_REPORT','PRESENTATION','CHEAT_SHEET','FORMULA_SHEET','BOOK','ARTICLE','VIDEO_LINK','SYLLABUS','OTHER']::text[]));

-- Doğrulama
SELECT type, COUNT(*) FROM lecture_notes WHERE is_deleted = false GROUP BY type ORDER BY type;
