package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.core.dtos.cms.request.SyncManifestRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.UpdatePageRequestDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.helpers.CmsLocaleResolver;
import com.matmuh.matmuhsite.core.helpers.FileUrlRule;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.ContentBlockDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.ContentDraftDao;
import com.matmuh.matmuhsite.entities.cms.BlockType;
import com.matmuh.matmuhsite.entities.cms.ContentBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
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

class ContentManagerFileUrlTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final JsonNode SAFE = MAPPER.readTree("{\"url\": \"/docs/a.pdf\", \"name\": \"a.pdf\"}");
    private static final JsonNode SCRIPT = MAPPER.readTree("{\"url\": \"javascript:alert(1)\", \"name\": \"x\"}");
    private static final JsonNode TEXT = MAPPER.readTree("\"yeni\"");

    private final ContentBlockDao blockDao = mock(ContentBlockDao.class);
    private final CmsLocaleResolver localeResolver = mock(CmsLocaleResolver.class);
    private ContentManager manager;

    @BeforeEach
    void setUp() {
        when(localeResolver.requireForWrite("tr")).thenReturn("tr");
        manager = new ContentManager(blockDao, mock(ContentDraftDao.class), MAPPER, localeResolver);
    }

    private static ContentBlock block(String path, BlockType type) {
        return ContentBlock.builder().id(UUID.randomUUID()).slug("/home").blockPath(path).locale("tr")
                .blockType(type).value(MAPPER.nullNode()).version(1).updatedBy("editor").build();
    }

    // Publish ya hep ya hiç: aynı istekteki geçerli blok da yazılmaz.
    @Test
    void publishRejectsScriptAddressesAndWritesNothing() {
        when(blockDao.findForUpdate(eq("/home"), anyList(), eq("tr")))
                .thenReturn(List.of(block("hero.doc", BlockType.FILE), block("hero.title", BlockType.SHORT_TEXT)));

        var exception = assertThrows(CmsValidationException.class, () -> manager.updatePage("u1",
                new UpdatePageRequestDto("/home", List.of(
                        new UpdatePageRequestDto.BlockUpdateDto("hero.doc", SCRIPT, 1),
                        new UpdatePageRequestDto.BlockUpdateDto("hero.title", TEXT, 1))), "tr"));

        assertEquals(List.of("Block 'hero.doc': 'url' " + FileUrlRule.EXPECTATION + "."), exception.getErrors());
        verify(blockDao, never()).saveAll(any());
    }

    @Test
    void syncRejectsScriptAddressesInEverySeedBeforeTouchingLocales() {
        var exception = assertThrows(CmsValidationException.class, () -> manager.sync(List.of(
                new SyncManifestRequestDto("/home", List.of(
                        new SyncManifestRequestDto.ManifestBlockDto("hero.doc", BlockType.FILE, SAFE,
                                Map.of("en", SCRIPT), 0, null),
                        new SyncManifestRequestDto.ManifestBlockDto("hero.cv", BlockType.FILE, SCRIPT,
                                null, 1, null)))), List.of("tr", "en"), false));

        assertEquals(List.of(
                "Block '/home/hero.doc': 'defaultValues.en.url' " + FileUrlRule.EXPECTATION + ".",
                "Block '/home/hero.cv': 'defaultValue.url' " + FileUrlRule.EXPECTATION + "."), exception.getErrors());
        verify(localeResolver, never()).replaceDeclared(any());
    }
}
