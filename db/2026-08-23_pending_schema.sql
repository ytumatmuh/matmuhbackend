-- Bekleyen tum sema degisikliklerini tek seferde uygular. Tekrar calistirilabilir:
-- her adim IF NOT EXISTS / kosullu, ikinci kez calistirmak hicbir sey bozmaz.
-- Deploy'DAN ONCE calistir: ddl-auto=update dolu tabloya NOT NULL kolon ekleyemez.
-- Katalog verisine bagli olanlar (elective_groups, lecture_degree_levels) BURADA DEGIL.

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

CREATE TABLE IF NOT EXISTS lecture_offering_exam_weights (
    lecture_offering_id UUID NOT NULL REFERENCES lecture_offerings(id),
    exam_type VARCHAR(20) NOT NULL,
    weight_percent INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS ix_offering_exam_weight_offering
    ON lecture_offering_exam_weights (lecture_offering_id);

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

ALTER TABLE lecture_offerings ADD COLUMN IF NOT EXISTS instructor_raw_name VARCHAR(255);
ALTER TABLE lecture_offerings ALTER COLUMN staff_id DROP NOT NULL;

ALTER TABLE staff ADD COLUMN IF NOT EXISTS photo_url VARCHAR(255);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS photo_alt VARCHAR(255);

ALTER TABLE grade_distributions ALTER COLUMN min_score DROP NOT NULL;
ALTER TABLE grade_distributions ALTER COLUMN max_score DROP NOT NULL;

ALTER TABLE lectures ALTER COLUMN term DROP NOT NULL;
UPDATE lectures SET term = NULL WHERE term = 0;

DO $longtext$
BEGIN
    ALTER TABLE lectures ALTER COLUMN about TYPE TEXT;
    ALTER TABLE lectures ALTER COLUMN grading_policy TYPE TEXT;
    ALTER TABLE lectures ALTER COLUMN resources TYPE TEXT;
    IF to_regclass('public.elective_groups') IS NOT NULL THEN
        ALTER TABLE elective_groups ALTER COLUMN about TYPE TEXT;
    END IF;
END
$longtext$;

ALTER TABLE lecture_notes ADD COLUMN IF NOT EXISTS review_status VARCHAR(16);
UPDATE lecture_notes
SET review_status = CASE
        WHEN is_approved IS TRUE THEN 'APPROVED'
        WHEN is_approved IS FALSE AND approved_by_id IS NOT NULL THEN 'REJECTED'
        ELSE 'PENDING'
    END
WHERE review_status IS NULL;
ALTER TABLE lecture_notes ALTER COLUMN review_status SET DEFAULT 'PENDING';
ALTER TABLE lecture_notes ALTER COLUMN review_status SET NOT NULL;

DO $$
DECLARE
    item     RECORD;
    old_name TEXT;
BEGIN
    FOR item IN
        SELECT * FROM (VALUES
        ('authorities', 'role', ARRAY['ROLE_ADMIN', 'ROLE_EDITOR', 'ROLE_USER']),
        ('academic_terms', 'semester', ARRAY['FALL', 'SPRING', 'SUMMER']),
        ('calendar_events', 'exam_type', ARRAY['MIDTERM_1', 'MIDTERM_2', 'MIDTERM_1_MAKEUP', 'MIDTERM_2_MAKEUP', 'FINAL', 'RESIT', 'QUIZ', 'ASSIGNMENT', 'PROJECT']),
        ('calendar_events', 'type', ARRAY['EXAM', 'ACADEMIC', 'HOLIDAY', 'EVENT']),
        ('content_blocks', 'block_type', ARRAY['SHORT_TEXT', 'LONG_TEXT', 'RICH_TEXT', 'NUMBER', 'BOOL', 'URL', 'DATE', 'IMAGE', 'FILE', 'LINK', 'SELECT', 'STRING_ARRAY', 'OBJECT_ARRAY', 'COLLECTION', 'LIST']),
        ('elective_group_degree_levels', 'degree_level', ARRAY['UNDERGRADUATE', 'MASTERS', 'DOCTORATE']),
        ('elective_groups', 'semester', ARRAY['FALL', 'SPRING', 'SUMMER']),
        ('exam_statistics', 'exam_type', ARRAY['MIDTERM_1', 'MIDTERM_2', 'MIDTERM_1_MAKEUP', 'MIDTERM_2_MAKEUP', 'FINAL', 'RESIT', 'QUIZ', 'ASSIGNMENT', 'PROJECT']),
        ('grade_results', 'evaluation_method', ARRAY['RELATIVE', 'ABSOLUTE', 'MANUAL']),
        ('grade_results', 'exam_period', ARRAY['NORMAL', 'BUT']),
        ('lecture_degree_levels', 'degree_level', ARRAY['UNDERGRADUATE', 'MASTERS', 'DOCTORATE']),
        ('lecture_notes', 'review_status', ARRAY['PENDING', 'APPROVED', 'REJECTED']),
        ('lecture_offering_exam_weights', 'exam_type', ARRAY['MIDTERM_1', 'MIDTERM_2', 'MIDTERM_1_MAKEUP', 'MIDTERM_2_MAKEUP', 'FINAL', 'RESIT', 'QUIZ', 'ASSIGNMENT', 'PROJECT']),
        ('lecture_offerings', 'language', ARRAY['TURKISH', 'ENGLISH']),
        ('lecture_offerings', 'semester', ARRAY['FALL', 'SPRING', 'SUMMER']),
        ('lectures', 'category', ARRAY['BASIC_SCIENCE', 'FOREIGN_LANGUAGE', 'COMMON_REQUIRED', 'CORE_PROFESSION', 'SPECIALIZATION', 'GENERAL_CULTURE']),
        ('lectures', 'degree_level', ARRAY['UNDERGRADUATE', 'MASTERS', 'DOCTORATE']),
        ('lectures', 'semester', ARRAY['FALL', 'SPRING', 'SUMMER']),
        ('lectures', 'type', ARRAY['REQUIRED', 'ELECTIVE']),
        ('media', 'media_type', ARRAY['IMAGE', 'FILE']),
        ('schedule_slots', 'day_of_week', ARRAY['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']),
        ('service_key_capabilities', 'capability', ARRAY['SCHEMA_SYNC', 'CONTENT_WRITE']),
        ('staff_groups', 'staff_group', ARRAY['MANAGEMENT', 'ACADEMIC', 'TEACHING_AND_RESEARCH', 'ADMINISTRATIVE']),
        ('users', 'provider', ARRAY['LOCAL', 'YTU_MAIL'])
        ) AS t(tbl, col, vals)
    LOOP
        IF to_regclass('public.' || item.tbl) IS NULL THEN
            RAISE NOTICE 'atlandi (tablo yok): %', item.tbl;
            CONTINUE;
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = item.tbl AND column_name = item.col
        ) THEN
            RAISE NOTICE 'atlandi (kolon yok): %.%', item.tbl, item.col;
            CONTINUE;
        END IF;

        FOR old_name IN
            SELECT con.conname
            FROM pg_constraint con
            JOIN pg_class rel ON rel.oid = con.conrelid
            JOIN pg_namespace ns ON ns.oid = rel.relnamespace
            WHERE con.contype = 'c'
              AND ns.nspname = 'public'
              AND rel.relname = item.tbl
              -- Kolonu deger bazinda kisitlayan her CHECK'i hedefler: hem
              -- "= ANY (ARRAY[...])" hem de tek degerli "= 'X'" bicimini yakalar.
              AND pg_get_constraintdef(con.oid) LIKE '%(' || item.col || ')::text%'
        LOOP
            EXECUTE format('ALTER TABLE public.%I DROP CONSTRAINT %I', item.tbl, old_name);
        END LOOP;

        EXECUTE format(
            'ALTER TABLE public.%I ADD CONSTRAINT %I CHECK (%I = ANY (%L::text[]))',
            item.tbl, item.tbl || '_' || item.col || '_check', item.col, item.vals);

        RAISE NOTICE 'guncellendi: %.% (% deger)', item.tbl, item.col, array_length(item.vals, 1);
    END LOOP;
END $$;

COMMIT;

-- Dogrulama
SELECT 'review_status' AS kontrol,
       (SELECT count(*) FROM information_schema.columns
         WHERE table_name='lecture_notes' AND column_name='review_status' AND is_nullable='NO') AS ok
UNION ALL
SELECT 'about TEXT',
       (SELECT count(*) FROM information_schema.columns
         WHERE table_name='lectures' AND column_name='about' AND data_type='text')
UNION ALL
SELECT 'ROLE_EDITOR atanabilir',
       (SELECT count(*) FROM pg_constraint con JOIN pg_class rel ON rel.oid=con.conrelid
         WHERE rel.relname='authorities' AND con.contype='c'
           AND pg_get_constraintdef(con.oid) LIKE '%ROLE_EDITOR%');
