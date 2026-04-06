package com.velora.app.modules.authModule.exception;

import com.velora.app.common.DomainException;

public class AuthException extends DomainException {
    public AuthException(String message) {
        super(message);
    }
}
