package com.velora.app.infrastructure.ui;

import com.velora.app.core.service.SaleService;

public class ConsoleUI {
    private final SaleService saleService;

    public ConsoleUI(SaleService saleService) {
        this.saleService = saleService;
    }

    public void launch() {
        System.out.println("Velora Console UI launched (placeholder)");
    }
}
