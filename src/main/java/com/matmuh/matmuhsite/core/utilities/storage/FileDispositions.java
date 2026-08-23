package com.matmuh.matmuhsite.core.utilities.storage;

import java.nio.charset.StandardCharsets;

public final class FileDispositions {

    private static final String UNRESERVED = "!#$&+-.^_`|~";

    private FileDispositions() {}


    public static String inline(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "inline";
        }

        var trimmed = originalFileName.trim();

        return "inline; filename=\"" + StorageKeys.sanitize(trimmed) + "\""
                + "; filename*=UTF-8''" + encode(trimmed);
    }

    private static String encode(String value) {
        var builder = new StringBuilder();

        for (byte raw : value.getBytes(StandardCharsets.UTF_8)) {
            var unsigned = raw & 0xFF;
            var character = (char) unsigned;

            if (Character.isLetterOrDigit(character) && unsigned < 128) {
                builder.append(character);
            } else if (UNRESERVED.indexOf(character) >= 0 && unsigned < 128) {
                builder.append(character);
            } else {
                builder.append('%').append(String.format("%02X", unsigned));
            }
        }

        return builder.toString();
    }
}
