package com.matmuh.matmuhsite.core.exceptions;

public class ResourceAlreadyExistsException extends MatmuhException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String message, Object... messageArguments) {
        super(message, messageArguments);
    }
}
