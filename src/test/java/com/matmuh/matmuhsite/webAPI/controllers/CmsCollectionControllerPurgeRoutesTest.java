package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.CmsCollectionService;
import com.matmuh.matmuhsite.core.dtos.cms.response.PurgeResultDto;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// "/{key}/purge" sabit yolu "/{key}/{slug}" arşivleme yolundan önce eşleşmeli; aksi halde
// arşivi boşaltmak "purge" slug'lı bir item'ı arşivlemeye çalışırdı.
class CmsCollectionControllerPurgeRoutesTest {

    private final CmsCollectionService service = mock(CmsCollectionService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new CmsCollectionController(service)).build();

    private final UsernamePasswordAuthenticationToken editor =
            new UsernamePasswordAuthenticationToken("necip", "n/a", List.of(new SimpleGrantedAuthority("ROLE_EDITOR")));

    @Test
    void collectionPurgeDoesNotFallIntoTheArchiveRoute() throws Exception {
        when(service.purgeArchived("news", "necip")).thenReturn(new PurgeResultDto(3));

        mvc.perform(delete("/api/cms/collections/news/purge").principal(editor))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"purged\":3}"))
                .andExpect(jsonPath("$.purged").value(3));

        verify(service, never()).archive(any(), any(), any(), any());
    }

    @Test
    void itemPurgeAnswers204() throws Exception {
        mvc.perform(delete("/api/cms/collections/news/eski-haber/purge").principal(editor))
                .andExpect(status().isNoContent());

        verify(service).purge("news", "eski-haber", "necip");
        verify(service, never()).archive(any(), any(), any(), any());
    }
}
