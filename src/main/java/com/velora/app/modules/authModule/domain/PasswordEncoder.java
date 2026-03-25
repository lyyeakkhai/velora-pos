package com.velora.app.modules.authModule.domain;

/**
 * Interface for password encoding and verification.
 * 
 * <p>
 * Implementations should use secure hashing algorithms (e.g., BCrypt, Argon2).
 * This abstraction allows for easy switching between hashing algorithms.
 */
public interface PasswordEncoder {

    /**
     * Encodes a raw password into a secure hash.
     *
     * @param rawPassword The plain-text password to encode
     * @return The encoded password hash
     */
    String encode(String rawPassword);

    /**
     * Verifies a raw password against an encoded hash.
     *
     * @param rawPassword     The plain-text password to verify
     * @param encodedPassword The stored password hash
     * @return true if the password matches, false otherwise
     */
    boolean matches(String rawPassword, String encodedPassword);

    /**
     * Checks if the encoding needs to be updated.
     * 
     * <p>
     * Some implementations may return true when:
     * <ul>
     * <li>The algorithm version has changed</li>
     * <li>Security parameters have been updated</li>
     * </ul>
     *
     * @param encodedPassword The stored password hash
     * @return true if re-encoding is recommended
     */
    default boolean needsReencode(String encodedPassword) {
        return false;
    }
}
