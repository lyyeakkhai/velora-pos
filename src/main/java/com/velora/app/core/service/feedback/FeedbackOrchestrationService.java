package com.velora.app.core.service.feedback;

import com.velora.app.common.AbstractDomainService;
import com.velora.app.core.domain.auth.Role;
import com.velora.app.core.domain.feedback.FeatureSuggestion;
import com.velora.app.core.domain.feedback.FeedbackService;
import com.velora.app.core.domain.feedback.SuggestionCategory;
import com.velora.app.core.domain.feedback.SuggestionStatus;
import com.velora.app.core.service.IFeedbackOrchestrationService;

import java.util.List;
import java.util.UUID;

/**
 * Application-layer service for feature suggestion and feedback management.
 *
 * <p>Extends {@link AbstractDomainService} to reuse {@code requireNotNull} guard methods.
 * Delegates domain logic to {@link FeedbackService}.
 *
 * <p>Requirements: 16.9
 */
public class FeedbackOrchestrationService extends AbstractDomainService
        implements IFeedbackOrchestrationService {

    private final FeedbackService feedbackService;

    public FeedbackOrchestrationService(FeedbackService feedbackService) {
        requireNotNull(feedbackService, "feedbackService");
        this.feedbackService = feedbackService;
    }

    /**
     * Submits a new feature suggestion from a user.
     */
    @Override
    public FeatureSuggestion submitSuggestion(UUID userId, SuggestionCategory category,
            String problemText, String solutionText) {
        requireNotNull(userId, "userId");
        requireNotNull(category, "category");
        requireNotNull(problemText, "problemText");

        return feedbackService.submitSuggestion(userId, category, problemText, solutionText);
    }

    /**
     * Edits an existing suggestion. Only the owner can edit while status is NEW.
     */
    @Override
    public FeatureSuggestion editSuggestion(UUID actorUserId, UUID suggestionId, SuggestionCategory category,
            String problemText, String solutionText) {
        requireNotNull(actorUserId, "actorUserId");
        requireNotNull(suggestionId, "suggestionId");
        requireNotNull(category, "category");
        requireNotNull(problemText, "problemText");

        return feedbackService.editSuggestion(actorUserId, suggestionId, category, problemText, solutionText);
    }

    /**
     * Updates the status of a suggestion. Requires admin role.
     */
    @Override
    public FeatureSuggestion updateStatus(UUID suggestionId, SuggestionStatus newStatus,
            String adminNotes, Role.RoleName actorRole) {
        requireNotNull(suggestionId, "suggestionId");
        requireNotNull(newStatus, "newStatus");
        requireNotNull(actorRole, "actorRole");

        return feedbackService.adminUpdateStatus(suggestionId, newStatus, adminNotes, actorRole);
    }

    /**
     * Lists all suggestions submitted by the given user.
     */
    @Override
    public List<FeatureSuggestion> listMySuggestions(UUID userId) {
        requireNotNull(userId, "userId");

        return feedbackService.listMySuggestions(userId);
    }

    /**
     * Lists all suggestions filtered by status. Requires admin role.
     */
    @Override
    public List<FeatureSuggestion> adminListByStatus(SuggestionStatus status, Role.RoleName actorRole) {
        requireNotNull(status, "status");
        requireNotNull(actorRole, "actorRole");

        return feedbackService.adminListByStatus(status, actorRole);
    }
}
