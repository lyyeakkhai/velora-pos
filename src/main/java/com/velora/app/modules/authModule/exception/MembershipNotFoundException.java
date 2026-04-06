package com.velora.app.modules.authModule.exception;

import java.util.UUID;

public class MembershipNotFoundException extends AuthException {
    public MembershipNotFoundException(UUID membershipId) {
        super("Membership not found: " + membershipId);
    }
}
