package com.velora.app.modules.store_managementModule.domain;

import com.velora.app.core.utils.ValidationUtils;
import java.math.BigDecimal;

public class ShopSettings {

    private BigDecimal platformFeeRatePercent;

    public ShopSettings(BigDecimal platformFeeRatePercent) {
        setPlatformFeeRatePercent(platformFeeRatePercent);
    }

    public static ShopSettings defaultSettings() {
        return new ShopSettings(BigDecimal.ZERO);
    }

    public BigDecimal getPlatformFeeRatePercent() {
        return platformFeeRatePercent;
    }

    public void setPlatformFeeRatePercent(BigDecimal platformFeeRatePercent) {
        if (platformFeeRatePercent == null) {
            platformFeeRatePercent = BigDecimal.ZERO;
        }
        this.platformFeeRatePercent = ValidationUtils.normalizePercentage(platformFeeRatePercent,
                "platformFeeRatePercent");
    }
}
