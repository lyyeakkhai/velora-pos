package com.velora.app.infrastructure.db;

import com.velora.app.core.domain.feedback.FeatureSuggestion;
import com.velora.app.core.domain.feedback.FeatureSuggestionRepository;
import com.velora.app.core.domain.feedback.SuggestionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PostgreSQL implementation of FeatureSuggestionRepository.
 * Requirements: 14.8
 */
public class PostgresFeatureSuggestionRepository implements FeatureSuggestionRepository {

    @Override
    public void save(FeatureSuggestion suggestion) {
        // TODO: implement JDBC insert/update
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<FeatureSuggestion> findById(UUID suggestionId) {
        // TODO: implement JDBC select by id
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<FeatureSuggestion> findByUserId(UUID userId) {
        // TODO: implement JDBC select by userId
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<FeatureSuggestion> findByStatus(SuggestionStatus status) {
        // TODO: implement JDBC select by status
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
