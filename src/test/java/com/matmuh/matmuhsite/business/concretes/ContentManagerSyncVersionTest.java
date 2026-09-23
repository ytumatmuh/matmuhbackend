package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.core.dtos.cms.request.SyncManifestRequestDto;
import com.matmuh.matmuhsite.core.helpers.CmsLocaleResolver;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.ContentBlockDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.ContentDraftDao;
import com.matmuh.matmuhsite.entities.cms.BlockType;
import com.matmuh.matmuhsite.entities.cms.ContentBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContentManagerSyncVersionTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final JsonNode VALUE = MAPPER.readTree("\"Merhaba\"");

    private final ContentBlockDao blockDao = mock(ContentBlockDao.class);
    private final CmsLocaleResolver localeResolver = mock(CmsLocaleResolver.class);
    private ContentManager manager;

    @BeforeEach
    void setUp() {
        when(localeResolver.replaceDeclared(any())).thenReturn(List.of());
        manager = new ContentManager(blockDao, mock(ContentDraftDao.class), MAPPER, localeResolver);
    }

    private static SyncManifestRequestDto.ManifestBlockDto block(String path, BlockType type, int sortOrder) {
        return new SyncManifestRequestDto.ManifestBlockDto(path, type, VALUE, null, sortOrder, null);
    }

    private static ContentBlock existing(String path, int version, boolean archived) {
        return ContentBlock.builder().id(UUID.randomUUID()).slug("/home").blockPath(path)
                .blockType(BlockType.SHORT_TEXT).value(VALUE).sortOrder(0).version(version)
                .archived(archived).updatedBy("editor").build();
    }

    @SuppressWarnings("unchecked")
    private List<ContentBlock> saved() {
        ArgumentCaptor<List<ContentBlock>> captor = ArgumentCaptor.forClass(List.class);
        verify(blockDao).saveAll(captor.capture());
        return captor.getValue();
    }

    private static ContentBlock find(List<ContentBlock> blocks, String path) {
        return blocks.stream().filter(b -> b.getBlockPath().equals(path)).findFirst().orElseThrow();
    }

    // Sayfası açık editör deploy'dan sonra publish ettiğinde 409 almamalı: sürümü yalnız publish ilerletir.
    @Test
    void retypingReorderingRestoringAndArchivingKeepTheVersion() {
        when(blockDao.findAll()).thenReturn(List.of(
                existing("hero.title", 3, false),
                existing("hero.body", 4, true),
                existing("hero.gone", 5, false)));

        manager.sync(List.of(new SyncManifestRequestDto("home", List.of(
                block("hero.title", BlockType.LONG_TEXT, 2),
                block("hero.body", BlockType.SHORT_TEXT, 0)))), null);

        var rows = saved();
        assertEquals(3, find(rows, "hero.title").getVersion());
        assertEquals(BlockType.LONG_TEXT, find(rows, "hero.title").getBlockType());
        assertEquals(4, find(rows, "hero.body").getVersion());
        assertFalse(find(rows, "hero.body").isArchived());
        assertEquals(5, find(rows, "hero.gone").getVersion());
        assertTrue(find(rows, "hero.gone").isArchived());
    }
}
