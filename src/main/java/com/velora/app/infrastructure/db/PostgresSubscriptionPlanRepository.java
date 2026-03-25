package com.velora.app.infrastructure.db;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.velora.app.modules.plan_subscriptionModule.domain.SubscriptionPlan;
import com.velora.app.modules.plan_subscriptionModule.domain.SubscriptionPlanRepository;

/**
 * PostgreSQL implementation of SubscriptionPlanRepository.
 * Requirements: 14.2
 */
public class PostgresSubscriptionPlanRepository implements SubscriptionPlanRepository {

    @Override
    public SubscriptionPlan save(SubscriptionPlan plan) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<SubscriptionPlan> findById(UUID planId) {
        // TODO: implement JDBC select by id
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<SubscriptionPlan> findBySlug(String slug) {
        // TODO: implement JDBC select by slug
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<SubscriptionPlan> findAllActive() {
        // TODO: implement JDBC select where status = ACTIVE
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
