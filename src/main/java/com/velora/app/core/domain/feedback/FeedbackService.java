package com.velora.app.core.domain.feedback;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.utils.ValidationUtils;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

/**
 * Domain service for private feedback/suggestions.
 */
public class FeedbackService {

    private final FeatureSuggestionRepository repository;
    private final Clock clock;

    public FeedbackService(FeatureSuggestionRepository repository, Clock clock) {
        ValidationUtils.validateNotBlank(repository, "repository");
        this.repository = repository;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public FeatureSuggestion submitSuggestion(UUID userId, SuggestionCategory category, String problemText,
            String solutionText) {
        ValidationUtils.validateUUID(userId, "userId");

        FeatureSuggestion suggestion = new FeatureSuggestion(UUID.randomUUID(), userId, category, problemText,
                solutionText, clock);
        repository.save(suggestion);
        return suggestion;
    }

    /**
     * Owner can edit while status is NEW.
     */
    public FeatureSuggestion editSuggestion(UUID actorUserId, UUID suggestionId, SuggestionCategory category,
            String problemText, String solutionText) {
        ValidationUtils.validateUUID(actorUserId, "actorUserId");
        ValidationUtils.validateUUID(suggestionId, "suggestionId");

        FeatureSuggestion suggestion = repository.findById(suggestionId)
                .orElseThrow(() -> new IllegalStateException("suggestion not found"));

        FeedbackAccessPolicy.requireOwner(actorUserId, suggestion.getUserId());
        if (suggestion.getStatus() != SuggestionStatus.NEW) {
            throw new IllegalStateException("Suggestion can only be edited while NEW");
        }

        suggestion.setCategory(category);
        suggestion.setProblemText(problemText);
        suggestion.setSolutionText(solutionText);
        repository.save(suggestion);
        return suggestion;
    }

    public FeatureSuggestion adminUpdateStatus(UUID suggestionId, SuggestionStatus newStatus, String adminNotes,
            Role.RoleName actorRole) {
        ValidationUtils.validateUUID(suggestionId, "suggestionId");
        ValidationUtils.validateNotBlank(newStatus, "newStatus");
        FeedbackAccessPolicy.requireAdmin(actorRole);

        FeatureSuggestion suggestion = repository.findById(suggestionId)
                .orElseThrow(() -> new IllegalStateException("suggestion not found"));

        suggestion.transitionStatus(newStatus);
        if (adminNotes != null) {
            suggestion.setAdminNotes(adminNotes);
        }
        repository.save(suggestion);
        return suggestion;
    }

    public List<FeatureSuggestion> listMySuggestions(UUID userId) {
        ValidationUtils.validateUUID(userId, "userId");
        return repository.findByUserId(userId);
    }

    public List<FeatureSuggestion> adminListByStatus(SuggestionStatus status, Role.RoleName actorRole) {
        ValidationUtils.validateNotBlank(status, "status");
        FeedbackAccessPolicy.requireAdmin(actorRole);
        return repository.findByStatus(status);
    }
}
