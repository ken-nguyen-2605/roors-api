package com.josephken.roors.auth.exception;

import com.josephken.roors.common.exception.BusinessException;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
