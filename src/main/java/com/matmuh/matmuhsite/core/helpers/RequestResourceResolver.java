package com.matmuh.matmuhsite.core.helpers;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.regex.Pattern;

public final class RequestResourceResolver {

    private RequestResourceResolver() {}

    private static final Pattern IDENTIFIER = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$|^\\d+$");

    private static final String API_SEGMENT = "api";

    public record Target(String resource, String id) {
        static final Target EMPTY = new Target(null, null);
    }

    public static Target resolve() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return Target.EMPTY;
        }

        return resolve(servletAttributes.getRequest().getRequestURI());
    }

    public static Target resolve(String uri) {
        if (uri == null || uri.isBlank()) {
            return Target.EMPTY;
        }

        var segments = uri.split("/");
        String resource = null;

        for (var segment : segments) {
            if (segment.isBlank() || segment.equals(API_SEGMENT)) {
                continue;
            }

            if (resource != null && IDENTIFIER.matcher(segment).matches()) {
                return new Target(resource, segment);
            }

            resource = segment;
        }

        return new Target(firstSegment(segments), null);
    }

    private static String firstSegment(String[] segments) {
        for (var segment : segments) {
            if (!segment.isBlank() && !segment.equals(API_SEGMENT)) {
                return segment;
            }
        }
        return null;
    }
}
