package com.velora.app.core.domain.reportandanalytic;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Local monetary normalization for analytics.
 * <p>
 * Note: Profits and percent changes may legitimately be negative.
 */
public final class AnalyticsMoney {

    private AnalyticsMoney() {
        // utility
    }

    /**
     * Normalizes to scale=2 HALF_UP and requires amount >= 0.
     */
    public static BigDecimal normalizeNonNegative(BigDecimal amount, String fieldName) {
        ValidationUtils.validateNotBlank(amount, fieldName);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be >= 0");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Normalizes to scale=2 HALF_UP and allows negative values.
     */
    public static BigDecimal normalizeSigned(BigDecimal amount, String fieldName) {
        ValidationUtils.validateNotBlank(amount, fieldName);
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
