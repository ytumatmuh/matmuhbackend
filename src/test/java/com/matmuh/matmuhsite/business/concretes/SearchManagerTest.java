package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.SearchService;
import com.matmuh.matmuhsite.business.constants.AnnouncementCollectionSchema;
import com.matmuh.matmuhsite.core.dtos.search.response.SearchHitDto;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CollectionItemDao;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.SearchResultType;
import com.matmuh.matmuhsite.entities.cms.CollectionItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class SearchManagerTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String TOKEN = "zqxarama";

    @Autowired
    private SearchService searchService;

    @Autowired
    private CollectionItemDao collectionItemDao;

    @Autowired
    private LectureDao lectureDao;

    // Egehan, 24 Eylül: arama sonuçları en yeni duyuruları değil, slug'a göre alfabetik ilkleri veriyordu.
    @Test
    void announcementsComeNewestFirstWithUndatedOnesLast() {
        announcement("a-" + TOKEN, "2019-03-01");
        announcement("b-" + TOKEN, null);
        announcement("c-" + TOKEN, "2026-09-20");
        announcement("d-" + TOKEN, "2023-05-05");

        var slugs = hits(TOKEN, SearchResultType.ANNOUNCEMENT, "tr").stream().map(SearchHitDto::getSlug).toList();

        assertEquals(List.of("c-" + TOKEN, "d-" + TOKEN, "a-" + TOKEN, "b-" + TOKEN), slugs);
    }

    @Test
    void lectureTitleFollowsTheRequestedLanguage() {
        var lecture = new Lecture();
        lecture.setCode("ZZA9001");
        lecture.setSlug("zza9001");
        lecture.setName("Arama Analizi " + TOKEN);
        lecture.setNameEn("Search Analysis " + TOKEN);
        lectureDao.saveAndFlush(lecture);

        assertEquals("Search Analysis " + TOKEN, hits(TOKEN, SearchResultType.LECTURE, "en").get(0).getTitle());
        assertEquals("Arama Analizi " + TOKEN, hits(TOKEN, SearchResultType.LECTURE, "tr").get(0).getTitle());
    }

    private List<SearchHitDto> hits(String query, SearchResultType type, String locale) {
        return searchService.search(query, Set.of(type), locale, 10).getGroups().get(0).getItems();
    }

    private void announcement(String slug, String publishedAt) {
        var data = JSON.createObjectNode().put("title", "Duyuru " + slug);
        if (publishedAt != null) {
            data.put("publishedAt", publishedAt);
        }
        collectionItemDao.saveAndFlush(CollectionItem.builder()
                .collectionKey(AnnouncementCollectionSchema.KEY)
                .slug(slug)
                .locale("tr")
                .translationGroupId(UUID.randomUUID())
                .data(data)
                .updatedBy("test")
                .build());
    }
}
