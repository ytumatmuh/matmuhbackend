package com.matmuh.matmuhsite.core.exceptions;

public class MatmuhException extends RuntimeException {

    private final Object[] messageArguments;

    public MatmuhException(String message) {
        this(message, new Object[0]);
    }

    public MatmuhException(String message, Object... messageArguments) {
        super(message);
        this.messageArguments = messageArguments == null ? new Object[0] : messageArguments;
    }

    public Object[] getMessageArguments() {
        return messageArguments;
    }
}
