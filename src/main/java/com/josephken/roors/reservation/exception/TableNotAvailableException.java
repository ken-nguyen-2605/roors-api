package com.josephken.roors.reservation.exception;

import com.josephken.roors.common.exception.BusinessException;

public class TableNotAvailableException extends BusinessException {
    public TableNotAvailableException(String message) {
        super(message);
    }
}
