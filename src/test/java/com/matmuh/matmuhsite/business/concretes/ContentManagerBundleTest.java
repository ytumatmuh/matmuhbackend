package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.core.dtos.cms.response.ContentBundleDto;
import com.matmuh.matmuhsite.core.helpers.CmsLocaleResolver;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.ContentBlockDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.ContentDraftDao;
import com.matmuh.matmuhsite.entities.cms.BlockType;
import com.matmuh.matmuhsite.entities.cms.ContentBlock;
import com.matmuh.matmuhsite.entities.cms.ContentDraft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContentManagerBundleTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final JsonNode PUBLISHED = MAPPER.readTree("\"yayında\"");
    private static final JsonNode DRAFTED = MAPPER.readTree("\"taslak\"");

    private final ContentBlockDao blockDao = mock(ContentBlockDao.class);
    private final ContentDraftDao draftDao = mock(ContentDraftDao.class);
    private final CmsLocaleResolver localeResolver = mock(CmsLocaleResolver.class);
    private ContentManager manager;

    @BeforeEach
    void setUp() {
        when(localeResolver.resolveForRead(null)).thenReturn("tr");
        when(blockDao.findPublishedByLocale("tr")).thenReturn(List.of(
                block("/", "hero.title"),
                block("/__global", "footer.text"),
                block("/bolum/__iletisim", "phone"),
                block("/bolum/hakkinda", "intro")));
        manager = new ContentManager(blockDao, draftDao, MAPPER, localeResolver);
    }

    private static ContentBlock block(String slug, String path) {
        return ContentBlock.builder().id(UUID.randomUUID()).slug(slug).blockPath(path).locale("tr")
                .blockType(BlockType.SHORT_TEXT).value(PUBLISHED).version(1).updatedBy("editor").build();
    }

    private static List<String> slugs(List<ContentBundleDto.ContentPageDto> pages) {
        return pages.stream().map(ContentBundleDto.ContentPageDto::slug).toList();
    }

    // Son segmenti __ ile başlayan slug rota değil: her sayfanın paylaştığı içerik global altında gelir.
    @Test
    void splitsSharedSlugsFromRoutes() {
        var bundle = manager.getAllPublished(null);

        assertEquals("tr", bundle.locale());
        assertEquals(List.of("/__global", "/bolum/__iletisim"), slugs(bundle.global()));
        assertEquals(List.of("/", "/bolum/hakkinda"), slugs(bundle.pages()));
        assertNull(bundle.pages().get(0).blocks().get(0).getDraftValue());
    }

    @Test
    void overlaysTheEditorsOwnDraftsOnly() {
        when(draftDao.findAllOwn("u1", "tr")).thenReturn(List.of(ContentDraft.builder()
                .slug("/bolum/hakkinda").userId("u1").locale("tr")
                .payload(MAPPER.readTree("[{\"blockPath\": \"intro\", \"value\": \"taslak\"}]"))
                .build()));

        var bundle = manager.getAllForEditor("u1", null);

        assertNull(bundle.pages().get(0).blocks().get(0).getDraftValue());
        assertEquals(DRAFTED, bundle.pages().get(1).blocks().get(0).getDraftValue());
    }
}
