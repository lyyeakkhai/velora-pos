package com.velora.app.core.utils;

public class RegexPatterns {

    // Username: letters, numbers, underscore, 3-30 chars
    public static final String USERNAME = "^[a-zA-Z0-9_]{3,30}$";

    // Email: basic email format
    public static final String EMAIL = "^\\S+@\\S+\\.\\S+$";

    // Profile URL: simple URL validation (supports http/https and domains)
    public static final String PROFILE_URL = "^(https?://)?[\\w.-]+(\\.[\\w.-]+)+[/#?]?.*$";

    // UUID: standard 8-4-4-4-12 hex digits
    public static final String UUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    //providerID for OAuth: letters, numbers, hyphen, 3-50 chars
    public static final String PROVIDER_UID = "^[a-zA-Z0-9-]{3,50}$";

    // Password: at least 8 chars, at least one letter and one number
    public static final String PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";

    // Bio: allow letters, numbers, spaces, punctuation, up to 160 chars
    public static final String BIO = "^.{0,160}$";

    // Phone number: digits only, 7-15 chars
    public static final String PHONE_NUMBER = "^[0-9]{7,15}$";

    // Hex color code: e.g., #FFFFFF or #FFF
    public static final String HEX_COLOR = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";

    // Simple date format YYYY-MM-DD
    public static final String DATE = "^\\d{4}-\\d{2}-\\d{2}$";

    // Simple time format HH:MM:SS
    public static final String TIME = "^([01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d$";

    // Alpha only: letters only, 1-50 chars
    public static final String ALPHA = "^[a-zA-Z]{1,50}$";

    // Alpha-numeric: letters + digits, 1-50 chars
    public static final String ALPHANUMERIC = "^[a-zA-Z0-9]{1,50}$";

    // Slug: lowercase letters, numbers, hyphen, 1-50 chars
    public static final String SLUG = "^[a-z0-9-]{1,50}$";

    // IP address (IPv4)
    public static final String IPV4 = "^((25[0-5]|2[0-4]\\d|[01]?\\d?\\d)(\\.|$)){4}$";

    // IP address (IPv6)
    public static final String IPV6 = "([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}";

}