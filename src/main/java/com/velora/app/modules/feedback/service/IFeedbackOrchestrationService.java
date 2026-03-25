package com.velora.app.modules.feedback.service;

import com.velora.app.core.domain.auth.Role;
import com.velora.app.modules.feedback.domain.FeatureSuggestion;
import com.velora.app.modules.feedback.domain.SuggestionCategory;
import com.velora.app.modules.feedback.domain.SuggestionStatus;

import java.util.List;
import java.util.UUID;

/**
 * Application-layer contract for feature suggestion and feedback management.
 *
 * <p>Requirement: 16.1, 16.9
 */
public interface IFeedbackOrchestrationService {

    /**
     * Submits a new feature suggestion from a user.
     */
    FeatureSuggestion submitSuggestion(UUID userId, SuggestionCategory category,
            String problemText, String solutionText);

    /**
     * Edits an existing suggestion. Only the owner can edit while status is NEW.
     */
    FeatureSuggestion editSuggestion(UUID actorUserId, UUID suggestionId, SuggestionCategory category,
            String problemText, String solutionText);

    /**
     * Updates the status of a suggestion. Requires admin role.
     */
    FeatureSuggestion updateStatus(UUID suggestionId, SuggestionStatus newStatus,
            String adminNotes, Role.RoleName actorRole);

    /**
     * Lists all suggestions submitted by the given user.
     */
    List<FeatureSuggestion> listMySuggestions(UUID userId);

    /**
     * Lists all suggestions filtered by status. Requires admin role.
     */
    List<FeatureSuggestion> adminListByStatus(SuggestionStatus status, Role.RoleName actorRole);
}
