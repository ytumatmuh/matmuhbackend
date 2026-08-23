package com.matmuh.matmuhsite.core.utilities.storage;

import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

public final class StorageKeys {

    public static final String PUBLIC_PREFIX = "images/";
    public static final String PRIVATE_PREFIX = "files/";

    private static final String TURKISH = "çÇğĞıİöÖşŞüÜ";
    private static final String ASCII = "cCgGiIoOsSuU";
    private static final int MAX_EXTENSION_LENGTH = 10;

    private StorageKeys() {}

    public static String newKey(FolderType folderType, String originalFileName) {
        return prefix(folderType) + UUID.randomUUID() + extensionOf(originalFileName);
    }

    static String extensionOf(String originalFileName) {
        if (originalFileName == null) {
            return "";
        }

        var dot = originalFileName.lastIndexOf('.');
        if (dot < 0 || dot == originalFileName.length() - 1) {
            return "";
        }

        var extension = originalFileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (extension.length() > MAX_EXTENSION_LENGTH || !extension.chars().allMatch(Character::isLetterOrDigit)) {
            return "";
        }

        return "." + extension;
    }

    public static String prefix(FolderType folderType) {
        return folderType == FolderType.IMAGE ? PUBLIC_PREFIX : PRIVATE_PREFIX;
    }

    public static boolean isPrivate(String key) {
        return key != null && key.startsWith(PRIVATE_PREFIX);
    }


    public static String fromRequestPath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return rawPath;
        }
        return UriUtils.decode(rawPath, StandardCharsets.UTF_8);
    }

    public static String sanitize(String originalFileName) {
        var builder = new StringBuilder(originalFileName.length());

        for (var character : originalFileName.toCharArray()) {
            var index = TURKISH.indexOf(character);
            var mapped = index >= 0 ? ASCII.charAt(index) : character;

            if (mapped == '.' || mapped == '-' || mapped == '_'
                    || (mapped >= 'a' && mapped <= 'z')
                    || (mapped >= 'A' && mapped <= 'Z')
                    || (mapped >= '0' && mapped <= '9')) {
                builder.append(mapped);
            } else {
                builder.append('_');
            }
        }

        var collapsed = builder.toString().replaceAll("_+", "_");
        return collapsed.isBlank() || "_".equals(collapsed) ? "file" : collapsed;
    }
}
