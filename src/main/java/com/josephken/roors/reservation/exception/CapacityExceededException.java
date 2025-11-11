package com.josephken.roors.reservation.exception;

import com.josephken.roors.common.exception.BusinessException;

public class CapacityExceededException extends BusinessException {
    public CapacityExceededException(String message) {
        super(message);
    }
}
