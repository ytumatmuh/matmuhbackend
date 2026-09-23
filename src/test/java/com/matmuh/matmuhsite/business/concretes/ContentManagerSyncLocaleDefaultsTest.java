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
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContentManagerSyncLocaleDefaultsTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final JsonNode TR = MAPPER.readTree("\"Hoş geldiniz\"");
    private static final JsonNode EN = MAPPER.readTree("\"Welcome\"");
    private static final JsonNode EDITED = MAPPER.readTree("\"Welcome, edited by hand\"");

    private final ContentBlockDao blockDao = mock(ContentBlockDao.class);
    private final CmsLocaleResolver localeResolver = mock(CmsLocaleResolver.class);
    private ContentManager manager;

    @BeforeEach
    void setUp() {
        when(localeResolver.replaceDeclared(any())).thenReturn(List.of("tr", "en"));
        when(blockDao.reseed(any(), anyInt(), any(), any())).thenReturn(1);
        manager = new ContentManager(blockDao, mock(ContentDraftDao.class), MAPPER, localeResolver);
    }

    private static SyncManifestRequestDto manifest(SyncManifestRequestDto.ManifestBlockDto... blocks) {
        return new SyncManifestRequestDto("home", List.of(blocks));
    }

    private static SyncManifestRequestDto.ManifestBlockDto block(String path, JsonNode defaultValue, Map<String, JsonNode> defaults) {
        return new SyncManifestRequestDto.ManifestBlockDto(path, BlockType.SHORT_TEXT, defaultValue, defaults, 0, null);
    }

    private static ContentBlock existing(String path, String locale, JsonNode value) {
        return ContentBlock.builder().id(UUID.randomUUID()).slug("/home").blockPath(path).locale(locale)
                .blockType(BlockType.SHORT_TEXT).value(value).updatedBy("editor").build();
    }

    @SuppressWarnings("unchecked")
    private List<ContentBlock> saved() {
        ArgumentCaptor<List<ContentBlock>> captor = ArgumentCaptor.forClass(List.class);
        verify(blockDao).saveAll(captor.capture());
        return captor.getValue();
    }

    private static JsonNode valueOf(List<ContentBlock> blocks, String path, String locale) {
        return blocks.stream()
                .filter(b -> b.getBlockPath().equals(path) && locale.equals(b.getLocale()))
                .findFirst().orElseThrow().getValue();
    }

    // Tek defaultValue her dile aynı metni yazıyordu; dil başına tohum varsa her dil kendininkini alır.
    @Test
    void seedsEachLocaleWithItsOwnDefault() {
        when(blockDao.findAll()).thenReturn(List.of());

        var result = manager.sync(List.of(manifest(
                block("hero.title", TR, Map.of("tr", TR, "en", EN)),
                block("hero.note", TR, null))), List.of("tr", "en"), false);

        var rows = saved();
        assertEquals(EN, valueOf(rows, "hero.title", "en"));
        assertEquals(TR, valueOf(rows, "hero.title", "tr"));
        assertEquals(TR, valueOf(rows, "hero.note", "en"));
        assertEquals(4, result.getResults().get(0).getCreated());
    }

    // Prod'daki durum: en satırı Türkçe tohumla oturuyor. Dokunulmamışsa yeni İngilizce
    // tohumu alır; editörün elle yazdığı satır asla ezilmez.
    @Test
    void reseedsOnlyRowsStillCarryingTheGenericSeed() {
        when(blockDao.findAll()).thenReturn(List.of(
                existing("hero.title", "tr", TR),
                existing("hero.title", "en", TR),
                existing("hero.note", "en", EDITED)));

        var result = manager.sync(List.of(manifest(
                block("hero.title", TR, Map.of("en", EN)),
                block("hero.note", TR, Map.of("en", EN)))), List.of("tr", "en"), false);

        var rows = saved();
        assertEquals(EN, valueOf(rows, "hero.title", "en"));
        assertEquals(1, result.getResults().get(0).getReseeded());
        assertEquals(1, rows.stream().filter(b -> b.getBlockPath().equals("hero.title") && "en".equals(b.getLocale())).count());
        assertEquals(1, rows.stream().filter(b -> b.getBlockPath().equals("hero.note")).count(), "yalnız eksik tr satırı oluşur, düzenlenmiş en satırı dokunulmaz");
    }

    @Test
    void secondSyncIsANoOp() {
        when(blockDao.findAll()).thenReturn(List.of(
                existing("hero.title", "tr", TR),
                existing("hero.title", "en", EN)));

        var result = manager.sync(List.of(manifest(
                block("hero.title", TR, Map.of("tr", TR, "en", EN)))), List.of("tr", "en"), false);

        assertEquals(0, result.getResults().get(0).getReseeded());
        assertEquals(2, result.getResults().get(0).getUnchanged());
        assertEquals(0, saved().size());
    }
}
