package com.velora.app.modules.report_analyticModule.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.velora.app.core.utils.ValidationUtils;

/**
 * Read model of a confirmed order used for analytics aggregation.
 */
public record OrderFact(UUID orderId,UUID shopId,UUID orgId,LocalDateTime confirmedAtUtc){public OrderFact{ValidationUtils.validateUUID(orderId,"orderId");ValidationUtils.validateUUID(shopId,"shopId");ValidationUtils.validateUUID(orgId,"orgId");ValidationUtils.validateNotBlank(confirmedAtUtc,"confirmedAtUtc");}}

