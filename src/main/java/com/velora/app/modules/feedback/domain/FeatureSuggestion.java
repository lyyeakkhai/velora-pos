package com.velora.app.modules.feedback.domain;

import com.velora.app.common.AbstractAuditableEntity;
import com.velora.app.core.utils.ValidationUtils;
import java.util.UUID;

/**
 * Aggregate root representing a private product improvement suggestion.
 * Extends AbstractAuditableEntity for id, createdAt, updatedAt, and touch().
 */
public class FeatureSuggestion extends AbstractAuditableEntity {

    private final UUID userId;

    private SuggestionCategory category;
    private String problemText;
    private String solutionText;

    private SuggestionStatus status;
    private String adminNotes;

    public FeatureSuggestion(UUID suggestionId, UUID userId, SuggestionCategory category,
            String problemText, String solutionText) {
        super(suggestionId);
        ValidationUtils.validateUUID(userId, "userId");
        ValidationUtils.validateNotBlank(category, "category");
        ValidationUtils.validateNotBlank(problemText, "problemText");

        this.userId = userId;
        setCategory(category);
        setProblemText(problemText);
        setSolutionText(solutionText);
        this.status = SuggestionStatus.NEW;
    }

    /** Convenience alias for getId(). */
    public UUID getSuggestionId() {
        return getId();
    }

    public UUID getUserId() {
        return userId;
    }

    public SuggestionCategory getCategory() {
        return category;
    }

    public String getProblemText() {
        return problemText;
    }

    public String getSolutionText() {
        return solutionText;
    }

    public SuggestionStatus getStatus() {
        return status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    /**
     * Edit the suggestion content. Calls touch() to record the modification timestamp.
     */
    public void edit(SuggestionCategory category, String problemText, String solutionText) {
        setCategory(category);
        setProblemText(problemText);
        setSolutionText(solutionText);
        touch();
    }

    /**
     * Update the suggestion status. Calls touch() to record the modification timestamp.
     */
    public void updateStatus(SuggestionStatus newStatus, String adminNotes) {
        transitionStatus(newStatus);
        setAdminNotes(adminNotes);
        touch();
    }

    public void transitionStatus(SuggestionStatus newStatus) {
        ValidationUtils.validateNotBlank(newStatus, "status");
        if (status == SuggestionStatus.SHIPPED && newStatus != SuggestionStatus.SHIPPED) {
            throw new IllegalStateException("SHIPPED suggestions cannot revert");
        }
        if (status == SuggestionStatus.NEW) {
            if (newStatus != SuggestionStatus.NEW && newStatus != SuggestionStatus.IN_REVIEW) {
                throw new IllegalStateException("Invalid transition from NEW");
            }
        }
        if (status == SuggestionStatus.IN_REVIEW) {
            if (newStatus == SuggestionStatus.NEW) {
                throw new IllegalStateException("Invalid transition from IN_REVIEW");
            }
        }
        if (status == SuggestionStatus.BACKLOG) {
            if (newStatus == SuggestionStatus.NEW) {
                throw new IllegalStateException("Invalid transition from BACKLOG");
            }
        }
        this.status = newStatus;
    }

    private void setCategory(SuggestionCategory category) {
        ValidationUtils.validateNotBlank(category, "category");
        this.category = category;
    }

    private void setProblemText(String problemText) {
        ValidationUtils.validateNotBlank(problemText, "problemText");
        this.problemText = problemText.trim();
    }

    private void setSolutionText(String solutionText) {
        if (solutionText == null) {
            this.solutionText = null;
            return;
        }
        if (solutionText.isBlank()) {
            throw new IllegalArgumentException("solutionText cannot be blank");
        }
        this.solutionText = solutionText.trim();
    }

    private void setAdminNotes(String adminNotes) {
        if (adminNotes == null) {
            this.adminNotes = null;
            return;
        }
        if (adminNotes.isBlank()) {
            throw new IllegalArgumentException("adminNotes cannot be blank");
        }
        this.adminNotes = adminNotes.trim();
    }
}
