package com.velora.app.modules.authModule.domain;

import java.util.UUID;

public class AuthDomainTest {

    public static void main(String[] args) {
        System.out.println("=== Auth Domain Test ===");

        try {
            // FIX 1: Use the Enum Role.RoleName.OWNER instead of String "OWNER"
            UUID roleId = UUID.randomUUID();
            Role ownerRole = new Role(roleId, Role.RoleName.OWNER); 
            System.out.println("✅ Role created: " + ownerRole.getRoleName());

            // FIX 2: Match exactly 4 arguments for User
            UUID userId = UUID.randomUUID();
            User user = new User(userId, "john_doe", "https://example.com/profile.jpg", "Software developer");
            System.out.println("✅ User created: " + user.getUsername());

            // FIX 3: Match exactly 6 arguments for UserAuth 
            // Required: UUID, Provider, ProviderUid, Email, Password, UserID
            UUID authId = UUID.randomUUID();
            UserAuth userAuth = new UserAuth(
                authId, 
                UserAuth.Provider.EMAIL, 
                null, // provider_uid can be null for email
                "john@example.com", 
                "hashed_pass", 
                userId
            );
            System.out.println("✅ UserAuth created: " + userAuth.getEmail());

            // FIX 4: Validation test using the Enum
            try {
                new Role(null, Role.RoleName.SELLER);
            } catch (IllegalArgumentException e) {
                System.out.println("✅ Validation working: " + e.getMessage());
            }

            System.out.println("=== All tests passed! ===");

        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}