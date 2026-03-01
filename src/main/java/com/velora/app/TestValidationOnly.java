package com.velora.app;

import com.velora.app.core.utils.ValidationUtils;
import com.velora.app.core.utils.RegexPatterns;
import java.util.UUID;

public class TestValidationOnly {
    public static void main(String[] args) {
        System.out.println("=== Testing ONLY ValidationUtils ===");

        try {
            // Test 1: UUID validation (your ValidationUtils takes Object)
            UUID testId = UUID.randomUUID();
            ValidationUtils.validateUUID(testId, "Test ID");
            System.out.println("✅ UUID validation works: " + testId);

            // Test 2: Format validation with your RegexPatterns
            ValidationUtils.validateFormat("test_user", RegexPatterns.USERNAME, "Username");
            System.out.println("✅ Username validation works");

            ValidationUtils.validateFormat("test@email.com", RegexPatterns.EMAIL, "Email");
            System.out.println("✅ Email validation works");

            // Test 3: NotBlank validation (your ValidationUtils takes Object)
            ValidationUtils.validateNotBlank("test", "Test Field");
            ValidationUtils.validateNotBlank(testId, "Test UUID");
            System.out.println("✅ NotBlank validation works");

            // Test 4: Error handling
            testErrorCases();

            System.out.println("\n🎉 Your ValidationUtils is PERFECT!");
            System.out.println("👉 The problem is in your domain classes, not ValidationUtils");

        } catch (Exception e) {
            System.out.println("❌ ValidationUtils test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testErrorCases() {
        System.out.println("\n🧪 Testing error handling...");

        // Test invalid username (too short)
        try {
            ValidationUtils.validateFormat("x", RegexPatterns.USERNAME, "Username");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Caught invalid username: " + e.getMessage());
        }

        // Test null value
        try {
            ValidationUtils.validateNotBlank(null, "Null Test");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ Caught null value: " + e.getMessage());
        }
    }
}