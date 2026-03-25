package com.velora.app.modules.inventory.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.velora.app.common.AbstractDiscountCalculator;
import com.velora.app.common.DomainException;

/**
 * Discount strategy that applies a percentage reduction.
 * Formula: {@code basePrice * (1 - discountValue / 100)}, rounded HALF_UP to 2 decimal places.
 * Valid range: 0 ≤ discountValue ≤ 100.
 */
public class PercentageDiscountStrategy extends AbstractDiscountCalculator implements DiscountStrategy {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    public DiscountType getType() {
        return DiscountType.PERCENTAGE;
    }

    @Override
    public BigDecimal apply(BigDecimal basePrice, BigDecimal discountValue) {
        // basePrice * (1 - discountValue / 100)
        BigDecimal factor = BigDecimal.ONE.subtract(
                discountValue.divide(HUNDRED, 10, RoundingMode.HALF_UP));
        return basePrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void validate(BigDecimal discountValue) {
        if (discountValue == null) {
            throw new DomainException("discountValue must not be null");
        }
        if (discountValue.compareTo(BigDecimal.ZERO) < 0 || discountValue.compareTo(HUNDRED) > 0) {
            throw new DomainException("Percentage discount must be between 0 and 100 inclusive, got: " + discountValue);
        }
    }
}

