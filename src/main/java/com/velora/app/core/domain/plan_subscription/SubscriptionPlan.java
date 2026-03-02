package com.velora.app.core.domain.plan_subscription;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Defines a subscription plan with pricing, duration, and feature assignments.
 */
public class SubscriptionPlan {

    private UUID planId;
    private String name;
    private String slug;
    private BigDecimal price;
    private Integer durationMonths;
    private PayerType payerType;
    private boolean active;

    private final Map<String, PlanFeature> featuresByKey = new HashMap<>();

    /**
     * Creates a plan with mandatory fields. planId is generated.
     */
    public SubscriptionPlan(String name, String slug, BigDecimal price, Integer durationMonths, PayerType payerType,
            boolean active) {
        setPlanId(UUID.randomUUID());
        setName(name);
        setSlug(slug);
        setPrice(price);
        setDurationMonths(durationMonths);
        setPayerType(payerType);
        setActive(active);
    }

    public UUID getPlanId() {
        return planId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public PayerType getPayerType() {
        return payerType;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Returns true if the plan can be used for new subscriptions.
     */
    public boolean isAvailable() {
        return active;
    }

    /**
     * Disables this plan.
     */
    public void disable() {
        setActive(false);
    }

    /**
     * Enables this plan.
     */
    public void enable() {
        setActive(true);
    }

    public void setName(String name) {
        ValidationUtils.validateNotBlank(name, "name");
        this.name = name.toString().trim();
    }

    public void setSlug(String slug) {
        ValidationUtils.validateSlug(slug, "slug");
        this.slug = slug;
    }

    public void setPrice(BigDecimal price) {
        this.price = ValidationUtils.normalizeMoney(price, "price");
    }

    public void setDurationMonths(Integer durationMonths) {
        ValidationUtils.validatePositiveInteger(durationMonths, "durationMonths");
        this.durationMonths = durationMonths;
    }

    public void setPayerType(PayerType payerType) {
        ValidationUtils.validateNotBlank(payerType, "payerType");
        this.payerType = payerType;
    }

    private void setPlanId(UUID planId) {
        ValidationUtils.validateUUID(planId, "planId");
        this.planId = planId;
    }

    private void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Adds or replaces a feature assignment.
     */
    public void upsertFeature(Feature feature, Integer limitValue, boolean enabled) {
        ValidationUtils.validateNotBlank(feature, "feature");
        PlanFeature planFeature = new PlanFeature(getPlanId(), feature, limitValue, enabled);
        featuresByKey.put(planFeature.getFeatureKey(), planFeature);
    }

    /**
     * Returns true if this plan has the given feature enabled.
     */
    public boolean hasFeature(String featureKey) {
        ValidationUtils.validateIdentifierKey(featureKey, "featureKey");
        PlanFeature planFeature = featuresByKey.get(featureKey);
        return planFeature != null && planFeature.isEnabled();
    }

    /**
     * Returns the configured feature limit, or null if not configured.
     */
    public Integer getFeatureLimit(String featureKey) {
        ValidationUtils.validateIdentifierKey(featureKey, "featureKey");
        PlanFeature planFeature = featuresByKey.get(featureKey);
        return planFeature == null ? null : planFeature.getLimitValue();
    }

    public Map<String, PlanFeature> getFeaturesByKey() {
        return Collections.unmodifiableMap(featuresByKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubscriptionPlan)) {
            return false;
        }
        SubscriptionPlan that = (SubscriptionPlan) o;
        return Objects.equals(planId, that.planId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId);
    }

    @Override
    public String toString() {
        return "SubscriptionPlan{" +
                "planId=" + planId +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", price=" + price +
                ", durationMonths=" + durationMonths +
                ", payerType=" + payerType +
                ", active=" + active +
                '}';
    }
}
