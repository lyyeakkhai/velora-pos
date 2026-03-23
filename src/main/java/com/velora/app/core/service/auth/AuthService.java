package com.velora.app.core.service.auth;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.common.DomainException;
import com.velora.app.core.domain.auth.Membership;
import com.velora.app.core.domain.auth.MembershipRepository;
import com.velora.app.core.domain.auth.PasswordEncoder;
import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.domain.auth.User;
import com.velora.app.core.domain.auth.UserAuth;
import com.velora.app.core.domain.auth.UserAuthRepository;
import com.velora.app.core.domain.auth.UserRepository;
import com.velora.app.core.service.IAuthService;

import java.util.UUID;

/**
 * Application-layer service for authentication and identity management.
 *
 * <p>Extends {@link AbstractDomainService} to reuse {@code requireRole} and
 * {@code requireNotNull} guard methods.
 *
 * <p>Requirements: 16.1, 16.2
 */
public class AuthService extends AbstractDomainService implements IAuthService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            UserAuthRepository userAuthRepository,
            MembershipRepository membershipRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userAuthRepository = userAuthRepository;
        this.membershipRepository = membershipRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with email/password credentials.
     *
     * <p>Validates email uniqueness, hashes the raw password, then persists
     * a {@link User} and its associated {@link UserAuth}.
     *
     * @param username    the desired username
     * @param email       the user's email address
     * @param rawPassword the plain-text password to hash and store
     * @return the persisted {@link User}
     * @throws DomainException if the email is already registered
     */
    @Override
    public User registerUser(String username, String email, String rawPassword) {
        requireNotNull(username, "username");
        requireNotNull(email, "email");
        requireNotNull(rawPassword, "rawPassword");

        if (userAuthRepository.existsByEmail(email)) {
            throw new DomainException("Email is already registered: " + email);
        }
        if (userRepository.existsByUsername(username)) {
            throw new DomainException("Username is already taken: " + username);
        }

        User user = userRepository.save(new User(UUID.randomUUID(), username, null, null));

        String passwordHash = passwordEncoder.encode(rawPassword);
        userAuthRepository.save(new UserAuth(
                UUID.randomUUID(),
                UserAuth.Provider.EMAIL,
                null,
                email,
                passwordHash,
                user.getId()));

        return user;
    }

    /**
     * Registers a new user via an OAuth provider.
     *
     * <p>Validates email uniqueness, then persists a {@link User} and its
     * associated {@link UserAuth} with the given provider details.
     *
     * @param username    the desired username
     * @param email       the user's email address from the OAuth provider
     * @param provider    the OAuth provider name (e.g. "GOOGLE", "FACEBOOK")
     * @param providerUid the provider-specific user identifier
     * @return the persisted {@link User}
     * @throws DomainException if the email is already registered
     */
    @Override
    public User registerOAuth(String username, String email, String provider, String providerUid) {
        requireNotNull(username, "username");
        requireNotNull(email, "email");
        requireNotNull(provider, "provider");
        requireNotNull(providerUid, "providerUid");

        if (userAuthRepository.existsByEmail(email)) {
            throw new DomainException("Email is already registered: " + email);
        }
        if (userRepository.existsByUsername(username)) {
            throw new DomainException("Username is already taken: " + username);
        }

        UserAuth.Provider authProvider;
        try {
            authProvider = UserAuth.Provider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainException("Unsupported OAuth provider: " + provider);
        }

        User user = userRepository.save(new User(UUID.randomUUID(), username, null, null));

        userAuthRepository.save(new UserAuth(
                UUID.randomUUID(),
                authProvider,
                providerUid,
                email,
                null,
                user.getId()));

        return user;
    }

    /**
     * Authenticates a user by email and raw password.
     *
     * @param email       the user's email address
     * @param rawPassword the plain-text password to verify
     * @return the matching {@link UserAuth}
     * @throws DomainException if credentials are invalid
     */
    @Override
    public UserAuth login(String email, String rawPassword) {
        requireNotNull(email, "email");
        requireNotNull(rawPassword, "rawPassword");

        UserAuth userAuth = userAuthRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("Invalid credentials"));

        if (!userAuth.isEmailAuth()) {
            throw new DomainException("Account uses OAuth authentication — password login not allowed");
        }

        if (!passwordEncoder.matches(rawPassword, userAuth.getPasswordHash())) {
            throw new DomainException("Invalid credentials");
        }

        return userAuth;
    }

    /**
     * Assigns a shop membership role to a user.
     *
     * <p>Requires the actor to hold the {@code OWNER} or {@code SUPER_ADMIN} role.
     *
     * @param actorId the ID of the user performing the action
     * @param userId  the ID of the user to assign the membership to
     * @param shopId  the ID of the shop
     * @param role    the role to assign
     * @return the persisted {@link Membership}
     * @throws DomainException if the actor lacks the required role
     */
    @Override
    public Membership assignMembership(UUID actorId, UUID userId, UUID shopId, Role.RoleName role) {
        requireNotNull(actorId, "actorId");
        requireNotNull(userId, "userId");
        requireNotNull(shopId, "shopId");
        requireNotNull(role, "role");

        Role.RoleName actorRole = resolveActorRole(actorId);
        requireRole(actorRole, Role.RoleName.OWNER, Role.RoleName.SUPER_ADMIN);

        UUID roleId = UUID.randomUUID(); // Role entity ID — resolved by infrastructure in full impl
        Membership membership = new Membership(UUID.randomUUID(), userId, shopId, roleId);
        return membershipRepository.save(membership);
    }

    /**
     * Revokes a membership by its ID.
     *
     * <p>Requires the actor to hold the {@code OWNER} or {@code SUPER_ADMIN} role.
     *
     * @param actorId      the ID of the user performing the action
     * @param membershipId the ID of the membership to revoke
     * @throws DomainException if the actor lacks the required role or the
     *                         membership is not found
     */
    @Override
    public void revokeMembership(UUID actorId, UUID membershipId) {
        requireNotNull(actorId, "actorId");
        requireNotNull(membershipId, "membershipId");

        Role.RoleName actorRole = resolveActorRole(actorId);
        requireRole(actorRole, Role.RoleName.OWNER, Role.RoleName.SUPER_ADMIN);

        membershipRepository.findById(membershipId)
                .orElseThrow(() -> new DomainException("Membership not found: " + membershipId));

        membershipRepository.deleteById(membershipId);
    }

    /**
     * Updates a user's account status.
     *
     * <p>Requires the actor to hold the {@code SUPER_ADMIN} role.
     *
     * @param actorId   the ID of the admin performing the action
     * @param userId    the ID of the user to update
     * @param newStatus the new status to apply
     * @return the updated {@link User}
     * @throws DomainException if the actor is not a SUPER_ADMIN or the user is not found
     */
    @Override
    public User updateUserStatus(UUID actorId, UUID userId, User.Status newStatus) {
        requireNotNull(actorId, "actorId");
        requireNotNull(userId, "userId");
        requireNotNull(newStatus, "newStatus");

        Role.RoleName actorRole = resolveActorRole(actorId);
        requireRole(actorRole, Role.RoleName.SUPER_ADMIN);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found: " + userId));

        switch (newStatus) {
            case ACTIVE    -> user.activate();
            case SUSPENDED -> user.suspend();
            case DELETED   -> user.delete();
        }

        return userRepository.save(user);
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    /**
     * Resolves the highest-privilege role for the given actor by inspecting their
     * memberships. Returns {@code SELLER} as the default when no membership exists.
     *
     * <p>In a full implementation this would query a role repository; here it
     * derives the role from the actor's membership records.
     */
    private Role.RoleName resolveActorRole(UUID actorId) {
        return membershipRepository.findByUserId(actorId).stream()
                .map(m -> resolveRoleNameFromId(m.getRoleId()))
                .reduce(Role.RoleName.SELLER, AuthService::highestPrivilege);
    }

    /**
     * Placeholder — in a full implementation this would load the Role entity.
     * Returns {@code SELLER} until the Role repository is wired in.
     */
    private Role.RoleName resolveRoleNameFromId(UUID roleId) {
        // TODO: inject RoleRepository and look up by roleId
        return Role.RoleName.SELLER;
    }

    /** Returns the higher-privilege role of the two. */
    private static Role.RoleName highestPrivilege(Role.RoleName a, Role.RoleName b) {
        int[] order = { ordinal(a), ordinal(b) };
        return order[0] <= order[1] ? a : b;
    }

    private static int ordinal(Role.RoleName r) {
        return switch (r) {
            case SUPER_ADMIN -> 0;
            case OWNER       -> 1;
            case MANAGER     -> 2;
            case SELLER      -> 3;
        };
    }
}
