package com.velora.app.infrastructure.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.velora.app.modules.authModule.domain.PasswordEncoder;

/**
 * BCrypt implementation of PasswordEncoder.
 * 
 * <p>
 * Uses the BCrypt algorithm with a cost factor of 12 for secure password
 * hashing.
 * BCrypt is designed to be slow and memory-intensive, making it resistant to
 * brute-force attacks.
 */
public class BCryptPasswordEncoder implements PasswordEncoder {

    private static final int DEFAULT_COST = 12;

    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        return BCrypt.withDefaults().hashToString(DEFAULT_COST, rawPassword.toCharArray());
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword).verified;
    }

    @Override
    public boolean needsReencode(String encodedPassword) {
        if (encodedPassword == null) {
            return true;
        }
        // Check if the cost factor needs updating (currently using 12)
        // Format: $2a$12$... or $2b$12$...
        try {
            String[] parts = encodedPassword.split("\\$");
            if (parts.length >= 3) {
                int cost = Integer.parseInt(parts[2]);
                return cost != DEFAULT_COST;
            }
        } catch (NumberFormatException e) {
            // Invalid format, should re-encode
            return true;
        }
        return false;
    }
}
