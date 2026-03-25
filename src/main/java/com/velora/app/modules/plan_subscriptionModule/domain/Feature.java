package com.velora.app.modules.plan_subscriptionModule.domain;

import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Defines a platform feature that can be assigned to plans.
 */
public class Feature {

    private UUID featureId;
    private String featureKey;
    private TargetType targetType;
    private String description;

    /**
     * Creates a feature with mandatory fields. featureId is generated.
     */
    public Feature(String featureKey, TargetType targetType, String description) {
        setFeatureId(UUID.randomUUID());
        setFeatureKey(featureKey);
        setTargetType(targetType);
        setDescription(description);
    }

    public UUID getFeatureId() {
        return featureId;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public String getDescription() {
        return description;
    }

    private void setFeatureId(UUID featureId) {
        ValidationUtils.validateUUID(featureId, "featureId");
        this.featureId = featureId;
    }

    public void setFeatureKey(String featureKey) {
        ValidationUtils.validateIdentifierKey(featureKey, "featureKey");
        this.featureKey = featureKey;
    }

    public void setTargetType(TargetType targetType) {
        ValidationUtils.validateNotBlank(targetType, "targetType");
        this.targetType = targetType;
    }

    public void setDescription(String description) {
        if (description == null) {
            this.description = null;
            return;
        }
        ValidationUtils.validateNotBlank(description, "description");
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Feature)) {
            return false;
        }
        Feature that = (Feature) o;
        return Objects.equals(featureId, that.featureId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureId);
    }

    @Override
    public String toString() {
        return "Feature{" +
                "featureId=" + featureId +
                ", featureKey='" + featureKey + '\'' +
                ", targetType=" + targetType +
                '}';
    }
}
