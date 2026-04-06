package com.velora.app.modules.authModule.exception;

import com.velora.app.modules.authModule.domain.Role;

public class UnauthorizedRoleAssignmentException extends AuthException {
    public UnauthorizedRoleAssignmentException(Role.RoleName actual) {
        super("Role " + actual + " is not authorized for this operation");
    }
}
