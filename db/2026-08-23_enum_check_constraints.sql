-- Enum kolonlarindaki CHECK kisitlarini mevcut Java enum degerlerine gore yeniden yazar.
-- Kisit adina guvenmez: ilgili kolondaki mevcut CHECK kisitlarini bulup dusurur,
-- sonra dogru listeyle yeniden ekler. Olmayan tablolari atlar, tekrar calistirilabilir.
--
-- ARTIK UYGULAMA BUNU ACILISTA KENDISI YAPIYOR: EnumCheckConstraintSynchronizer
-- (core/utilities/schema) listeyi varlik siniflarindan yansimayla turetir, Hibernate
-- DDL'inden sonra farkli olan her kisiti yeniden yazar; deploy yeterlidir. Bu dosya
-- belgeleme ve elle yedek plan olarak duruyor; asagidaki liste enum buyudugunde
-- guncellenmezse bayatlar (media.media_type ayirici kolondur, uygulama ona dokunmaz;
-- lectures.degree_level 2026-08-22 migration'inin dusurdugu eski kolondur).

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
        ('lecture_notes', 'type', ARRAY['LECTURE_NOTE', 'SUMMARY', 'PAST_EXAM', 'SAMPLE_QUESTION', 'SOLUTION', 'HOMEWORK', 'PROJECT', 'LAB_REPORT', 'PRESENTATION', 'CHEAT_SHEET', 'FORMULA_SHEET', 'BOOK', 'ARTICLE', 'VIDEO_LINK', 'SYLLABUS', 'OTHER']),
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
