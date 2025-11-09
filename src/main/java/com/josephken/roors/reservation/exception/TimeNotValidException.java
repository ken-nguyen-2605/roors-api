package com.josephken.roors.reservation.exception;

import com.josephken.roors.common.exception.BusinessException;

public class TimeNotValidException extends BusinessException {
    public TimeNotValidException(String message) {
        super(message);
    }
}
