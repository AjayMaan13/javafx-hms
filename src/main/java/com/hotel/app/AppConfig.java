package com.hotel.app;

import com.hotel.service.PricingService;
import com.hotel.service.pricing.PricingStrategy;
import com.hotel.service.pricing.StandardPricingStrategy;

public class AppConfig {

    // Swap this one line to change the pricing algorithm for the whole app.
    private static final PricingStrategy DEFAULT_PRICING_STRATEGY = new StandardPricingStrategy();

    private final PricingService pricingService;

    public AppConfig() {
        this.pricingService = new PricingService(DEFAULT_PRICING_STRATEGY);
    }

    public PricingService getPricingService() {
        return pricingService;
    }
}
