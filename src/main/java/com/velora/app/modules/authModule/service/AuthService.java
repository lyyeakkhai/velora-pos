package com.velora.app.modules.authModule.service;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.modules.authModule.domain.Membership;
import com.velora.app.modules.authModule.Repository.MembershipRepository;
import com.velora.app.modules.authModule.domain.PasswordEncoder;
import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.modules.authModule.domain.User;
import com.velora.app.modules.authModule.domain.UserAuth;
import com.velora.app.modules.authModule.Repository.UserAuthRepository;
import com.velora.app.modules.authModule.Repository.UserRepository;
import com.velora.app.modules.authModule.exception.EmailAlreadyExistsException;
import com.velora.app.modules.authModule.exception.InvalidCredentialsException;
import com.velora.app.modules.authModule.exception.MembershipNotFoundException;
import com.velora.app.modules.authModule.exception.UnsupportedOAuthProviderException;
import com.velora.app.modules.authModule.exception.UserNotFoundException;
import com.velora.app.modules.authModule.exception.UsernameAlreadyTakenException;

import java.util.UUID;

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

    @Override
    public User registerUser(String username, String email, String rawPassword) {
        requireNotNull(username, "username");
        requireNotNull(email, "email");
        requireNotNull(rawPassword, "rawPassword");

        if (userAuthRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyTakenException(username);
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

    @Override
    public User registerOAuth(String username, String email, String provider, String providerUid) {
        requireNotNull(username, "username");
        requireNotNull(email, "email");
        requireNotNull(provider, "provider");
        requireNotNull(providerUid, "providerUid");

        if (userAuthRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyTakenException(username);
        }

        UserAuth.Provider authProvider;
        try {
            authProvider = UserAuth.Provider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedOAuthProviderException(provider);
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

    @Override
    public UserAuth login(String email, String rawPassword) {
        requireNotNull(email, "email");
        requireNotNull(rawPassword, "rawPassword");

        UserAuth userAuth = userAuthRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!userAuth.isEmailAuth()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(rawPassword, userAuth.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return userAuth;
    }

    @Override
    public Membership assignMembership(UUID actorId, UUID userId, UUID shopId, Role.RoleName role) {
        requireNotNull(actorId, "actorId");
        requireNotNull(userId, "userId");
        requireNotNull(shopId, "shopId");
        requireNotNull(role, "role");

        Role.RoleName actorRole = resolveActorRole(actorId);
        requireRole(actorRole, Role.RoleName.OWNER, Role.RoleName.SUPER_ADMIN);

        UUID roleId = UUID.randomUUID();
        Membership membership = new Membership(UUID.randomUUID(), userId, shopId, roleId);
        return membershipRepository.save(membership);
    }

    @Override
    public void revokeMembership(UUID actorId, UUID membershipId) {
        requireNotNull(actorId, "actorId");
        requireNotNull(membershipId, "membershipId");

        Role.RoleName actorRole = resolveActorRole(actorId);
        requireRole(actorRole, Role.RoleName.OWNER, Role.RoleName.SUPER_ADMIN);

        membershipRepository.findById(membershipId)
                .orElseThrow(() -> new MembershipNotFoundException(membershipId));

        membershipRepository.deleteById(membershipId);
    }

    @Override
    public User updateUserStatus(UUID actorId, UUID userId, User.Status newStatus) {
        requireNotNull(actorId, "actorId");
        requireNotNull(userId, "userId");
        requireNotNull(newStatus, "newStatus");

        Role.RoleName actorRole = resolveActorRole(actorId);
        requireRole(actorRole, Role.RoleName.SUPER_ADMIN);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        switch (newStatus) {
            case ACTIVE -> user.activate();
            case SUSPENDED -> user.suspend();
            case DELETED -> user.delete();
        }

        return userRepository.save(user);
    }

    private Role.RoleName resolveActorRole(UUID actorId) {
        return membershipRepository.findByUserId(actorId).stream()
                .map(m -> resolveRoleNameFromId(m.getRoleId()))
                .reduce(Role.RoleName.SELLER, AuthService::highestPrivilege);
    }

    private Role.RoleName resolveRoleNameFromId(UUID roleId) {
        return Role.RoleName.SELLER;
    }

    private static Role.RoleName highestPrivilege(Role.RoleName a, Role.RoleName b) {
        int[] order = { ordinal(a), ordinal(b) };
        return order[0] <= order[1] ? a : b;
    }

    private static int ordinal(Role.RoleName r) {
        return switch (r) {
            case SUPER_ADMIN -> 0;
            case OWNER -> 1;
            case MANAGER -> 2;
            case SELLER -> 3;
        };
    }
}
