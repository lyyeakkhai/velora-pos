package com.velora.app.modules.authModule.exception;

public class UnsupportedOAuthProviderException extends AuthException {
    public UnsupportedOAuthProviderException(String provider) {
        super("Unsupported OAuth provider: " + provider);
    }
}
