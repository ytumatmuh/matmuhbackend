package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.CmsCollectionService;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// SecurityConfig EDITOR'e yazma verir; controller da onu editör saymalı, yoksa panelde
// canEdit=false, taslak yok, virtualItems yok — ama canCreate=true ile çelişir.
class CmsCollectionControllerEditorRoleTest {

    private final CmsCollectionService service = mock(CmsCollectionService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new CmsCollectionController(service)).build();

    private String userIdSeenFor(String role) throws Exception {
        when(service.allowsAnonymousRead("news")).thenReturn(true);
        when(service.list(eq("news"), any(), any(), any(), anyBoolean(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new CollectionListDto(List.of(), 0, 0, 50));
        var auth = role == null ? null
                : new UsernamePasswordAuthenticationToken("necip", "n/a", List.of(new SimpleGrantedAuthority(role)));

        var request = get("/api/cms/collections/news");
        if (auth != null) request = request.principal(auth);
        mvc.perform(request).andExpect(status().isOk());

        var captor = ArgumentCaptor.forClass(String.class);
        verify(service).list(eq("news"), captor.capture(), any(), any(), anyBoolean(), any(), any(), anyInt(), anyInt());
        return captor.getValue();
    }

    @Test
    void editorRoleIsAnEditor() throws Exception {
        assertEquals("necip", userIdSeenFor("ROLE_EDITOR"));
    }

    @Test
    void plainUserIsAReader() throws Exception {
        assertNull(userIdSeenFor("ROLE_USER"));
    }
}
