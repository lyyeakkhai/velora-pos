package com.velora.app.common;

import java.math.BigDecimal;

/**
 * Abstract base for all discount calculation strategies.
 * Subclasses implement the specific discount formula via {@link #apply(BigDecimal, BigDecimal)}.
 * Profit margin protection is enforced at this level via {@link #validateProfitMargin(BigDecimal, BigDecimal)}.
 */
public abstract class AbstractDiscountCalculator {

    /**
     * Apply the discount to the base price.
     *
     * @param basePrice     the original price before discount
     * @param discountValue the discount amount or percentage (interpretation is subclass-specific)
     * @return the final price after applying the discount
     */
    public abstract BigDecimal apply(BigDecimal basePrice, BigDecimal discountValue);

    /**
     * Validates that the final price is strictly greater than the cost price.
     * Throws {@link DomainException} if {@code finalPrice <= costPrice}.
     *
     * @param finalPrice the price after discount
     * @param costPrice  the cost/floor price that must not be reached or undercut
     */
    public void validateProfitMargin(BigDecimal finalPrice, BigDecimal costPrice) {
        if (finalPrice.compareTo(costPrice) <= 0) {
            throw new DomainException("Final price must be strictly greater than cost price");
        }
    }
}
