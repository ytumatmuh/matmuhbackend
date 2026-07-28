package com.matmuh.matmuhsite.core.security.oauth2;

import com.matmuh.matmuhsite.core.properties.FrontendProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class FrontendCallbackCapturingResolver implements OAuth2AuthorizationRequestResolver {

    public static final String SESSION_KEY = "MATMUH_FRONTEND_CALLBACK";

    private static final String PARAM = "redirect";

    private final Logger logger = LoggerFactory.getLogger(FrontendCallbackCapturingResolver.class);

    private final OAuth2AuthorizationRequestResolver delegate;
    private final FrontendProperties frontendProperties;

    public FrontendCallbackCapturingResolver(OAuth2AuthorizationRequestResolver delegate,
                                             FrontendProperties frontendProperties) {
        this.delegate = delegate;
        this.frontendProperties = frontendProperties;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        var resolved = delegate.resolve(request);
        if (resolved != null) {
            capture(request);
        }
        return resolved;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        var resolved = delegate.resolve(request, clientRegistrationId);
        if (resolved != null) {
            capture(request);
        }
        return resolved;
    }

    private void capture(HttpServletRequest request) {
        var requested = request.getParameter(PARAM);
        if (requested == null || requested.isBlank()) {
            return;
        }
        if (!frontendProperties.isAllowedCallback(requested)) {
            logger.warn("Izin verilmeyen frontend callback istegi yok sayildi: {}", requested);
            return;
        }
        request.getSession(true).setAttribute(SESSION_KEY, requested);
    }
}
