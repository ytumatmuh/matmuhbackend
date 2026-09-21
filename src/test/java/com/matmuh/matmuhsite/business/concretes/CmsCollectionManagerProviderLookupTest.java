package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CmsCollectionProvider;
import com.matmuh.matmuhsite.business.constants.CollectionRegistry;
import com.matmuh.matmuhsite.business.constants.StaffCollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import com.matmuh.matmuhsite.core.helpers.CmsLocaleResolver;
import com.matmuh.matmuhsite.core.helpers.FilePreviewEnricher;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CmsLocaleDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CollectionDraftDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CollectionItemDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CollectionSlugAliasDao;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Sağlayıcı koleksiyonda arama ilk 20 kaydı bellekte süzüyordu: seçici 20. personelden
// sonrasını bulamıyordu (offering'de hoca seçimi). Arama artık bütün sayfaları tarar.
class CmsCollectionManagerProviderLookupTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static CollectionItemDto staff(int index) {
        var dto = new CollectionItemDto();
        dto.setSlug("staff-" + index);
        dto.setData(JSON.createObjectNode().put("fullName", index == 137 ? "Prof. Dr. Ayşe Yılmaz" : "Hoca " + index));
        return dto;
    }

    @Test
    void findsAMatchBeyondTheFirstPage() {
        var all = IntStream.range(0, 150).mapToObj(CmsCollectionManagerProviderLookupTest::staff).toList();
        var provider = mock(CmsCollectionProvider.class);
        when(provider.collectionKey()).thenReturn(StaffCollectionSchema.KEY);
        when(provider.list(any(), any(), anyInt(), anyInt())).thenAnswer(invocation -> {
            int offset = invocation.getArgument(2);
            int limit = invocation.getArgument(3);
            var page = all.subList(Math.min(offset, all.size()), Math.min(offset + limit, all.size()));
            return new CollectionListDto(page, all.size(), offset, limit);
        });

        var localeDao = mock(CmsLocaleDao.class);
        when(localeDao.findAllByOrderByPositionAsc()).thenReturn(List.of());
        var enricher = mock(FilePreviewEnricher.class);
        when(enricher.enrich(any(), any())).thenAnswer(invocation -> invocation.getArgument(1));
        var manager = new CmsCollectionManager(mock(CollectionItemDao.class), mock(CollectionDraftDao.class),
                mock(CollectionSlugAliasDao.class), new CollectionRegistry(), new CmsLocaleResolver(localeDao),
                enricher, List.of(provider));

        var result = manager.lookup(StaffCollectionSchema.KEY, "yılmaz", null, null, 20);

        assertEquals(1, result.total());
        assertEquals("staff-137", result.items().get(0).slug());
        assertEquals("Prof. Dr. Ayşe Yılmaz", result.items().get(0).label());
    }
}
