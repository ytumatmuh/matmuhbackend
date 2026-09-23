package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.entities.cms.BlockType;
import com.matmuh.matmuhsite.entities.cms.ContentBlock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ContentBlockDaoReseedDbTest {

    private static final String SLUG = "/reseed-db-test";
    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final JsonNode SEED = MAPPER.readTree("\"yeni tohum\"");

    @Autowired
    private ContentBlockDao contentBlockDao;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate tx;

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM content_blocks WHERE slug = ?", SLUG);
    }

    // JPQL UPDATE jsonb değeri parametre olarak bağlıyor; birim testler bunu göremez. Şart, okunan
    // sürüm artık satırda değilse hiçbir şey yazmamalı ve yazdığında sürümü ilerletmemeli.
    @Test
    void rewritesOnlyTheVersionItReadAndKeepsIt() {
        var id = contentBlockDao.saveAndFlush(ContentBlock.builder()
                .slug(SLUG).blockPath("hero.title").locale("tr").blockType(BlockType.SHORT_TEXT)
                .value(MAPPER.readTree("\"eski tohum\"")).version(2).updatedBy("editor")
                .build()).getId();

        var stale = tx.execute(status -> contentBlockDao.reseed(id, 1, SEED, "deploy-pipeline"));
        var current = tx.execute(status -> contentBlockDao.reseed(id, 2, SEED, "deploy-pipeline"));

        assertEquals(0, stale);
        assertEquals(1, current);
        var row = jdbc.queryForMap("SELECT value::text AS value, version, updated_by FROM content_blocks WHERE id = ?", id);
        assertEquals("\"yeni tohum\"", row.get("value"));
        assertEquals(2, row.get("version"));
        assertEquals("deploy-pipeline", row.get("updated_by"));
    }
}
