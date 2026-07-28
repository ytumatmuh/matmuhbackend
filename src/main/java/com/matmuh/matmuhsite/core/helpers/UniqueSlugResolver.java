package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.exceptions.BusinessRuleException;
import com.matmuh.matmuhsite.core.exceptions.ResourceAlreadyExistsException;

import java.util.function.Predicate;

public final class UniqueSlugResolver {

    private UniqueSlugResolver() {}

    public static String resolve(String preferredSource, String fallbackSource, Predicate<String> taken) {
        var base = SlugGenerator.slugify(preferredSource);
        if (base.isBlank()) {
            base = SlugGenerator.slugify(fallbackSource);
        }
        if (base.isBlank()) {
            return "";
        }

        var candidate = base;
        int suffix = 2;
        while (taken.test(candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    public static String resolve(String requestedSlug, String preferredSource, String fallbackSource, Predicate<String> taken,
                                 String invalidMessage,
                                 String takenMessage) {

        if (requestedSlug == null || requestedSlug.isBlank()) {
            return resolve(preferredSource, fallbackSource, taken);
        }

        var slug = SlugGenerator.slugify(requestedSlug);
        if (slug.isBlank()) {
            throw new BusinessRuleException(invalidMessage);
        }

        if (taken.test(slug)) {
            throw new ResourceAlreadyExistsException(takenMessage);
        }
        return slug;
    }
}
