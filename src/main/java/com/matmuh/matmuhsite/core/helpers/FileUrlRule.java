package com.matmuh.matmuhsite.core.helpers;

import tools.jackson.databind.JsonNode;

import java.util.Locale;

// Site File değerinin url'ini doğrudan bir bağlantıya basar; `javascript:` ya da `data:` bir
// adres okuyucunun tarayıcısında çalışacak bir script olurdu.
public final class FileUrlRule {

    public static final String EXPECTATION = "must be empty, an http or https address, or a path starting with '/'";

    private FileUrlRule() {}

    public static boolean accepts(JsonNode value) {
        if (value == null || !value.isObject() || !value.has("url")) {
            return true;
        }
        var url = value.get("url");
        return url.isTextual() && isAllowed(url.asText());
    }

    private static boolean isAllowed(String url) {
        var lower = url.toLowerCase(Locale.ROOT);
        return url.isEmpty()
                || url.startsWith("/")
                || lower.startsWith("http:")
                || lower.startsWith("https:");
    }
}
