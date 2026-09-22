-- Ofis belgelerinin PDF karşılığının depolama anahtarı.
-- Kolon nullable ve varsayılansız: Hibernate (ddl-auto=update) kendisi de ekleyebilir,
-- bu blok çalışmadan deploy edilirse hiçbir şey bozulmaz — önizleme yalnızca boş kalır.

ALTER TABLE lecture_notes ADD COLUMN IF NOT EXISTS preview_url VARCHAR(255);

-- Doğrulama
SELECT count(*) AS onizlemeli_not FROM lecture_notes WHERE preview_url IS NOT NULL;
