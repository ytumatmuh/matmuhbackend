package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.core.dtos.cms.request.UpdatePageRequestDto;
import com.matmuh.matmuhsite.core.helpers.CmsLocaleResolver;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.ContentBlockDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.ContentDraftDao;
import com.matmuh.matmuhsite.entities.cms.ContentDraft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContentManagerDraftMergeTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final ContentDraftDao draftDao = mock(ContentDraftDao.class);
    private final CmsLocaleResolver localeResolver = mock(CmsLocaleResolver.class);
    private ContentManager manager;

    @BeforeEach
    void setUp() {
        when(localeResolver.requireForWrite("tr")).thenReturn("tr");
        manager = new ContentManager(mock(ContentBlockDao.class), draftDao, MAPPER, localeResolver);
    }

    private static UpdatePageRequestDto request(String path, String value) {
        return new UpdatePageRequestDto("/home", List.of(
                new UpdatePageRequestDto.BlockUpdateDto(path, MAPPER.readTree("\"" + value + "\""), 1)));
    }

    private static ContentDraft draftWith(String path, String value) {
        return ContentDraft.builder().slug("/home").userId("u1").locale("tr")
                .payload(MAPPER.readTree("[{\"blockPath\":\"" + path + "\",\"value\":\"" + value + "\",\"version\":1}]"))
                .build();
    }

    private Map<String, String> savedValues() {
        var captor = ArgumentCaptor.forClass(ContentDraft.class);
        verify(draftDao).save(captor.capture());
        var values = new LinkedHashMap<String, String>();
        for (JsonNode node : captor.getValue().getPayload()) {
            values.put(node.get("blockPath").asString(), node.get("value").asString());
        }
        return values;
    }

    // SDK autosave'i yalnız son kayıttan beri değişen blokları yollar (collectForSlug);
    // gövdeyi olduğu gibi yazmak önceki bloğun taslağını düşürüyordu.
    @Test
    void keepsBlocksAlreadyInTheDraft() {
        when(draftDao.findOwn("/home", "u1", "tr")).thenReturn(Optional.of(draftWith("hero.title", "A")));

        manager.saveDraft("u1", request("hero.note", "B"), "tr");

        assertEquals(Map.of("hero.title", "A", "hero.note", "B"), savedValues());
    }

    @Test
    void latestValueWinsForTheSamePath() {
        when(draftDao.findOwn("/home", "u1", "tr")).thenReturn(Optional.of(draftWith("hero.title", "A")));

        manager.saveDraft("u1", request("Hero.Title", "A2"), "tr");

        assertEquals(Map.of("hero.title", "A2"), savedValues());
    }

    @Test
    void createsTheDraftWhenNoneExists() {
        when(draftDao.findOwn("/home", "u1", "tr")).thenReturn(Optional.empty());

        manager.saveDraft("u1", request("hero.title", "A"), "tr");

        assertEquals(Map.of("hero.title", "A"), savedValues());
        var captor = ArgumentCaptor.forClass(ContentDraft.class);
        verify(draftDao).save(captor.capture());
        assertEquals("tr", captor.getValue().getLocale());
        assertEquals("u1", captor.getValue().getUserId());
    }
}
