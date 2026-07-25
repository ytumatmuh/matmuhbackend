package com.matmuh.matmuhsite.core.helpers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Component
public class OriginValidator {

    private final List<String> allowedOrigins;

    public OriginValidator(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }


    public boolean isTrusted(HttpServletRequest request) {
        var origin = request.getHeader("Origin");
        if (origin == null) {
            var referer = request.getHeader("Referer");
            if (referer == null) {
                return false;
            }
            origin = toOrigin(referer);
        }
        var candidate = origin;
        return allowedOrigins.stream().anyMatch(allowed -> allowed.equalsIgnoreCase(candidate))
                || isSameOrigin(request, candidate);
    }

    private boolean isSameOrigin(HttpServletRequest request, String origin) {
        var scheme = request.getScheme();
        var host = request.getServerName();
        var port = request.getServerPort();
        var expected = scheme + "://" + host;
        if (!(("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443))) {
            expected = expected + ":" + port;
        }
        return expected.equalsIgnoreCase(origin);
    }

    private String toOrigin(String url) {
        try {
            var uri = URI.create(url);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return "";
            }
            var origin = uri.getScheme() + "://" + uri.getHost();
            return uri.getPort() == -1 ? origin : origin + ":" + uri.getPort();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}
