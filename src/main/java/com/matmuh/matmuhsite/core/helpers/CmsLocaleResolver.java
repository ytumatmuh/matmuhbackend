package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.dataAccess.abstracts.cms.CmsLocaleDao;
import com.matmuh.matmuhsite.entities.cms.CmsLocale;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;


@Component
public class CmsLocaleResolver {

    private static final Pattern VALID_CODE = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    private static final int MAX_CODE_LENGTH = 16;

    private final CmsLocaleDao cmsLocaleDao;

    public CmsLocaleResolver(CmsLocaleDao cmsLocaleDao) {
        this.cmsLocaleDao = cmsLocaleDao;
    }


    public static String normalize(String code) {
        return code == null ? null : code.trim().toLowerCase(java.util.Locale.ROOT);
    }

    public List<String> declared() {
        return cmsLocaleDao.findAllByOrderByPositionAsc().stream().map(CmsLocale::getCode).toList();
    }

    public boolean isLocalized() {
        return !declared().isEmpty();
    }


    public String defaultLocale() {
        return declared().stream().findFirst().orElse(null);
    }


    public String resolveForRead(String requested) {
        var locales = declared();
        if (locales.isEmpty()) {
            return null;
        }
        if (requested == null || requested.isBlank()) {
            return locales.get(0);
        }
        var normalized = normalize(requested);
        return locales.contains(normalized) ? normalized : locales.get(0);
    }


    public String requireForWrite(String requested) {
        return requireForWrite(requested, null);
    }

    public String requireForWrite(String requested, String collectionKey) {
        var locales = declared();

        if (locales.isEmpty()) {
            if (requested != null && !requested.isBlank()) {
                throw new CmsValidationException(CmsMessages.LOCALE_NOT_DECLARED + requested);
            }
            return null;
        }


        if (requested == null || requested.isBlank()) {
            var scope = collectionKey == null ? "" : " Collection: " + collectionKey + ".";
            throw new CmsValidationException(
                    CmsMessages.LOCALE_REQUIRED_FOR_WRITE + String.join(", ", locales) + "." + scope);
        }

        var normalized = normalize(requested);
        if (!locales.contains(normalized)) {
            throw new CmsValidationException(CmsMessages.LOCALE_NOT_DECLARED + requested);
        }
        return normalized;
    }


    @Transactional
    public List<String> replaceDeclared(List<String> locales) {
        if (locales == null) {
            return declared();
        }

        var normalized = locales.stream()
                .map(code -> code == null ? "" : normalize(code))
                .filter(code -> !code.isEmpty())
                .distinct()
                .toList();

        for (var code : normalized) {
            if (code.length() > MAX_CODE_LENGTH || !VALID_CODE.matcher(code).matches()) {
                throw new CmsValidationException(CmsMessages.LOCALE_INVALID + code);
            }
        }

        cmsLocaleDao.deleteAllInBatch();
        for (int position = 0; position < normalized.size(); position++) {
            cmsLocaleDao.save(new CmsLocale(normalized.get(position), position));
        }
        return normalized;
    }
}
