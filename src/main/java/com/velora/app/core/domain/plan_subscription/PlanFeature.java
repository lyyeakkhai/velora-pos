package com.velora.app.core.domain.plan_subscription;

import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Bridge configuration between a subscription plan and a feature.
 */
public class PlanFeature {

    private UUID planId;
    private UUID featureId;
    private String featureKey;
    private Integer limitValue;
    private boolean enabled;

    /**
     * Creates a plan feature link with mandatory fields.
     */
    public PlanFeature(UUID planId, Feature feature, Integer limitValue, boolean enabled) {
        setPlanId(planId);
        setFeature(feature);
        setLimitValue(limitValue);
        setEnabled(enabled);
    }

    public UUID getPlanId() {
        return planId;
    }

    public UUID getFeatureId() {
        return featureId;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public Integer getLimitValue() {
        return limitValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void setPlanId(UUID planId) {
        ValidationUtils.validateUUID(planId, "planId");
        this.planId = planId;
    }

    private void setFeature(Feature feature) {
        ValidationUtils.validateNotBlank(feature, "feature");
        ValidationUtils.validateUUID(feature.getFeatureId(), "featureId");
        ValidationUtils.validateIdentifierKey(feature.getFeatureKey(), "featureKey");
        this.featureId = feature.getFeatureId();
        this.featureKey = feature.getFeatureKey();
    }

    public void setLimitValue(Integer limitValue) {
        if (limitValue == null) {
            this.limitValue = null;
            return;
        }
        ValidationUtils.validateNonNegativeInteger(limitValue, "limitValue");
        this.limitValue = limitValue;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlanFeature)) {
            return false;
        }
        PlanFeature that = (PlanFeature) o;
        return Objects.equals(planId, that.planId) && Objects.equals(featureId, that.featureId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, featureId);
    }

    @Override
    public String toString() {
        return "PlanFeature{" +
                "planId=" + planId +
                ", featureId=" + featureId +
                ", featureKey='" + featureKey + '\'' +
                ", limitValue=" + limitValue +
                ", enabled=" + enabled +
                '}';
    }
}
