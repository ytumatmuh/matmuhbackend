package com.matmuh.matmuhsite.core.exceptions;

public class ArchivedException extends RuntimeException {

    private final String path;

    private final int version;

    public ArchivedException(String path, int version) {
        super("Item '" + path + "' is archived; restore it before writing to it.");
        this.path = path;
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public int getVersion() {
        return version;
    }
}
