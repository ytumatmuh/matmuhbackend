package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CmsCollectionProvider;
import com.matmuh.matmuhsite.business.constants.AcademicTermCollectionSchema;
import com.matmuh.matmuhsite.business.constants.AnnouncementCollectionSchema;
import com.matmuh.matmuhsite.business.constants.CollectionRegistry;
import com.matmuh.matmuhsite.business.constants.NewsCollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.MyCollectionDto;
import com.matmuh.matmuhsite.core.helpers.CmsLocaleResolver;
import com.matmuh.matmuhsite.core.helpers.FilePreviewEnricher;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CmsLocaleDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CollectionDraftDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CollectionItemDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CollectionSlugAliasDao;
import com.matmuh.matmuhsite.entities.cms.CmsLocale;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CmsCollectionManagerItemCountTest {

    private record Row(String getCollectionKey, String getLocale, long getCount)
            implements CollectionItemDao.LiveItemCount {
    }

    // Panel koleksiyon kartında listelemenin göstereceği sayıyı yazar: arşiv sorguda elenir,
    // dilli koleksiyonda yalnız varsayılan dil (tr) sayılır; sağlayıcılı koleksiyon kendi tablosunu sayar.
    @Test
    void countsWhatTheListingWouldShow() {
        var itemDao = mock(CollectionItemDao.class);
        when(itemDao.countLiveByCollection(any())).thenReturn(List.of(
                new Row(NewsCollectionSchema.KEY, "tr", 3),
                new Row(NewsCollectionSchema.KEY, "en", 2),
                new Row(AnnouncementCollectionSchema.KEY, "tr", 1)));

        var localeDao = mock(CmsLocaleDao.class);
        when(localeDao.findAllByOrderByPositionAsc())
                .thenReturn(List.of(new CmsLocale("tr", 0), new CmsLocale("en", 1)));

        var termProvider = mock(CmsCollectionProvider.class);
        when(termProvider.collectionKey()).thenReturn(AcademicTermCollectionSchema.KEY);
        when(termProvider.count()).thenReturn(7L);

        var manager = new CmsCollectionManager(itemDao, mock(CollectionDraftDao.class), mock(CollectionSlugAliasDao.class),
                new CollectionRegistry(), new CmsLocaleResolver(localeDao), mock(FilePreviewEnricher.class),
                List.of(termProvider));

        Map<String, Long> counts = manager.getMyCollections().stream()
                .collect(Collectors.toMap(MyCollectionDto::getCollectionKey, MyCollectionDto::getItemCount));

        assertEquals(3L, counts.get(NewsCollectionSchema.KEY));
        assertEquals(1L, counts.get(AnnouncementCollectionSchema.KEY));
        assertEquals(7L, counts.get(AcademicTermCollectionSchema.KEY));
    }
}
