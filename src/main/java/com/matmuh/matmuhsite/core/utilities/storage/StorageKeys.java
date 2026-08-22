package com.matmuh.matmuhsite.core.utilities.storage;

import java.util.UUID;

public final class StorageKeys {

    public static final String PUBLIC_PREFIX = "images/";
    public static final String PRIVATE_PREFIX = "files/";

    private StorageKeys() {}

    public static String newKey(FolderType folderType, String originalFileName) {
        var sanitized = originalFileName == null || originalFileName.isBlank()
                ? "file"
                : originalFileName.replaceAll("\\s+", "_");

        return prefix(folderType) + UUID.randomUUID() + "-" + sanitized;
    }

    public static String prefix(FolderType folderType) {
        return folderType == FolderType.IMAGE ? PUBLIC_PREFIX : PRIVATE_PREFIX;
    }

    public static boolean isPrivate(String key) {
        return key != null && key.startsWith(PRIVATE_PREFIX);
    }
}
