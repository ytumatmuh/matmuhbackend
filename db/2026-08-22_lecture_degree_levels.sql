-- Derslere ogrenim duzeyi atar. Tabloyu Hibernate (ddl-auto=update) kendisi olusturur,
-- bu script sadece eski kayitlari doldurur ve eski tek degerli kolonu kaldirir.
-- Deploy SONRASI bir kez calistirilmali.
--
-- Kural (Bologna mufredatindan): bir ders birden fazla programda okutulabilir.
--   1000-4999          -> UNDERGRADUATE
--   5000-5003          -> MASTERS        (YL tezi, seminer, uzmanlik alan dersi)
--   6000-6003          -> DOCTORATE      (doktora tezi, seminer, uzmanlik alan dersi)
--   diger 5000+        -> MASTERS + DOCTORATE (ortak lisansustu secmeli havuzu)
--   1000 alti / kodsuz -> atanmaz (SEC0001, MES1-2B gibi secmeli slotlari)

BEGIN;

CREATE TABLE IF NOT EXISTS lecture_degree_levels (
    lecture_id UUID NOT NULL REFERENCES lectures(id),
    degree_level VARCHAR(255) NOT NULL
);

WITH numbered AS (
    SELECT id, CAST(substring(code from '[0-9]{4}') AS INT) AS number
    FROM lectures
    WHERE code ~ '[0-9]{4}'
),
derived AS (
    SELECT id,
           CASE
               WHEN number < 1000 THEN ARRAY[]::varchar[]
               WHEN number < 5000 THEN ARRAY['UNDERGRADUATE']::varchar[]
               WHEN number < 5004 THEN ARRAY['MASTERS']::varchar[]
               WHEN number >= 6000 AND number < 6004 THEN ARRAY['DOCTORATE']::varchar[]
               ELSE ARRAY['MASTERS', 'DOCTORATE']::varchar[]
           END AS levels
    FROM numbered
)
INSERT INTO lecture_degree_levels (lecture_id, degree_level)
SELECT derived.id, unnest(derived.levels)
FROM derived
WHERE NOT EXISTS (
    SELECT 1 FROM lecture_degree_levels existing WHERE existing.lecture_id = derived.id
);

ALTER TABLE lectures DROP COLUMN IF EXISTS degree_level;

COMMIT;

-- Kontrol: hangi ders hangi duzeylere dustu, atanamayan kaldi mi
--   SELECT degree_level, count(*) FROM lecture_degree_levels GROUP BY degree_level ORDER BY 1;
--   SELECT l.code, l.name FROM lectures l
--     WHERE NOT EXISTS (SELECT 1 FROM lecture_degree_levels d WHERE d.lecture_id = l.id);
