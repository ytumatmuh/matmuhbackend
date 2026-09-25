package com.matmuh.matmuhsite.dataAccess.abstracts.cms;

import com.matmuh.matmuhsite.business.constants.AnnouncementCollectionSchema;
import com.matmuh.matmuhsite.core.helpers.CollectionSortParser;
import com.matmuh.matmuhsite.entities.cms.CollectionItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class CollectionItemDaoBoolSortDbTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String MARKER = "{\"boolSortTest\": true}";

    @Autowired
    private CollectionItemDao collectionItemDao;

    // Egehan, 25 Eylül: panelden kaydedilip featured=false yazılan tek duyuru, alanı hiç olmayan 487 kaydın önüne geçiyordu.
    @Test
    void aMissingBoolSortsLikeFalse() {
        save("never-saved-from-panel", null, "2026-09-20");
        save("saved-from-panel", false, "2019-01-01");
        save("pinned", true, "2020-01-01");

        var sorts = CollectionSortParser.parse(AnnouncementCollectionSchema.SCHEMA, "featured:desc,publishedAt:desc");
        var slugs = collectionItemDao.searchByFilter(AnnouncementCollectionSchema.KEY, MARKER, sorts, false, "tr",
                        null, null, 0, 3)
                .stream().map(CollectionItem::getSlug).toList();

        assertEquals(List.of("pinned", "never-saved-from-panel", "saved-from-panel"), slugs);
    }

    private void save(String slug, Boolean featured, String publishedAt) {
        var data = JSON.createObjectNode().put("title", slug).put("publishedAt", publishedAt).put("boolSortTest", true);
        if (featured != null) {
            data.put("featured", featured);
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
