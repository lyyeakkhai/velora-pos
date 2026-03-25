package com.velora.app.modules.feedback.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeatureSuggestionRepository {
    void save(FeatureSuggestion suggestion);

    Optional<FeatureSuggestion> findById(UUID suggestionId);

    List<FeatureSuggestion> findByUserId(UUID userId);

    List<FeatureSuggestion> findByStatus(SuggestionStatus status);
}
