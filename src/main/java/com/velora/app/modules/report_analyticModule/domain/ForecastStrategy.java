package com.velora.app.modules.report_analyticModule.domain;

import java.util.List;

/**
 * Pluggable strategy for out-of-stock prediction algorithms.
 *
 * <p>Implementations define the forecasting algorithm used to predict
 * which product variants are at risk of running out of stock.
 * {@link ForecastService} accepts a strategy and delegates prediction to it,
 * keeping the service free of algorithm-specific branching logic.
 *
 * <p>Requirements: 23.1
 */
public interface ForecastStrategy {

    /**
     * Returns a human-readable name identifying this forecast algorithm.
     *
     * @return the forecast type name (e.g. "LINEAR_TREND", "MOVING_AVERAGE")
     */
    String getForecastType();

    /**
     * Predicts out-of-stock risk for each variant in the given snapshots.
     *
     * @param snapshots the historical daily product snapshots to analyse
     * @return a list of predictions, one per variant; empty if snapshots is empty
     */
    List<OutOfStockPredictionDTO> predict(List<DailyProductSnapshot> snapshots);
}

