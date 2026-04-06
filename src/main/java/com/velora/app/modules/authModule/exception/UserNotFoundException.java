package com.velora.app.modules.authModule.exception;

import java.util.UUID;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException(UUID userId) {
        super("User not found: " + userId);
    }
}
