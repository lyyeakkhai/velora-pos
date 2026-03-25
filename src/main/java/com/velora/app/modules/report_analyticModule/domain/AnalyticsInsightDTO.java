package com.velora.app.modules.report_analyticModule.domain;

import java.math.BigDecimal;

import com.velora.app.core.utils.ValidationUtils;

/**
 * AI-ready insight computed strictly from snapshot data.
 */
public record AnalyticsInsightDTO(String metricName,BigDecimal previousValue,BigDecimal currentValue,BigDecimal changePercent,RiskLevel riskLevel,String recommendation){public AnalyticsInsightDTO{ValidationUtils.validateNotBlank(metricName,"metricName");AnalyticsMoney.normalizeSigned(previousValue,"previousValue");AnalyticsMoney.normalizeSigned(currentValue,"currentValue");AnalyticsMoney.normalizeSigned(changePercent,"changePercent");ValidationUtils.validateNotBlank(riskLevel,"riskLevel");ValidationUtils.validateNotBlank(recommendation,"recommendation");}}

