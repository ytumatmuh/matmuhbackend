package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.core.dtos.cms.request.UpdatePageRequestDto;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContentManagerPublishConflictTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final JsonNode OLD = MAPPER.readTree("\"old\"");
    private static final JsonNode NEW = MAPPER.readTree("\"new\"");

    private final ContentBlockDao blockDao = mock(ContentBlockDao.class);
    private final ContentDraftDao draftDao = mock(ContentDraftDao.class);
    private final CmsLocaleResolver localeResolver = mock(CmsLocaleResolver.class);
    private ContentManager manager;

    @BeforeEach
    void setUp() {
        when(localeResolver.requireForWrite("tr")).thenReturn("tr");
        manager = new ContentManager(blockDao, draftDao, MAPPER, localeResolver);
    }

    private static ContentBlock block(String path, int version) {
        return ContentBlock.builder().id(UUID.randomUUID()).slug("/home").blockPath(path).locale("tr")
                .blockType(BlockType.SHORT_TEXT).value(OLD).version(version).updatedBy("editor").build();
    }

    private static UpdatePageRequestDto request(UpdatePageRequestDto.BlockUpdateDto... blocks) {
        return new UpdatePageRequestDto("/home", List.of(blocks));
    }

    private static UpdatePageRequestDto.BlockUpdateDto update(String path, JsonNode value, int version) {
        return new UpdatePageRequestDto.BlockUpdateDto(path, value, version);
    }

    // SDK 409 gövdesindeki `conflicts` listesine bakıp yalnız o kartları çakışma durumuna alır;
    // ilk uyuşmazlıkta kesmek ikinci bayat bloğu gizliyordu.
    @Test
    void reportsEveryStaleBlockInOneConflict() {
        when(blockDao.findForUpdate(eq("/home"), anyList(), eq("tr")))
                .thenReturn(List.of(block("hero.title", 4), block("hero.note", 2), block("hero.cta", 1)));

        var exception = assertThrows(ConcurrencyConflictException.class, () -> manager.updatePage("u1", request(
                update("hero.title", NEW, 3),
                update("hero.note", NEW, 1),
                update("hero.cta", NEW, 1)), "tr"));

        assertEquals(List.of(
                new ConcurrencyConflictException.BlockConflict("hero.title", 4, 3),
                new ConcurrencyConflictException.BlockConflict("hero.note", 2, 1)), exception.getConflicts());
        verify(blockDao, never()).saveAll(any());
        verify(draftDao, never()).deleteOwn(any(), any(), any());
    }

    @Test
    void publishesAndCountsWhenVersionsMatch() {
        when(blockDao.findForUpdate(eq("/home"), anyList(), eq("tr")))
                .thenReturn(List.of(block("hero.title", 4), block("hero.note", 2)));

        var result = manager.updatePage("u1", request(
                update("hero.title", NEW, 4),
                update("hero.note", OLD, 2)), "tr");

        assertEquals(1, result.getUpdated());
        assertEquals(1, result.getUnchanged());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ContentBlock>> captor = ArgumentCaptor.forClass(List.class);
        verify(blockDao).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(5, captor.getValue().get(0).getVersion());
        assertEquals("u1", captor.getValue().get(0).getUpdatedBy());
        verify(draftDao).deleteOwn("/home", "u1", "tr");
    }
}
