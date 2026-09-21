package com.matmuh.matmuhsite.core.exceptionHandlers;

import com.matmuh.matmuhsite.business.abstracts.CmsMediaService;
import com.matmuh.matmuhsite.business.abstracts.ContentService;
import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.core.exceptions.FileSizeExceededException;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.webAPI.controllers.CmsContentController;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CmsExceptionHandlerTest {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;
    private static final String MANIFEST_WITH_UNKNOWN_TYPE =
            "[{\"slug\":\"/home\",\"blocks\":[{\"blockPath\":\"hero.title\",\"blockType\":\"Nope\",\"defaultValue\":\"\",\"sortOrder\":1}]}]";
    private static final String MANIFEST_WITHOUT_TYPE =
            "[{\"slug\":\"/home\",\"blocks\":[{\"blockPath\":\"hero.title\",\"defaultValue\":\"\",\"sortOrder\":1}]}]";
    private static final String PUBLISH_BODY =
            "{\"slug\":\"/home\",\"blocks\":[{\"blockPath\":\"hero.title\",\"value\":\"x\",\"version\":3}]}";

    private final ContentService contentService = mock(ContentService.class);
    private final CmsMediaService mediaService = mock(CmsMediaService.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new CmsContentController(contentService, mediaService))
            .setControllerAdvice(new CmsExceptionHandler(new MessageResolver(messages())))
            .build();

    private static StaticMessageSource messages() {
        var source = new StaticMessageSource();
        source.addMessage("file.size.exceeded.limit", Locale.ENGLISH, "En fazla {0} MB");
        return source;
    }

    private static UsernamePasswordAuthenticationToken editor() {
        return new UsernamePasswordAuthenticationToken("u1", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    // cms-sync bilinmeyen tipi 500 "beklenmeyen hata" olarak görüyordu; SDK `detail`i basar,
    // hangi bloğun reddedildiği orada yazmalı.
    @Test
    void unknownBlockTypeIsA400NamingTheBlock() throws Exception {
        mvc.perform(post("/api/cms/sync").contentType(MediaType.APPLICATION_JSON).content(MANIFEST_WITH_UNKNOWN_TYPE))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(containsString("Unknown blockType: Nope")))
                .andExpect(jsonPath("$.detail").value(containsString("[0].blocks[0].blockType")));
    }

    // Liste gövdesinin ihlalleri HandlerMethodValidationException olarak gelir.
    @Test
    void missingBlockTypeIsA400() throws Exception {
        mvc.perform(post("/api/cms/sync").contentType(MediaType.APPLICATION_JSON).content(MANIFEST_WITHOUT_TYPE))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(CmsMessages.BLOCK_TYPE_NOT_NULL));
    }

    @Test
    void missingSlugParameterIsA400() throws Exception {
        mvc.perform(get("/api/cms/content"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(CmsMessages.PARAMETER_REQUIRED + "slug"));
    }

    @Test
    void blankSlugIsA400() throws Exception {
        when(contentService.getPublishedBySlug(anyString(), any())).thenThrow(new IllegalArgumentException("slug is required"));

        mvc.perform(get("/api/cms/content").param("slug", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("slug is required"));
    }

    @Test
    void malformedJsonIsA400() throws Exception {
        mvc.perform(put("/api/cms/content").principal(editor()).contentType(MediaType.APPLICATION_JSON).content("{nope"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("error.request.malformed"));
    }

    // SDK `conflicts[].path` ile kartları işaretler; anahtar yoksa düz yazma yarışı sayar.
    @Test
    void versionConflictListsTheStaleBlocks() throws Exception {
        when(contentService.updatePage(eq("u1"), any(), any())).thenThrow(new ConcurrencyConflictException(
                CmsMessages.VERSION_CONFLICT,
                List.of(new ConcurrencyConflictException.BlockConflict("hero.title", 4, 3))));

        mvc.perform(put("/api/cms/content").principal(editor()).contentType(MediaType.APPLICATION_JSON).content(PUBLISH_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(CmsMessages.VERSION_CONFLICT))
                .andExpect(jsonPath("$.conflicts[0].path").value("hero.title"))
                .andExpect(jsonPath("$.conflicts[0].expected").value(4))
                .andExpect(jsonPath("$.conflicts[0].provided").value(3));
    }

    @Test
    void plainConflictCarriesNoConflictsKey() throws Exception {
        when(contentService.updatePage(eq("u1"), any(), any()))
                .thenThrow(new ConcurrencyConflictException(CmsMessages.VERSION_CONFLICT));

        mvc.perform(put("/api/cms/content").principal(editor()).contentType(MediaType.APPLICATION_JSON).content(PUBLISH_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.conflicts").doesNotExist());
    }

    @Test
    void oversizedUploadIsA413NamingTheLimit() throws Exception {
        when(mediaService.upload(any(), anyBoolean())).thenThrow(new FileSizeExceededException("file.size.exceeded.limit", 25L));

        mvc.perform(multipart("/api/cms/media").locale(Locale.ENGLISH)
                        .file(new MockMultipartFile("file", "big.pdf", "application/pdf", new byte[1])))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(content().contentType(PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.detail").value("En fazla 25 MB"));
    }

    @Test
    void uploadWithoutTheFilePartIsA400() throws Exception {
        mvc.perform(post("/api/cms/media"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value(CmsMessages.FILE_PART_REQUIRED));
    }
}
