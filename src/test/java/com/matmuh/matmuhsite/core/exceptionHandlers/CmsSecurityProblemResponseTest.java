package com.matmuh.matmuhsite.core.exceptionHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matmuh.matmuhsite.core.config.ObjectMapperConfig;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CmsSecurityProblemResponseTest {

    private final ObjectMapper mapper = new ObjectMapperConfig().objectMapper();
    private final MessageResolver messages = new MessageResolver(messages());
    private final CustomAccessDeniedHandler accessDenied = new CustomAccessDeniedHandler(mapper, messages);
    private final CustomAuthenticationEntryPointHandler entryPoint = new CustomAuthenticationEntryPointHandler(mapper, messages);

    private static StaticMessageSource messages() {
        var source = new StaticMessageSource();
        source.addMessage("error.access.denied", Locale.getDefault(), "Yetkiniz yok.");
        source.addMessage("error.authentication.required", Locale.getDefault(), "Giriş yapmalısınız.");
        return source;
    }

    // Filtre katmanının 401/403'ü SDK'ya ErrorResult zarfıyla gidiyordu; SDK `detail` okur,
    // bulamayınca boş statusText'e düşüyordu.
    @Test
    void cmsForbiddenIsProblemJson() throws Exception {
        var response = new MockHttpServletResponse();
        accessDenied.handle(new MockHttpServletRequest("PUT", "/api/cms/content"), response, new AccessDeniedException("no"));

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/problem+json"));
        var body = mapper.readTree(response.getContentAsString());
        assertEquals(403, body.get("status").intValue());
        assertEquals("Yetkiniz yok.", body.get("detail").asText());
        assertEquals("/api/cms/content", body.get("instance").asText());
    }

    @Test
    void cmsUnauthenticatedIsProblemJson() throws Exception {
        var response = new MockHttpServletResponse();
        entryPoint.commence(new MockHttpServletRequest("POST", "/api/cms/sync"), response,
                new InsufficientAuthenticationException("no"));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/problem+json"));
        var body = mapper.readTree(response.getContentAsString());
        assertEquals(401, body.get("status").intValue());
        assertEquals("Giriş yapmalısınız.", body.get("detail").asText());
    }

    @Test
    void otherEndpointsKeepTheErrorEnvelope() throws Exception {
        var request = new MockHttpServletRequest("DELETE", "/api/lectures/1");
        var response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            accessDenied.handle(request, response, new AccessDeniedException("no"));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        var body = mapper.readTree(response.getContentAsString());
        assertFalse(body.get("success").booleanValue());
        assertEquals("Yetkiniz yok.", body.get("message").asText());
    }
}
