package com.matmuh.matmuhsite.core.helpers;

import java.util.Locale;

public final class SlugNormalizer {

    private SlugNormalizer() {}

    public static String normalizeSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("slug is required");
        }
        var lowered = slug.trim().toLowerCase(Locale.ROOT);
        if (!lowered.startsWith("/")) {
            lowered = "/" + lowered;
        }
        while (lowered.length() > 1 && lowered.endsWith("/")) {
            lowered = lowered.substring(0, lowered.length() - 1);
        }
        return lowered;
    }

    public static String normalizeBlockPath(String blockPath) {
        if (blockPath == null || blockPath.isBlank()) {
            throw new IllegalArgumentException("blockPath is required");
        }
        return blockPath.trim().toLowerCase(Locale.ROOT);
    }
}