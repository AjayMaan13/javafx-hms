package com.hotel.config;

import com.hotel.model.enums.Role;

/**
 * Role-based discount caps (the brief: Admin up to 15%, Manager up to 30%). Centralised here
 * so the limits live in one place rather than as magic numbers scattered through services/UI.
 */
public class DiscountPolicy {

    private static final double ADMIN_MAX_PERCENT = 15.0;
    private static final double MANAGER_MAX_PERCENT = 30.0;

    /** Maximum discount percentage (0..100) this role may apply. */
    public double maxPercentFor(Role role) {
        switch (role) {
            case ADMIN:
                return ADMIN_MAX_PERCENT;
            case MANAGER:
                return MANAGER_MAX_PERCENT;
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}
