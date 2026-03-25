package com.velora.app.infrastructure.db;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.velora.app.modules.feedback.domain.FeatureSuggestion;
import com.velora.app.modules.feedback.domain.FeatureSuggestionRepository;
import com.velora.app.modules.feedback.domain.SuggestionStatus;

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
