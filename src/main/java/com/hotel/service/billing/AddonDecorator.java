package com.hotel.service.billing;

/**
 * Abstract decorator: holds a wrapped PricedBooking and delegates {@link #nights()} to it,
 * so per-night add-on costs propagate down the chain. Concrete decorators override
 * {@link #price()} and {@link #description()} to add their own cost and label on top of
 * whatever they wrap.
 */
public abstract class AddonDecorator implements PricedBooking {

    protected final PricedBooking wrapped;

    protected AddonDecorator(PricedBooking wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public long nights() {
        return wrapped.nights();
    }
}
