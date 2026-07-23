package com.hotel.service.billing;

/**
 * Component interface for the Decorator pattern (Final requirement: add-ons decorate the
 * booking price). A PricedBooking knows its running price and an itemised description.
 * {@link BaseBooking} is the concrete component (room-nights); each add-on is a decorator
 * that wraps a PricedBooking and layers on its own cost.
 */
public interface PricedBooking {

    double price();

    String description();

    /** Number of nights in the stay — decorators with a per-night cost multiply by this. */
    long nights();
}
