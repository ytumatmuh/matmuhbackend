package com.matmuh.matmuhsite.core.exceptions;

import java.util.List;

public class CmsValidationException extends RuntimeException {

    private final List<String> errors;

    public CmsValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = errors;
    }

    public CmsValidationException(String error) {
        this(List.of(error));
    }

    public List<String> getErrors() {
        return errors;
    }
}
