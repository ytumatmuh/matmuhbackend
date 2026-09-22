-- Varlıkta @Column(columnDefinition = "TEXT") olan kolonları gerçekten TEXT'e çıkarır.
-- ddl-auto=update mevcut kolonun tipini asla genişletmez: bu alanlar anotasyon eklenmeden
-- önce oluştukları için prod'da varchar(255)/varchar(1000) kaldı ve uzun içerik kaydedilirken
-- "value too long for type character varying(255)" veriyordu.
--
-- ARTIK UYGULAMA BUNU AÇILIŞTA KENDİSİ YAPIYOR: TextColumnWidener (core/utilities/schema,
-- ApplicationRunner @Order(5)) listeyi varlık sınıflarından yansımayla türetir
-- (core/helpers/EntityColumns), tipi text olmayan her kolonu Hibernate DDL'inden sonra
-- genişletir; deploy yeterlidir. Bu dosya belgeleme ve elle yedek plan olarak duruyor;
-- aşağıdaki liste yeni bir TEXT alanı eklendiğinde güncellenmezse bayatlar (uygulamadaki
-- liste bayatlamaz, TextColumnWidenerTest onu literal olarak sabitler).
--
-- Tekrar çalıştırılabilir: tipi zaten text olan kolona dokunmaz. jsonb kolonları
-- (content_blocks.value, collection_items.data, ...) kapsam dışı; onların text'e
-- çevrilmesi veriyi bozar.

DO $$
DECLARE
    item     RECORD;
    old_type TEXT;
BEGIN
    FOR item IN
        SELECT * FROM (VALUES
        ('elective_groups',  'about'),
        ('elective_groups',  'about_en'),
        ('lecture_syllabus', 'topic'),
        ('lecture_syllabus', 'topic_en'),
        ('lectures',         'about'),
        ('lectures',         'about_en'),
        ('lectures',         'grading_policy'),
        ('lectures',         'grading_policy_en'),
        ('lectures',         'resources'),
        ('lectures',         'resources_en')
        ) AS t(tbl, col)
    LOOP
        IF to_regclass('public.' || item.tbl) IS NULL THEN
            RAISE NOTICE 'atlandı (tablo yok): %', item.tbl;
            CONTINUE;
        END IF;

        SELECT data_type INTO old_type
        FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = item.tbl AND column_name = item.col;

        IF old_type IS NULL THEN
            RAISE NOTICE 'atlandı (kolon yok): %.%', item.tbl, item.col;
            CONTINUE;
        END IF;

        IF old_type = 'text' THEN
            CONTINUE;
        END IF;

        EXECUTE format('ALTER TABLE public.%I ALTER COLUMN %I TYPE text', item.tbl, item.col);
        RAISE NOTICE 'genişletildi: %.% (% -> text)', item.tbl, item.col, old_type;
    END LOOP;
END $$;

-- Kontrol: hepsi text mi
--   SELECT table_name, column_name, data_type, character_maximum_length
--     FROM information_schema.columns
--     WHERE (table_name, column_name) IN
--           (('lectures','about'),('lectures','about_en'),
--            ('lectures','grading_policy'),('lectures','grading_policy_en'),
--            ('lectures','resources'),('lectures','resources_en'),
--            ('elective_groups','about'),('elective_groups','about_en'),
--            ('lecture_syllabus','topic'),('lecture_syllabus','topic_en'))
--     ORDER BY table_name, column_name;
