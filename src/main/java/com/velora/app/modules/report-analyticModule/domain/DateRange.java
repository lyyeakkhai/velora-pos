package com.velora.app.core.domain.reportandanalytic;

import java.time.LocalDate;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Inclusive date range used for snapshot queries.
 */
public record DateRange(LocalDate startInclusive,LocalDate endInclusive){public DateRange{ValidationUtils.validateNotBlank(startInclusive,"startInclusive");ValidationUtils.validateNotBlank(endInclusive,"endInclusive");if(startInclusive.isAfter(endInclusive)){throw new IllegalArgumentException("startInclusive must be <= endInclusive");}}}
