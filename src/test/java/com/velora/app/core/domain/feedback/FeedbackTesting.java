package com.velora.app.modules.feedback.domain;

import com.velora.app.common.DomainException;
import com.velora.app.modules.authModule.domain.Role;
import com.velora.app.modules.feedback.domain.FeatureSuggestion;
import com.velora.app.modules.feedback.domain.FeatureSuggestionRepository;
import com.velora.app.modules.feedback.domain.FeedbackService;
import com.velora.app.modules.feedback.domain.SuggestionCategory;
import com.velora.app.modules.feedback.domain.SuggestionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;

import static org.junit.Assert.*;

public class FeedbackTesting {

    private static class InMemorySuggestionRepo implements FeatureSuggestionRepository {
        private final Map<UUID, FeatureSuggestion> byId = new HashMap<>();

        @Override
        public void save(FeatureSuggestion suggestion) {
            byId.put(suggestion.getId(), suggestion);
        }

        @Override
        public Optional<FeatureSuggestion> findById(UUID suggestionId) {
            return Optional.ofNullable(byId.get(suggestionId));
        }

        @Override
        public List<FeatureSuggestion> findByUserId(UUID userId) {
            List<FeatureSuggestion> out = new ArrayList<>();
            for (FeatureSuggestion suggestion : byId.values()) {
                if (suggestion.getUserId().equals(userId)) {
                    out.add(suggestion);
                }
            }
            return out;
        }

        @Override
        public List<FeatureSuggestion> findByStatus(SuggestionStatus status) {
            List<FeatureSuggestion> out = new ArrayList<>();
            for (FeatureSuggestion suggestion : byId.values()) {
                if (suggestion.getStatus() == status) {
                    out.add(suggestion);
                }
            }
            return out;
        }
    }

    @Test
    public void submitSuggestion_defaultsToNew_andSetsCreatedAt() {
        InMemorySuggestionRepo repo = new InMemorySuggestionRepo();
        Clock fixed = Clock.fixed(Instant.parse("2026-03-01T00:00:00Z"), ZoneOffset.UTC);
        FeedbackService service = new FeedbackService(repo);
        UUID userId = UUID.randomUUID();

        FeatureSuggestion suggestion = service.submitSuggestion(userId, SuggestionCategory.UI, "Problem", "Solution");

        assertEquals(SuggestionStatus.NEW, suggestion.getStatus());
        assertNotNull(suggestion.getCreatedAt());
        assertEquals(userId, suggestion.getUserId());
        assertEquals(1, repo.findByUserId(userId).size());
    }

    @Test
    public void editSuggestion_onlyOwner_andOnlyWhenNew() {
        InMemorySuggestionRepo repo = new InMemorySuggestionRepo();
        FeedbackService service = new FeedbackService(repo);
        UUID userId = UUID.randomUUID();
        FeatureSuggestion suggestion = service.submitSuggestion(userId, SuggestionCategory.INVENTORY, "P", null);

        FeatureSuggestion edited = service.editSuggestion(userId, suggestion.getId(), SuggestionCategory.STAFF,
                "P2", "S2");
        assertEquals(SuggestionCategory.STAFF, edited.getCategory());
        assertEquals("P2", edited.getProblemText());

        service.adminUpdateStatus(suggestion.getId(), SuggestionStatus.IN_REVIEW, "note", Role.RoleName.SUPER_ADMIN);

        try {
            service.editSuggestion(userId, suggestion.getId(), SuggestionCategory.UI, "P3", null);
            fail("Expected exception");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("only be edited"));
        }

        try {
            service.editSuggestion(UUID.randomUUID(), suggestion.getId(), SuggestionCategory.UI, "P3", null);
            fail("Expected exception");
        } catch (DomainException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("owner"));
        }
    }

    @Test
    public void adminUpdateStatus_requiresSuperAdmin_andPreventsShippedRevert() {
        InMemorySuggestionRepo repo = new InMemorySuggestionRepo();
        FeedbackService service = new FeedbackService(repo);
        UUID userId = UUID.randomUUID();
        FeatureSuggestion suggestion = service.submitSuggestion(userId, SuggestionCategory.FINANCE, "P", null);

        try {
            service.adminUpdateStatus(suggestion.getId(), SuggestionStatus.IN_REVIEW, null, Role.RoleName.OWNER);
            fail("Expected exception");
        } catch (DomainException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("super_admin"));
        }

        service.adminUpdateStatus(suggestion.getId(), SuggestionStatus.IN_REVIEW, "n1", Role.RoleName.SUPER_ADMIN);
        service.adminUpdateStatus(suggestion.getId(), SuggestionStatus.SHIPPED, "done", Role.RoleName.SUPER_ADMIN);

        try {
            service.adminUpdateStatus(suggestion.getId(), SuggestionStatus.BACKLOG, "no", Role.RoleName.SUPER_ADMIN);
            fail("Expected exception");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("cannot revert"));
        }
    }

    @Test
    public void adminListByStatus_requiresSuperAdmin() {
        InMemorySuggestionRepo repo = new InMemorySuggestionRepo();
        FeedbackService service = new FeedbackService(repo);
        UUID userId = UUID.randomUUID();
        service.submitSuggestion(userId, SuggestionCategory.OTHER, "P", null);

        try {
            service.adminListByStatus(SuggestionStatus.NEW, Role.RoleName.MANAGER);
            fail("Expected exception");
        } catch (DomainException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("super_admin"));
        }

        assertEquals(1, service.adminListByStatus(SuggestionStatus.NEW, Role.RoleName.SUPER_ADMIN).size());
    }
}

