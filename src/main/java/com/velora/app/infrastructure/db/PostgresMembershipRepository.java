package com.velora.app.infrastructure.db;

import com.velora.app.modules.authModule.domain.Membership;
import com.velora.app.modules.authModule.Repository.MembershipRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL implementation of MembershipRepository.
 * Requirements: 14.1
 */
public class PostgresMembershipRepository implements MembershipRepository {

    @Override
    public Membership save(Membership membership) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Membership> findByUserId(UUID userId) {
        // TODO: implement JDBC select by userId
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Membership> findByShopId(UUID shopId) {
        // TODO: implement JDBC select by shopId
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Membership> findByUserAndShop(UUID userId, UUID shopId) {
        // TODO: implement JDBC select by userId and shopId
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<Membership> findById(UUID membershipId) {
        // TODO: implement JDBC select by id
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteById(UUID membershipId) {
        // TODO: implement JDBC delete
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
