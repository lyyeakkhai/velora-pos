package com.velora.app.modules.authModule.exception;

public class UsernameAlreadyTakenException extends AuthException {
    public UsernameAlreadyTakenException(String username) {
        super("Username is already taken: " + username);
    }
}
