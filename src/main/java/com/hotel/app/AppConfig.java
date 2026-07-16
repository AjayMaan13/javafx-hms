package com.hotel.app;

import com.hotel.repository.AddonRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.service.PricingService;
import com.hotel.service.ReservationService;
import com.hotel.service.pricing.PricingStrategy;
import com.hotel.service.pricing.StandardPricingStrategy;
import com.hotel.util.DataSeeder;

public class AppConfig {

    // Swap this one line to change the pricing algorithm for the whole app.
    private static final PricingStrategy DEFAULT_PRICING_STRATEGY = new StandardPricingStrategy();

    private final GuestRepository guestRepository = new GuestRepository();
    private final RoomRepository roomRepository = new RoomRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final AddonRepository addonRepository = new AddonRepository();

    private final PricingService pricingService = new PricingService(DEFAULT_PRICING_STRATEGY);
    private final ReservationService reservationService = new ReservationService(
            guestRepository, roomRepository, reservationRepository, addonRepository, pricingService);

    private final DataSeeder dataSeeder = new DataSeeder(roomRepository, addonRepository);

    public void seedData() {
        dataSeeder.seedIfEmpty();
    }

    public PricingService getPricingService() {
        return pricingService;
    }

    public ReservationService getReservationService() {
        return reservationService;
    }

    public AddonRepository getAddonRepository() {
        return addonRepository;
    }
}
