-- Sinav agirlik dagilimi hocadan hocaya degistigi icin donem kaydinda (offering) tutuluyor.
-- Dersteki midterm_weight / final_weight Bologna varsayilani olarak kaliyor.
-- Tabloyu Hibernate (ddl-auto=update) kendisi olusturur; bu script elle kurulum icin.

BEGIN;

CREATE TABLE IF NOT EXISTS lecture_offering_exam_weights (
    lecture_offering_id UUID NOT NULL REFERENCES lecture_offerings(id),
    exam_type VARCHAR(20) NOT NULL,
    weight_percent INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_offering_exam_weight_offering
    ON lecture_offering_exam_weights (lecture_offering_id);

COMMIT;

-- Kontrol: hangi donem kaydinda hangi agirliklar var
--   SELECT o.academic_year, o.semester, w.exam_type, w.weight_percent
--     FROM lecture_offering_exam_weights w
--     JOIN lecture_offerings o ON o.id = w.lecture_offering_id
--     ORDER BY o.academic_year, o.semester, w.exam_type;
