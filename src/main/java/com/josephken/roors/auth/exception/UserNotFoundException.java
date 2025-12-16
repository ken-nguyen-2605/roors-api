package com.josephken.roors.auth.exception;

import com.josephken.roors.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(String message) {
        super(message);
    }
}