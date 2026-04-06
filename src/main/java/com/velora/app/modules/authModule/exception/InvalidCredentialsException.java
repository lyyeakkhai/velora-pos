package com.velora.app.modules.authModule.exception;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
