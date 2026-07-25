package com.matmuh.matmuhsite.core.helpers;

import jakarta.servlet.http.HttpServletResponse;

public final class CmsCacheHeaders {

    private CmsCacheHeaders() {}

    public static void anonymous(HttpServletResponse response) {
        response.setHeader("Cache-Control", "public, max-age=60, stale-while-revalidate=300");
        response.setHeader("Vary", "Authorization");
    }

    public static void editor(HttpServletResponse response) {
        response.setHeader("Cache-Control", "private, no-store");
        response.setHeader("Vary", "Authorization");
    }
}
