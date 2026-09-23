package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.CmsMediaService;
import com.matmuh.matmuhsite.business.abstracts.ContentService;
import com.matmuh.matmuhsite.core.dtos.cms.response.ContentBundleDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.ContentResponseDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.SyncResultDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CmsContentControllerPathsTest {

    private final ContentService contentService = mock(ContentService.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new CmsContentController(contentService, mock(CmsMediaService.class)))
            .build();

    // SDK 4.4 clientKey ile `/cms/public/{clientKey}/content` çağırır; eski `.../data` da kalır.
    @Test
    void publicClientKeyPathServesPublishedContent() throws Exception {
        when(contentService.getPublishedBySlug("/about", null))
                .thenReturn(new ContentResponseDto("/about", null, List.of()));

        for (var path : new String[]{"/api/cms/public/site/content", "/api/cms/public/site/data"}) {
            mvc.perform(get(path).param("slug", "/about"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Cache-Control", "public, max-age=60, stale-while-revalidate=300"))
                    .andExpect(header().string("Vary", "Authorization"))
                    .andExpect(jsonPath("$.slug").value("/about"))
                    .andExpect(jsonPath("$.locale").doesNotExist())
                    .andExpect(jsonPath("$.blocks").isArray());
        }
        verify(contentService, times(2)).getPublishedBySlug("/about", null);
    }

    // SDK 5.0 sunucu tarafında siteyi sayfa sayfa değil bu tek çağrıyla okur; yoksa render durur.
    @Test
    void wholeSiteReadServesPublishedBundleOnBothPaths() throws Exception {
        when(contentService.getAllPublished("en")).thenReturn(new ContentBundleDto("en",
                List.of(new ContentBundleDto.ContentPageDto("/__global", List.of())),
                List.of(new ContentBundleDto.ContentPageDto("/about", List.of()))));

        for (var path : new String[]{"/api/cms/content/all", "/api/cms/public/site/content/all"}) {
            mvc.perform(get(path).param("locale", "en"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Cache-Control", "public, max-age=60, stale-while-revalidate=300"))
                    .andExpect(jsonPath("$.locale").value("en"))
                    .andExpect(jsonPath("$.global[0].slug").value("/__global"))
                    .andExpect(jsonPath("$.pages[0].slug").value("/about"));
        }
        verify(contentService, times(2)).getAllPublished("en");
    }

    // transport.syncManifests `?locales=tr,en` diye tek parametrede virgülle gönderir.
    @Test
    void commaSeparatedLocalesReachTheServiceAsAList() throws Exception {
        when(contentService.sync(List.of(), List.of("tr", "en")))
                .thenReturn(new SyncResultDto(List.of(), List.of()));

        mvc.perform(post("/api/cms/sync").param("locales", "tr,en")
                        .contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.prunedSlugs").isArray());

        verify(contentService).sync(List.of(), List.of("tr", "en"));
    }
}
