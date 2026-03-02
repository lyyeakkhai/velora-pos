package com.velora.app.core.domain.feedback;

import com.velora.app.core.utils.ValidationUtils;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Aggregate root representing a private product improvement suggestion.
 */
public class FeatureSuggestion {

    private final UUID suggestionId;
    private final UUID userId;
    private final LocalDateTime createdAt;

    private SuggestionCategory category;
    private String problemText;
    private String solutionText;

    private SuggestionStatus status;
    private String adminNotes;

    public FeatureSuggestion(UUID suggestionId, UUID userId, SuggestionCategory category, String problemText,
            String solutionText, Clock clock) {
        ValidationUtils.validateUUID(suggestionId, "suggestionId");
        ValidationUtils.validateUUID(userId, "userId");
        ValidationUtils.validateNotBlank(category, "category");
        ValidationUtils.validateNotBlank(problemText, "problemText");

        this.suggestionId = suggestionId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now(clock == null ? Clock.systemUTC() : clock);
        setCategory(category);
        setProblemText(problemText);
        setSolutionText(solutionText);
        this.status = SuggestionStatus.NEW;
    }

    public UUID getSuggestionId() {
        return suggestionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public SuggestionCategory getCategory() {
        return category;
    }

    public void setCategory(SuggestionCategory category) {
        ValidationUtils.validateNotBlank(category, "category");
        this.category = category;
    }

    public String getProblemText() {
        return problemText;
    }

    public void setProblemText(String problemText) {
        ValidationUtils.validateNotBlank(problemText, "problemText");
        this.problemText = problemText.trim();
    }

    public String getSolutionText() {
        return solutionText;
    }

    public void setSolutionText(String solutionText) {
        if (solutionText == null) {
            this.solutionText = null;
            return;
        }
        if (solutionText.isBlank()) {
            throw new IllegalArgumentException("solutionText cannot be blank");
        }
        this.solutionText = solutionText.trim();
    }

    public SuggestionStatus getStatus() {
        return status;
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

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
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
