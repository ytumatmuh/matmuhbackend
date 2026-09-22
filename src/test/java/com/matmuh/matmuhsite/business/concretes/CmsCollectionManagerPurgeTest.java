package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CmsCollectionProvider;
import com.matmuh.matmuhsite.business.constants.CollectionRegistry;
import com.matmuh.matmuhsite.business.constants.LectureCollectionSchema;
import com.matmuh.matmuhsite.business.constants.NewsCollectionSchema;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.exceptions.SlugConflictException;
import com.matmuh.matmuhsite.core.helpers.CmsLocaleResolver;
import com.matmuh.matmuhsite.core.helpers.FilePreviewEnricher;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CmsLocaleDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CollectionDraftDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CollectionItemDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CollectionSlugAliasDao;
import com.matmuh.matmuhsite.entities.cms.CmsLocale;
import com.matmuh.matmuhsite.entities.cms.CollectionDraft;
import com.matmuh.matmuhsite.entities.cms.CollectionItem;
import com.matmuh.matmuhsite.entities.cms.CollectionSlugAlias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Arşiv çöp kutusudur: purge yalnız oradaki satırı, alias ve taslaklarıyla, kalıcı siler.
class CmsCollectionManagerPurgeTest {

    private static final String KEY = NewsCollectionSchema.KEY;
    private static final String USER = "editor-1";
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private CollectionItemDao itemDao;
    private CollectionDraftDao draftDao;
    private CollectionSlugAliasDao aliasDao;
    private CmsCollectionManager manager;

    @BeforeEach
    void setUp() {
        itemDao = mock(CollectionItemDao.class);
        draftDao = mock(CollectionDraftDao.class);
        aliasDao = mock(CollectionSlugAliasDao.class);

        var localeDao = mock(CmsLocaleDao.class);
        when(localeDao.findAllByOrderByPositionAsc())
                .thenReturn(List.of(new CmsLocale("tr", 0), new CmsLocale("en", 1)));

        var lectures = mock(CmsCollectionProvider.class);
        when(lectures.collectionKey()).thenReturn(LectureCollectionSchema.KEY);

        manager = new CmsCollectionManager(itemDao, draftDao, aliasDao, new CollectionRegistry(),
                new CmsLocaleResolver(localeDao), mock(FilePreviewEnricher.class), List.of(lectures));
    }

    @Test
    void purgeDeletesTheArchivedRowWithItsAliasesAndDrafts() {
        var item = archived("eski-haber");
        var draft = CollectionDraft.builder().collectionKey(KEY).slug("eski-haber").userId(USER).build();
        when(itemDao.findByCollectionKeyAndSlug(KEY, "eski-haber")).thenReturn(Optional.of(item));
        when(draftDao.findByCollectionKeyAndSlug(KEY, "eski-haber")).thenReturn(List.of(draft));

        manager.purge(KEY, "eski-haber", USER);

        verify(aliasDao).deleteByCollectionKeyAndItemId(KEY, item.getId());
        verify(draftDao).deleteAll(List.of(draft));
        verify(itemDao).delete(item);
    }

    // Canlı kayıt purge edilemez; önce arşivlenmeli. Durum çelişkisi olduğu için 409.
    @Test
    void purgeRefusesALiveRow() {
        var item = archived("canli-haber");
        item.setArchived(false);
        when(itemDao.findByCollectionKeyAndSlug(KEY, "canli-haber")).thenReturn(Optional.of(item));

        var error = assertThrows(ConcurrencyConflictException.class, () -> manager.purge(KEY, "canli-haber", USER));

        assertTrue(error.getMessage().contains("archive it first"));
        verify(itemDao, never()).delete(any());
        verify(aliasDao, never()).deleteByCollectionKeyAndItemId(any(), any());
    }

    @Test
    void purgeOfAMissingRowIs404AndOfAnOldAddressIsMoved() {
        when(itemDao.findByCollectionKeyAndSlug(KEY, "yok")).thenReturn(Optional.empty());
        when(aliasDao.findByCollectionKeyAndSlug(KEY, "yok")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> manager.purge(KEY, "yok", USER));

        var current = archived("yeni-adres");
        var alias = CollectionSlugAlias.builder().collectionKey(KEY).slug("eski-adres").itemId(current.getId()).build();
        when(itemDao.findByCollectionKeyAndSlug(KEY, "eski-adres")).thenReturn(Optional.empty());
        when(aliasDao.findByCollectionKeyAndSlug(KEY, "eski-adres")).thenReturn(Optional.of(alias));
        when(itemDao.findById(current.getId())).thenReturn(Optional.of(current));

        var moved = assertThrows(SlugConflictException.class, () -> manager.purge(KEY, "eski-adres", USER));
        assertEquals(SlugConflictException.REASON_MOVED, moved.getReason());
        assertEquals("yeni-adres", moved.getConflictingSlug());
        verify(itemDao, never()).delete(any());
    }

    // Sağlayıcı tabanlı koleksiyonun arşivi yok: iki uç da 400 ile reddeder.
    @Test
    void providerBackedCollectionsCannotBePurged() {
        assertThrows(CmsValidationException.class, () -> manager.purge(LectureCollectionSchema.KEY, "mtm1501", USER));
        assertThrows(CmsValidationException.class, () -> manager.purgeArchived(LectureCollectionSchema.KEY, USER));
        verify(itemDao, never()).findByCollectionKeyAndArchivedTrue(any());
    }

    @Test
    void purgeArchivedEmptiesTheWholeArchiveAndCounts() {
        var first = archived("bir");
        var second = archived("iki");
        when(itemDao.findByCollectionKeyAndArchivedTrue(KEY)).thenReturn(List.of(first, second));
        when(draftDao.findByCollectionKeyAndSlug(any(), any())).thenReturn(List.of());

        var result = manager.purgeArchived(KEY, USER);

        assertEquals(2, result.purged());
        verify(itemDao).delete(first);
        verify(itemDao).delete(second);
        verify(aliasDao).deleteByCollectionKeyAndItemId(KEY, first.getId());
        verify(aliasDao).deleteByCollectionKeyAndItemId(KEY, second.getId());
    }

    @Test
    void purgeArchivedOnAnEmptyArchiveIsZero() {
        when(itemDao.findByCollectionKeyAndArchivedTrue(KEY)).thenReturn(List.of());

        assertEquals(0, manager.purgeArchived(KEY, USER).purged());
        verify(itemDao, never()).delete(any());
    }

    private static CollectionItem archived(String slug) {
        return CollectionItem.builder()
                .id(UUID.randomUUID())
                .collectionKey(KEY)
                .slug(slug)
                .locale("tr")
                .translationGroupId(UUID.randomUUID())
                .data(JSON.createObjectNode().put("title", "Başlık"))
                .version(2)
                .updatedBy(USER)
                .archived(true)
                .archivedAt(Instant.now())
                .build();
    }
}
