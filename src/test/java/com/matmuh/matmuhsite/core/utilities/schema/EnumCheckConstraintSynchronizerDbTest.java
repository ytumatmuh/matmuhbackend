package com.matmuh.matmuhsite.core.utilities.schema;

import com.matmuh.matmuhsite.entities.cms.BlockType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EnumCheckConstraintSynchronizerDbTest {

    private static final String CHECKS_ON_BLOCK_TYPE = """
            SELECT con.conname
            FROM pg_constraint con
            JOIN pg_class rel ON rel.oid = con.conrelid
            JOIN pg_namespace ns ON ns.oid = rel.relnamespace
            WHERE con.contype = 'c'
              AND ns.nspname = current_schema()
              AND rel.relname = 'content_blocks'
              AND pg_get_constraintdef(con.oid) LIKE '%(block\\_type)::text%' ESCAPE '\\'
            """;

    private static final String DEFINITION_OF = """
            SELECT pg_get_constraintdef(con.oid)
            FROM pg_constraint con
            JOIN pg_class rel ON rel.oid = con.conrelid
            WHERE rel.relname = 'content_blocks' AND con.conname = ?
            """;

    @Autowired
    private EnumCheckConstraintSynchronizer synchronizer;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void rewritesStaleCheckAndThenReportsNothingToDo() {
        for (var name : jdbc.queryForList(CHECKS_ON_BLOCK_TYPE, String.class)) {
            jdbc.execute("ALTER TABLE content_blocks DROP CONSTRAINT \"" + name + "\"");
        }
        jdbc.execute("ALTER TABLE content_blocks ADD CONSTRAINT stale_block_type_check "
                + "CHECK (block_type = ANY (ARRAY['SHORT_TEXT', 'LONG_TEXT']::text[]))");

        var changes = synchronizer.synchronize();

        assertEquals(1, changes.size());
        assertEquals("content_blocks", changes.get(0).table());
        assertEquals("block_type", changes.get(0).column());
        assertEquals(List.of("stale_block_type_check"), changes.get(0).dropped());
        assertEquals("content_blocks_block_type_check", changes.get(0).added());

        var names = jdbc.queryForList(CHECKS_ON_BLOCK_TYPE, String.class);
        assertEquals(List.of("content_blocks_block_type_check"), names);
        var definition = jdbc.queryForObject(DEFINITION_OF, String.class, "content_blocks_block_type_check");
        for (var type : BlockType.values()) {
            assertTrue(definition.contains("'" + type.name() + "'"), type.name() + " missing from " + definition);
        }

        assertTrue(synchronizer.synchronize().isEmpty());
    }
}
