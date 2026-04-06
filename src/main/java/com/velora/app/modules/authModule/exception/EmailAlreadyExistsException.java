package com.velora.app.modules.authModule.exception;

public class EmailAlreadyExistsException extends AuthException {
    public EmailAlreadyExistsException(String email) {
        super("Email is already registered: " + email);
    }
}
