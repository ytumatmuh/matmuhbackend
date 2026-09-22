package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CmsCollectionProvider;
import com.matmuh.matmuhsite.business.constants.AcademicTermCollectionSchema;
import com.matmuh.matmuhsite.business.constants.CollectionRegistry;
import com.matmuh.matmuhsite.business.constants.NewsCollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.request.RenameSlugRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.SaveDraftRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.SaveNewDraftRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.UpsertCollectionItemRequestDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
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
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// inscribed 4.4.1 sözleşmesi (shared/contracts/schemas.js, transport.js, errors.js) ile
// koleksiyon uçlarının uyumu; her test SDK'nın dayandığı bir davranışı sabitler.
class CmsCollectionManagerContractTest {

    private static final String KEY = NewsCollectionSchema.KEY;
    private static final String PROVIDER_KEY = AcademicTermCollectionSchema.KEY;
    private static final String TERM_SLUG = "2026-2027-fall";
    private static final String USER = "editor-1";
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private CollectionItemDao itemDao;
    private CollectionDraftDao draftDao;
    private CollectionSlugAliasDao aliasDao;
    private CmsCollectionProvider termProvider;
    private CmsCollectionManager manager;

    @BeforeEach
    void setUp() {
        itemDao = mock(CollectionItemDao.class);
        draftDao = mock(CollectionDraftDao.class);
        aliasDao = mock(CollectionSlugAliasDao.class);

        var localeDao = mock(CmsLocaleDao.class);
        when(localeDao.findAllByOrderByPositionAsc())
                .thenReturn(List.of(new CmsLocale("tr", 0), new CmsLocale("en", 1)));

        var enricher = mock(FilePreviewEnricher.class);
        when(enricher.enrich(any(), any())).thenAnswer(invocation -> invocation.getArgument(1));

        termProvider = mock(CmsCollectionProvider.class);
        when(termProvider.collectionKey()).thenReturn(PROVIDER_KEY);

        manager = new CmsCollectionManager(itemDao, draftDao, aliasDao, new CollectionRegistry(),
                new CmsLocaleResolver(localeDao), enricher, List.of(termProvider));
    }

    // SDK sil düğmesini her koleksiyonda çiziyor (affordance koleksiyon tipine bakmıyor);
    // sağlayıcılı koleksiyonda aynı uç satırı kendi tablosunda siler ve yanıt şekli değişmez.
    @Test
    void providerBackedArchiveDeletesThroughTheProvider() {
        var result = manager.archive(PROVIDER_KEY, TERM_SLUG, 3, USER);

        verify(termProvider).delete(TERM_SLUG, 3);
        assertEquals(PROVIDER_KEY, result.collectionKey());
        assertEquals(TERM_SLUG, result.slug());
        assertEquals(3, result.version());
    }

    // Sürümsüz DELETE jsonb'de olduğu gibi burada da 400: çağıranın okuduğundan başka bir
    // kaydı düşürebilir. Gerçek karşılaştırmayı sürümü olan sağlayıcılar kendi yapar.
    @Test
    void providerBackedArchiveWithoutVersionIsRefused() {
        var error = assertThrows(CmsValidationException.class,
                () -> manager.archive(PROVIDER_KEY, TERM_SLUG, null, USER));

        assertTrue(error.getMessage().contains("Version is required"));
        verify(termProvider, never()).delete(any(), any());
    }

    // Silme sağlayıcıya iniyor ama geri yükleme ve purge inemez: satır kendi tablosunda
    // soft-delete edilir, CMS arşivinde listelenmez. Hata nereye gidileceğini söyler.
    @Test
    void providerBackedRestoreAndPurgeStayRefused() {
        var restore = assertThrows(CmsValidationException.class, () -> manager.restore(PROVIDER_KEY, TERM_SLUG, USER));
        assertTrue(restore.getMessage().contains("DELETE /api/calendar-admin/terms/{id}"));

        assertThrows(CmsValidationException.class, () -> manager.purge(PROVIDER_KEY, TERM_SLUG, USER));
        assertThrows(CmsValidationException.class, () -> manager.purgeArchived(PROVIDER_KEY, USER));

        verify(termProvider, never()).delete(any(), any());
    }

    // Bekleyen taslak editörün slotudur, listenin penceresi değil: offset, sıralama ve
    // süzgeç ne olursa olsun döner; yalnız archived=true görünümünde yoktur.
    @Test
    void pendingDraftComesBackOnEveryEditorListing() {
        when(itemDao.searchByFilter(eq(KEY), any(), anyList(), anyBoolean(), any(), anyList(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(draftDao.findOwnNewDraft(KEY, USER, "tr")).thenReturn(Optional.of(newDraft()));

        var paged = manager.list(KEY, USER, Map.of("featured", "true"), "publishedAt:desc", false, "tr", null, 40, 20);
        assertNotNull(paged.getVirtualItems());
        assertEquals("pending", paged.getVirtualItems().get(0).getOrigin());
        assertNull(paged.getVirtualItems().get(0).getSlug());

        var archive = manager.list(KEY, USER, null, null, true, "tr", null, 0, 20);
        assertNull(archive.getVirtualItems());
    }

    // Bekleyen slot slug saklamaz; SDK zaten göndermez, gönderse de yok sayılır.
    @Test
    void newDraftSlotStoresNoSlug() {
        when(draftDao.findOwnNewDraft(KEY, USER, "en")).thenReturn(Optional.empty());

        manager.saveNewDraft(KEY, USER, new SaveNewDraftRequestDto(data("title", "Half-written")), "en");

        var saved = ArgumentCaptor.forClass(CollectionDraft.class);
        verify(draftDao).save(saved.capture());
        assertEquals(CollectionDraft.DEFAULT_SLUG, saved.getValue().getSlug());
        assertTrue(saved.getValue().isForNewItem());
    }

    // Arşivdeki satırı editör slug'ıyla okuyabilir (isArchived ile), anonim okuyucu 404 alır.
    @Test
    void archivedRowIsReadableByEditorsOnly() {
        var item = item("eski-haber", "tr", 3);
        item.setArchived(true);
        item.setArchivedAt(Instant.now());
        when(itemDao.findByCollectionKeyAndSlug(KEY, "eski-haber")).thenReturn(Optional.of(item));
        when(itemDao.findByCollectionKeyAndSlugAndArchivedFalse(KEY, "eski-haber")).thenReturn(Optional.empty());
        when(aliasDao.findByCollectionKeyAndSlug(KEY, "eski-haber")).thenReturn(Optional.empty());

        var dto = manager.getBySlug(KEY, "eski-haber", USER, null);
        assertEquals(Boolean.TRUE, dto.getIsArchived());
        assertNotNull(dto.getArchivedAt());
        assertEquals(3, dto.getVersion());
        assertTrue(dto.isCanEdit());

        assertThrows(ResourceNotFoundException.class, () -> manager.getBySlug(KEY, "eski-haber", null, null));
    }

    // Taslak satırın kendi diliyle saklanır; tek kayıt okuması locale göndermediği için
    // sitenin varsayılanına değil satırın diline bakmalı, yoksa İngilizce kaydın taslağı kaybolur.
    @Test
    void draftIsReadUnderTheRowsOwnLocale() {
        var item = item("new-product", "en", 1);
        when(itemDao.findByCollectionKeyAndSlug(KEY, "new-product")).thenReturn(Optional.of(item));
        var draft = CollectionDraft.builder().collectionKey(KEY).slug("new-product").userId(USER).locale("en")
                .payload(data("title", "Edited")).build();
        when(draftDao.findOwnItemDraft(KEY, "new-product", USER, "en")).thenReturn(Optional.of(draft));

        var dto = manager.getBySlug(KEY, "new-product", USER, null);

        assertEquals("Edited", dto.getDraftData().get("title").asString());
    }

    // Sürümsüz PUT yalnız yaratır; var olan satırda 400 döner ki bir "oluştur" başkasının
    // kaydını ezmesin. SDK bu metni /version/i ile tanıyıp "adres dolu" der.
    @Test
    void upsertWithoutVersionRefusesToOverwriteAnExistingRow() {
        when(itemDao.findByCollectionKeyAndSlug(KEY, "yeni-urun")).thenReturn(Optional.of(item("yeni-urun", "tr", 2)));

        var error = assertThrows(CmsValidationException.class,
                () -> manager.upsert(KEY, "yeni-urun", new UpsertCollectionItemRequestDto(data("title", "X"), null), USER, null, null));

        assertTrue(error.getMessage().contains("Version is required"));
    }

    @Test
    void archiveWithoutVersionIsRefused() {
        when(itemDao.findByCollectionKeyAndSlug(KEY, "yeni-urun")).thenReturn(Optional.of(item("yeni-urun", "tr", 2)));

        assertThrows(CmsValidationException.class, () -> manager.archive(KEY, "yeni-urun", null, USER));
    }

    // Yoldaki slug eski bir adresse hiçbir yazma onu takip etmez: 409 reason=moved,
    // conflictingSlug kaydın şimdiki adresi. Publish, taslak, arşiv, geri yükleme, yeniden adlandırma.
    @Test
    void everyWriteToAnOldAddressAnswersMoved() {
        var current = item("yeni-adres", "tr", 1);
        var alias = CollectionSlugAlias.builder().collectionKey(KEY).slug("eski-adres").itemId(current.getId()).build();
        when(itemDao.findByCollectionKeyAndSlug(KEY, "eski-adres")).thenReturn(Optional.empty());
        when(aliasDao.findByCollectionKeyAndSlug(KEY, "eski-adres")).thenReturn(Optional.of(alias));
        when(itemDao.findById(current.getId())).thenReturn(Optional.of(current));

        var upsert = new UpsertCollectionItemRequestDto(data("title", "X"), null);
        assertMoved(assertThrows(SlugConflictException.class,
                () -> manager.upsert(KEY, "eski-adres", upsert, USER, "tr", null)));
        assertMoved(assertThrows(SlugConflictException.class,
                () -> manager.saveItemDraft(KEY, "eski-adres", USER, new SaveDraftRequestDto(data("title", "X")), null)));
        assertMoved(assertThrows(SlugConflictException.class,
                () -> manager.archive(KEY, "eski-adres", 1, USER)));
        assertMoved(assertThrows(SlugConflictException.class,
                () -> manager.restore(KEY, "eski-adres", USER)));
        assertMoved(assertThrows(SlugConflictException.class,
                () -> manager.renameSlug(KEY, "eski-adres", new RenameSlugRequestDto("baska-adres", 1), false, USER)));
    }

    // Yanıt SDK'nın önbelleğine olduğu gibi girer; canEdit düşerse editör salt okunur kalır.
    @Test
    void renameAnswersWithAnEditableRowAtItsNewAddress() {
        var item = item("eski-baslik", "tr", 4);
        when(itemDao.findByCollectionKeyAndSlug(KEY, "eski-baslik")).thenReturn(Optional.of(item));
        when(itemDao.findByCollectionKeyAndSlug(KEY, "yeni-baslik")).thenReturn(Optional.empty());
        when(aliasDao.findByCollectionKeyAndSlug(KEY, "yeni-baslik")).thenReturn(Optional.empty());
        when(itemDao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var renamed = manager.renameSlug(KEY, "eski-baslik", new RenameSlugRequestDto("Yeni Başlık", 4), false, USER);

        assertEquals("yeni-baslik", renamed.getSlug());
        assertEquals(5, renamed.getVersion());
        assertTrue(renamed.isCanEdit());
    }

    private static void assertMoved(SlugConflictException error) {
        assertEquals(SlugConflictException.REASON_MOVED, error.getReason());
        assertEquals("yeni-adres", error.getConflictingSlug());
    }

    private static CollectionItem item(String slug, String locale, int version) {
        return CollectionItem.builder()
                .id(UUID.randomUUID())
                .collectionKey(KEY)
                .slug(slug)
                .locale(locale)
                .translationGroupId(UUID.randomUUID())
                .data(data("title", "Başlık"))
                .version(version)
                .updatedBy(USER)
                .build();
    }

    private static CollectionDraft newDraft() {
        return CollectionDraft.builder()
                .collectionKey(KEY)
                .userId(USER)
                .locale("tr")
                .forNewItem(true)
                .payload(data("title", "Half-written"))
                .build();
    }

    private static ObjectNode data(String field, String value) {
        return JSON.createObjectNode().put(field, value);
    }

    // Çeviri eklerken grup gerçek olmalı; yetim gruba bağlanan kayıt hiçbir kardeşe bağlanmaz.
    @Test
    void createRejectsAnUnknownTranslationGroup() {
        var group = UUID.randomUUID();
        when(itemDao.findByCollectionKeyAndTranslationGroupId(KEY, group)).thenReturn(List.of());

        var request = new com.matmuh.matmuhsite.core.dtos.cms.request.CreateCollectionItemRequestDto();
        request.setData(data("title", "Haber"));

        assertThrows(CmsValidationException.class,
                () -> manager.createWithAutoSlug(KEY, request, USER, "en", group));
    }

    // Aynı grupta aynı dilde ikinci kayıt: TranslationChips ikisini de kardeş sanır.
    @Test
    void createRefusesASecondItemInTheSameLocaleOfAGroup() {
        var group = UUID.randomUUID();
        var existing = CollectionItem.builder().collectionKey(KEY).slug("haber").locale("en")
                .translationGroupId(group).data(data("title", "News")).updatedBy(USER).version(1).build();
        when(itemDao.findByCollectionKeyAndTranslationGroupId(KEY, group)).thenReturn(List.of(existing));

        var request = new com.matmuh.matmuhsite.core.dtos.cms.request.CreateCollectionItemRequestDto();
        request.setData(data("title", "Haber"));

        assertThrows(com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException.class,
                () -> manager.createWithAutoSlug(KEY, request, USER, "en", group));
    }
}
