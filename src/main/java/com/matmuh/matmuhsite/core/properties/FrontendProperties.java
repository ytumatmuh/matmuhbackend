package com.matmuh.matmuhsite.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@ConfigurationProperties(prefix = "app.frontend")
@Getter
@Setter
public class FrontendProperties {

    private String callbackUrl;

    private List<String> allowedCallbacks = new ArrayList<>();

    public boolean isAllowedCallback(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        if (allowedCallbacks.contains(url)) {
            return true;
        }
        var origin = originOf(url);
        if (origin == null) {
            return false;
        }
        return Stream.concat(allowedCallbacks.stream(), Stream.of(callbackUrl))
                .map(FrontendProperties::originOf)
                .anyMatch(origin::equalsIgnoreCase);
    }

    private static String originOf(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            var uri = URI.create(url);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }
            var origin = uri.getScheme() + "://" + uri.getHost();
            return uri.getPort() == -1 ? origin : origin + ":" + uri.getPort();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
