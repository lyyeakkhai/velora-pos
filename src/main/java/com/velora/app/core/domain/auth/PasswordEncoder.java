package com.velora.app.core.domain.auth;

/**
 * Domain-layer contract for password hashing and verification.
 *
 * <p>Infrastructure implementations (e.g. BCrypt) are injected at the
 * application boundary so the domain stays free of framework dependencies.
 */
public interface PasswordEncoder {

    /**
     * Hashes a raw password.
     *
     * @param rawPassword the plain-text password
     * @return the hashed representation
     */
    String encode(String rawPassword);

    /**
     * Verifies a raw password against a stored hash.
     *
     * @param rawPassword the plain-text password to check
     * @param encodedPassword the stored hash
     * @return true if they match, false otherwise
     */
    boolean matches(String rawPassword, String encodedPassword);
}
