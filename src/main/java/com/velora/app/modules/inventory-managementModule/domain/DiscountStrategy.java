package com.velora.app.modules.inventory.domain;

import java.math.BigDecimal;

/**
 * Pluggable discount calculation strategy.
 * Each implementation handles a specific {@link DiscountType}.
 */
public interface DiscountStrategy {

    /**
     * Returns the discount type this strategy handles.
     */
    DiscountType getType();

    /**
     * Applies the discount to the base price and returns the final price.
     *
     * @param basePrice     the original price before discount
     * @param discountValue the discount amount or percentage
     * @return the final price after applying the discount
     */
    BigDecimal apply(BigDecimal basePrice, BigDecimal discountValue);

    /**
     * Validates that the discount value is within acceptable bounds.
     * Throws {@link com.velora.app.common.DomainException} if invalid.
     *
     * @param discountValue the discount value to validate
     */
    void validate(BigDecimal discountValue);
}

