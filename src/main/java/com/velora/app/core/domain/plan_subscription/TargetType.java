package com.velora.app.core.domain.plan_subscription;

/**
 * Identifies whether an entity/feature targets a USER, SHOP, or BOTH.
 */
public enum TargetType {
    USER,
    SHOP,
    BOTH;

    /**
     * Returns true if this target applies to the given target.
     */
    public boolean supports(TargetType targetType) {
        if (targetType == null) {
            return false;
        }
        if (this == BOTH) {
            return true;
        }
        return this == targetType;
    }
}
