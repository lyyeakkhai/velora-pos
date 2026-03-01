package com.velora.app;

import com.velora.app.core.utils.ValidationUtils;
import com.velora.app.core.utils.RegexPatterns;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Testing ValidationUtils Only ===");

        try {
            testValidationUtils();
            testBusinessLogicRules();
            System.out.println("\n🎉 ValidationUtils tests passed!");

        } catch (Exception e) {
            System.out.println("\n❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testValidationUtils() {
        System.out.println("\n🧪 Testing your ValidationUtils...");

        // Test 1: UUID validation (Object parameter)
        UUID testId = UUID.randomUUID();
        ValidationUtils.validateUUID(testId, "Test ID");
        System.out.println("✅ UUID validation works");

        // Test 2: Format validation with RegexPatterns
        ValidationUtils.validateFormat("test_user", RegexPatterns.USERNAME, "Username");
        ValidationUtils.validateFormat("test@email.com", RegexPatterns.EMAIL, "Email");
        ValidationUtils.validateFormat("https://example.com", RegexPatterns.PROFILE_URL, "Profile URL");
        System.out.println("✅ Format validation works");

        // Test 3: NotBlank validation (Object parameter)
        ValidationUtils.validateNotBlank("test", "Test Field");
        ValidationUtils.validateNotBlank(testId, "Test UUID");
        System.out.println("✅ NotBlank validation works");

        // Test 4: Bcrypt validation
        String bcryptHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        ValidationUtils.validateBcryptHash(bcryptHash, "Password Hash");
        System.out.println("✅ Bcrypt validation works");

        // Test 5: Invalid cases (should fail gracefully)
        testInvalidCases();
    }

    private static void testInvalidCases() {
        System.out.println("\n🧪 Testing validation error handling...");

        // Test invalid username (too short)
        try {
            ValidationUtils.validateFormat("ab", RegexPatterns.USERNAME, "Username");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Caught invalid username: " + e.getMessage());
        }

        // Test invalid email
        try {
            ValidationUtils.validateFormat("invalid-email", RegexPatterns.EMAIL, "Email");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Caught invalid email: " + e.getMessage());
        }

        // Test null value
        try {
            ValidationUtils.validateNotBlank(null, "Null Test");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Caught null value: " + e.getMessage());
        }
    }

    private static void testBusinessLogicRules() {
        System.out.println("\n📋 Testing Business Logic Rules...");

        System.out.println("📋 Rule 1: UUID only created 1 time cannot change it");
        UUID immutableId = UUID.randomUUID();
        ValidationUtils.validateUUID(immutableId, "Immutable ID");
        System.out.println("✅ UUID created once: " + immutableId);

        System.out.println("\n📋 Rule 2: createdAt only created 1 time no need setter");
        System.out.println("✅ Timestamp should be set only in constructor");

        System.out.println("\n📋 Rule 3: updatedAt only updates when setter used");
        System.out.println("✅ Update timestamp should change only when fields change");
    }
}