package com.josephken.roors.auth.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailNotFoundException extends RuntimeException {
    private String email;
    private String responseMessage;

    public EmailNotFoundException(String message, String email, String responseMessage) {
        super(message);
        this.email = email;
        this.responseMessage = responseMessage;
    }
}
