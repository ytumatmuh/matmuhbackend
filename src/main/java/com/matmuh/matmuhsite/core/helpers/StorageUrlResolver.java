package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.utilities.storage.StorageKeys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StorageUrlResolver {

    private static final String UPLOADS_PATH = "/api/uploads/";

    private final String publicDomain;
    private final String apiUrl;

    public StorageUrlResolver(@Value("${app.storage.domain}") String publicDomain,
                              @Value("${api.url}") String apiUrl) {
        this.publicDomain = trimTrailingSlash(publicDomain);
        this.apiUrl = trimTrailingSlash(apiUrl);
    }

    public String urlFor(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }

        var normalized = key.startsWith("/") ? key.substring(1) : key;

        if (StorageKeys.isPrivate(normalized) || StorageKeys.isPublicFile(normalized)) {
            return apiUrl + UPLOADS_PATH + normalized;
        }
        return publicDomain + "/" + normalized;
    }


    public String keyFor(String url) {
        if (url == null) {
            return null;
        }
        var marker = url.indexOf(UPLOADS_PATH);
        if (marker < 0) {
            return null;
        }
        var rest = url.substring(marker + UPLOADS_PATH.length());
        var cut = rest.indexOf('?');
        var key = StorageKeys.fromRequestPath(cut < 0 ? rest : rest.substring(0, cut));
        return key.isBlank() ? null : key;
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        var trimmed = value.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
