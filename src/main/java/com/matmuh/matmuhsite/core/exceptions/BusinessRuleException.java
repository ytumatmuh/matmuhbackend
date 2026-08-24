package com.matmuh.matmuhsite.core.exceptions;

public class BusinessRuleException extends MatmuhException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Object... messageArguments) {
        super(message, messageArguments);
    }
}
