package com.velora.app.core.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

public class ValidationUtils {

    /**
     * Validates that the given value matches the specified format (regex).
     *
     * @param value     The input string to validate
     * @param regex     The regex pattern defining the allowed format
     * @param fieldName Name of the field for error messages
     * @throws IllegalArgumentException if value is null, empty, or doesn't match
     *                                  regex
     */

    public static void validateFormat(String value, String regex, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        if (!value.matches(regex)) {
            throw new IllegalArgumentException(fieldName + " has invalid format");
        }
    }

    /**
     * Validates that the given URL string is a valid HTTP or HTTPS URL.
     *
     * @param url       The URL string to validate
     * @param fieldName Name of the field for error messages
     * @throws IllegalArgumentException if URL is null, empty, or not a valid
     *                                  HTTP/HTTPS URL
     */
    public static void validateUrl(String url, String fieldName) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        if (!url.matches(RegexPatterns.PROFILE_URL)) {
            throw new IllegalArgumentException(fieldName + " is not a valid URL");
        }
    }

    /**
     * Validates that the given value is not null or empty.
     *
     * @param value     The input value to validate
     * @param fieldName Name of the field for error messages
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void validateNotBlank(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        if (value instanceof String && ((String) value).isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
    }

    /**
     * Validates that the given string is a valid UUID format.
     *
     * @param uuid      The string to validate as UUID
     * @param fieldName Name of the field for error messages
     * @throws IllegalArgumentException if uuid is null, empty, or not a valid UUID
     */
    public static void validateUUID(Object uuid, String fieldName) {

        if (uuid == null || uuid.toString().isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        try {
            UUID.fromString(uuid.toString()); // Throws exception if invalid
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(fieldName + " is not a valid UUID", ex);
        }
    }

    /**
     * Validates that the given value is a valid bcrypt hash.
     *
     * @param hash      The hash string to validate
     * @param fieldName Name of the field for error messages
     * @throws IllegalArgumentException if hash is null, empty, or not a valid
     *                                  bcrypt hash
     */
    public static void validateBcryptHash(String hash, String fieldName) {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        if (!hash.matches("^\\$2[ayb]\\$[0-9]{2}\\$[A-Za-z0-9./]{53}$")) {
            throw new IllegalArgumentException(fieldName + " is not a valid bcrypt hash");
        }
    }

    /**
     * Validates that the given value is a valid enum constant.
     *
     * @param value     The string value to validate
     * @param enumClass The enum class to check against
     * @param fieldName Name of the field for error messages
     * @throws IllegalArgumentException if value is null, empty, or not a valid enum
     *                                  constant
     */
    public static <T extends Enum<T>> void validateEnum(String value, Class<T> enumClass, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        try {
            Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(fieldName + " is not a valid enum value");
        }
    }

    /**
     * Validates the string contains exactly {@code digits} numeric characters.
     */
    public static void validateExactDigits(String value, int digits, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        if (digits <= 0) {
            throw new IllegalArgumentException("digits must be > 0");
        }
        if (!value.matches("\\d{" + digits + "}")) {
            throw new IllegalArgumentException(fieldName + " must be exactly " + digits + " digits");
        }
    }

    /**
     * Normalizes a monetary amount to scale=2 using HALF_UP and validates
     * non-negative.
     */
    public static BigDecimal normalizeMoney(BigDecimal amount, String fieldName) {
        validateNotBlank(amount, fieldName);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be >= 0");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Validates a non-negative integer (>= 0).
     */
    public static void validateNonNegativeInteger(Integer value, String fieldName) {
        validateNotBlank(value, fieldName);
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be >= 0");
        }
    }

    /**
     * Validates a strictly positive integer (> 0).
     */
    public static void validatePositiveInteger(Integer value, String fieldName) {
        validateNotBlank(value, fieldName);
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0");
        }
    }

    /**
     * Validates a URL-safe lowercase slug.
     */
    public static void validateSlug(String slug, String fieldName) {
        validateNotBlank(slug, fieldName);
        validateFormat(slug, RegexPatterns.SLUG, fieldName);
        if (!slug.equals(slug.toLowerCase())) {
            throw new IllegalArgumentException(fieldName + " must be lowercase");
        }
    }

    /**
     * Validates a feature key / identifier format.
     * <p>
     * Allowed: lowercase letters, digits, underscore; must start with a letter.
     */
    public static void validateIdentifierKey(String key, String fieldName) {
        validateNotBlank(key, fieldName);
        if (!key.matches("^[a-z][a-z0-9_]{0,63}$")) {
            throw new IllegalArgumentException(fieldName + " has invalid identifier format");
        }
    }

    /**
     * Validates that start is strictly before end.
     */
    public static void validateStartBeforeEnd(LocalDateTime start, LocalDateTime end, String startFieldName,
            String endFieldName) {
        validateNotBlank(start, startFieldName);
        validateNotBlank(end, endFieldName);
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException(startFieldName + " must be before " + endFieldName);
        }
    }

    /**
     * Example: generate default format value if needed
     */
    public static String generateDefault(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 6);
    }
}
