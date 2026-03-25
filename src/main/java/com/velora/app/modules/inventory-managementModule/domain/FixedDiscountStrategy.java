package com.velora.app.core.domain.inventoryeventmanagement;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.velora.app.common.AbstractDiscountCalculator;
import com.velora.app.common.DomainException;

/**
 * Discount strategy that subtracts a fixed amount from the base price.
 * Formula: {@code basePrice - discountValue}.
 * Constraint: discountValue must be ≤ basePrice.
 */
public class FixedDiscountStrategy extends AbstractDiscountCalculator implements DiscountStrategy {

    @Override
    public DiscountType getType() {
        return DiscountType.FIXED;
    }

    @Override
    public BigDecimal apply(BigDecimal basePrice, BigDecimal discountValue) {
        return basePrice.subtract(discountValue).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Validates that discountValue is non-negative.
     * The basePrice check (discountValue ≤ basePrice) is enforced at call sites
     * via {@link #validateAgainstBasePrice(BigDecimal, BigDecimal)}.
     */
    @Override
    public void validate(BigDecimal discountValue) {
        if (discountValue == null) {
            throw new DomainException("discountValue must not be null");
        }
        if (discountValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Fixed discount value must not be negative, got: " + discountValue);
        }
    }

    /**
     * Additional validation that the fixed discount does not exceed the base price.
     *
     * @param discountValue the discount amount
     * @param basePrice     the price being discounted
     */
    public void validateAgainstBasePrice(BigDecimal discountValue, BigDecimal basePrice) {
        if (discountValue.compareTo(basePrice) > 0) {
            throw new DomainException(
                    "Fixed discount (" + discountValue + ") must not exceed base price (" + basePrice + ")");
        }
    }
}
