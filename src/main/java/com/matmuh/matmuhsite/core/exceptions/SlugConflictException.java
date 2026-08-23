package com.matmuh.matmuhsite.core.exceptions;

public class SlugConflictException extends RuntimeException {

    public static final String REASON_TAKEN = "taken";
    public static final String REASON_ALIAS = "alias";

    private final String reason;
    private final String conflictingSlug;

    public SlugConflictException(String message, String reason, String conflictingSlug) {
        super(message);
        this.reason = reason;
        this.conflictingSlug = conflictingSlug;
    }

    public String getReason() {
        return reason;
    }

    public String getConflictingSlug() {
        return conflictingSlug;
    }
}
