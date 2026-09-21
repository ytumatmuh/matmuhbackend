package com.matmuh.matmuhsite.core.exceptions;

import java.util.List;

public class ConcurrencyConflictException extends RuntimeException {

    public record BlockConflict(String path, int expected, int provided) {}

    private final List<BlockConflict> conflicts;

    public ConcurrencyConflictException(String message) {
        this(message, List.of());
    }

    public ConcurrencyConflictException(String message, List<BlockConflict> conflicts) {
        super(message);
        this.conflicts = conflicts == null ? List.of() : List.copyOf(conflicts);
    }

    public List<BlockConflict> getConflicts() {
        return conflicts;
    }
}
