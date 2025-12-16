package com.josephken.roors.auth.exception;

import com.josephken.roors.common.exception.BusinessException;

public class EmailNotVerifiedException extends BusinessException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
