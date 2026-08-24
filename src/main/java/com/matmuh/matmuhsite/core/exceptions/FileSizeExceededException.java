package com.matmuh.matmuhsite.core.exceptions;

public class FileSizeExceededException extends MatmuhException {

    public FileSizeExceededException(String message) {
        super(message);
    }

    public FileSizeExceededException(String message, Object... messageArguments) {
        super(message, messageArguments);
    }
}
