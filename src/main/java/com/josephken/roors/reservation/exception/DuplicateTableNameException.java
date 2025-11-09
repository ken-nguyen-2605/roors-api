package com.josephken.roors.reservation.exception;

import com.josephken.roors.common.exception.BusinessException;

public class DuplicateTableNameException extends BusinessException {
    public DuplicateTableNameException(String message) {
        super(message);
    }
}
