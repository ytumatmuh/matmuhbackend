package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.core.dtos.cms.request.SyncManifestRequestDto;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.core.helpers.CmsLocaleResolver;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.ContentBlockDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.ContentDraftDao;
import com.matmuh.matmuhsite.entities.cms.BlockType;
import com.matmuh.matmuhsite.entities.cms.ContentBlock;
import com.matmuh.matmuhsite.entities.cms.ContentDraft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContentManagerSyncReseedTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final JsonNode OLD = MAPPER.readTree("\"eski tohum\"");
    private static final JsonNode NEW = MAPPER.readTree("\"yeni tohum\"");

    private final ContentBlockDao blockDao = mock(ContentBlockDao.class);
    private final ContentDraftDao draftDao = mock(ContentDraftDao.class);
    private final CmsLocaleResolver localeResolver = mock(CmsLocaleResolver.class);
    private ContentManager manager;

    private final ContentBlock fresh = existing("hero.fresh", 1, OLD, false);
    private final ContentBlock published = existing("hero.published", 2, OLD, false);
    private final ContentBlock drafted = existing("hero.drafted", 1, OLD, false);
    private final ContentBlock current = existing("hero.current", 1, NEW, false);

    @BeforeEach
    void setUp() {
        when(localeResolver.replaceDeclared(any())).thenReturn(List.of("tr"));
        when(draftDao.findAll()).thenReturn(List.of(ContentDraft.builder()
                .slug("/home").userId("editor").locale("tr")
                .payload(MAPPER.readTree("[{\"blockPath\": \"hero.drafted\", \"value\": \"yarım\", \"version\": 1}]"))
                .build()));
        manager = new ContentManager(blockDao, draftDao, MAPPER, localeResolver);
    }

    private static ContentBlock existing(String path, int version, JsonNode value, boolean archived) {
        return ContentBlock.builder().id(UUID.randomUUID()).slug("/home").blockPath(path).locale("tr")
                .blockType(BlockType.SHORT_TEXT).value(value).version(version).archived(archived)
                .updatedBy("editor").build();
    }

    private static SyncManifestRequestDto manifest(ContentBlock... rows) {
        return new SyncManifestRequestDto("home", Arrays.stream(rows)
                .map(row -> new SyncManifestRequestDto.ManifestBlockDto(row.getBlockPath(), BlockType.SHORT_TEXT,
                        NEW, null, 0, null))
                .toList());
    }

    @SuppressWarnings("unchecked")
    private List<ContentBlock> saved() {
        ArgumentCaptor<List<ContentBlock>> captor = ArgumentCaptor.forClass(List.class);
        verify(blockDao).saveAll(captor.capture());
        return captor.getValue();
    }

    // Değişen tohum yalnız dokunulmamış satıra iner: yayınlanmış (sürüm > 1) ve o dilde taslağı
    // olan satır korunur, yeniden tohumlanan satır sürümünü tutar ki sonraki reseed onu yine bulsun.
    @Test
    void flagRewritesRowsNobodyPublishedOrDrafted() {
        when(blockDao.findAll()).thenReturn(List.of(fresh, published, drafted, current));
        when(blockDao.reseed(any(), anyInt(), any(), any())).thenReturn(1);

        var result = manager.sync(List.of(manifest(fresh, published, drafted, current)), null, true);

        verify(blockDao).reseed(fresh.getId(), 1, NEW, CmsMessages.SYNCED_BY_DEPLOY_PIPELINE);
        verify(blockDao, times(1)).reseed(any(), anyInt(), any(), any());
        assertEquals(List.of(fresh), saved());
        assertEquals(NEW, fresh.getValue());
        assertEquals(1, fresh.getVersion());
        assertEquals(OLD, published.getValue());
        assertEquals(OLD, drafted.getValue());

        var counts = result.getResults().get(0);
        assertEquals(1, counts.getReseeded());
        assertEquals(3, counts.getUnchanged(), "yeniden tohumlanan satır unchanged'a da sayılmaz");
    }

    @Test
    void withoutTheFlagUnpublishedRowsKeepTheirValue() {
        when(blockDao.findAll()).thenReturn(List.of(fresh, published, drafted, current));

        var result = manager.sync(List.of(manifest(fresh, published, drafted, current)), null, false);

        verify(blockDao, never()).reseed(any(), anyInt(), any(), any());
        verify(draftDao, never()).findAll();
        assertEquals(OLD, fresh.getValue());
        assertEquals(0, result.getResults().get(0).getReseeded());
        assertEquals(4, result.getResults().get(0).getUnchanged());
    }

    // Kimsenin yayınlamadığı arşivli satır aynı sync'te hem geri gelir hem tohumunu alır.
    @Test
    void restoredRowNobodyPublishedIsReseededToo() {
        var archived = existing("hero.back", 1, OLD, true);
        when(blockDao.findAll()).thenReturn(List.of(archived));
        when(blockDao.reseed(any(), anyInt(), any(), any())).thenReturn(1);

        var result = manager.sync(List.of(manifest(archived)), null, true);

        assertFalse(archived.isArchived());
        assertEquals(NEW, archived.getValue());
        assertEquals(1, result.getResults().get(0).getRestored());
        assertEquals(1, result.getResults().get(0).getReseeded());
    }

    // Okumayla yazma arasına giren publish sürümü ilerletir; şartlı UPDATE 0 satır döner ve
    // sync o publish'i ezmek yerine hiçbir şey yazmadan 409 ile biter.
    @Test
    void aPublishRacingTheReseedFailsTheSyncAndWritesNothing() {
        when(blockDao.findAll()).thenReturn(List.of(fresh));
        when(blockDao.reseed(any(), anyInt(), any(), any())).thenReturn(0);

        var exception = assertThrows(ConcurrencyConflictException.class,
                () -> manager.sync(List.of(manifest(fresh)), null, true));

        assertEquals(CmsMessages.RESEED_CONFLICT + "/home/hero.fresh", exception.getMessage());
        verify(blockDao, never()).saveAll(any());
    }
}
