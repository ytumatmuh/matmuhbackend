package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CmsCollectionService;
import com.matmuh.matmuhsite.business.constants.NewsCollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.request.CreateCollectionItemRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.RenameSlugRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.SaveDraftRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.helpers.CmsLocaleResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Arşivlenen kayıt slug'ını tutar; aynı başlıkla yeniden oluşturulan kayıt "-2" alır.
// Purge sonrası slug (alias'larıyla birlikte) gerçekten boşalmalı. Koleksiyon geneli purge
// burada bilerek yok: paylaşılan bir geliştirme veritabanında gerçek arşivi silerdi.
@SpringBootTest
class CmsCollectionPurgeTest {

    private static final String KEY = NewsCollectionSchema.KEY;
    private static final String TITLE = "Cms Purge Test Haberi";
    private static final String SLUG = "cms-purge-test-haberi";
    private static final String USER = "cms-purge-test";
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    private CmsCollectionService collectionService;

    @Autowired
    private CmsLocaleResolver localeResolver;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM collection_slug_aliases WHERE slug LIKE ?", SLUG + "%");
        jdbc.update("DELETE FROM collection_drafts WHERE user_id = ?", USER);
        jdbc.update("DELETE FROM collection_items WHERE slug LIKE ?", SLUG + "%");
    }

    @Test
    void purgeFreesTheSlugAndItsAliases() {
        var first = create();
        assertEquals(SLUG, first.getSlug());

        var renamed = collectionService.renameSlug(KEY, SLUG, new RenameSlugRequestDto(SLUG + "-tasindi", first.getVersion()), false, USER);
        collectionService.saveItemDraft(KEY, renamed.getSlug(), USER, new SaveDraftRequestDto(data("Taslak")), null);
        assertThrows(ConcurrencyConflictException.class, () -> collectionService.purge(KEY, renamed.getSlug(), USER));
        collectionService.archive(KEY, renamed.getSlug(), renamed.getVersion(), USER);

        var second = create();
        assertEquals(SLUG + "-2", second.getSlug(), "the archived item and its alias still hold the slug");

        collectionService.purge(KEY, renamed.getSlug(), USER);

        assertEquals(0, count("collection_items", "slug", renamed.getSlug()));
        assertEquals(0, count("collection_slug_aliases", "slug", SLUG));
        assertEquals(0, count("collection_drafts", "slug", renamed.getSlug()));
        assertThrows(ResourceNotFoundException.class, () -> collectionService.getBySlug(KEY, renamed.getSlug(), USER, null));

        collectionService.archive(KEY, second.getSlug(), second.getVersion(), USER);
        collectionService.purge(KEY, second.getSlug(), USER);
        assertEquals(SLUG, create().getSlug(), "slug is not free after purge");
    }

    private CollectionItemDto create() {
        var request = new CreateCollectionItemRequestDto();
        request.setData(data(TITLE));
        // Dil listesi veritabanından gelir; boş kurulumda yazma dilsiz, dolu kurulumda varsayılan dil.
        return collectionService.createWithAutoSlug(KEY, request, USER, localeResolver.defaultLocale(), null);
    }

    private static tools.jackson.databind.node.ObjectNode data(String title) {
        return JSON.createObjectNode().put("title", title);
    }

    private int count(String table, String column, String value) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE " + column + " = ?", Integer.class, value);
    }
}
