package com.josephken.roors.auth.exception;

import com.josephken.roors.common.exception.BusinessException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailNotFoundException extends BusinessException {
    private String email;
    private String responseMessage;

    public EmailNotFoundException(String message, String email, String responseMessage) {
        super(message);
        this.email = email;
        this.responseMessage = responseMessage;
    }
}
