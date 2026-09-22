-- content_blocks.block_type CHECK kisitini guncel BlockType enum'una gore yeniden yazar.
-- Kisit adina guvenmez, tekrar calistirilabilir.
DO $$
DECLARE
    old_name TEXT;
    allowed  TEXT[] := ARRAY[
        'SHORT_TEXT','LONG_TEXT','RICH_TEXT','NUMBER','BOOL','URL','DATE',
        'IMAGE','FILE','LINK','SELECT','STRING_ARRAY','OBJECT_ARRAY','COLLECTION','LIST'];
BEGIN
    FOR old_name IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        WHERE con.contype = 'c'
          AND rel.relname = 'content_blocks'
          AND pg_get_constraintdef(con.oid) ILIKE '%block_type%'
    LOOP
        EXECUTE format('ALTER TABLE content_blocks DROP CONSTRAINT %I', old_name);
    END LOOP;

    EXECUTE format(
        'ALTER TABLE content_blocks ADD CONSTRAINT content_blocks_block_type_check CHECK (block_type::text = ANY (%L))',
        allowed);

    RAISE NOTICE 'content_blocks.block_type: % deger', array_length(allowed, 1);
END $$;

SELECT pg_get_constraintdef(con.oid) AS yeni_kisit
FROM pg_constraint con JOIN pg_class rel ON rel.oid = con.conrelid
WHERE con.contype='c' AND rel.relname='content_blocks';
