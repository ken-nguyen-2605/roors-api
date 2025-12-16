package com.josephken.roors.auth.exception;

import com.josephken.roors.common.exception.BusinessException;

public class UserAlreadyExistsException extends BusinessException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
