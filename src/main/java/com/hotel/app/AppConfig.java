package com.hotel.app;

import com.hotel.config.DiscountPolicy;
import com.hotel.config.PricingPolicy;
import com.hotel.events.RoomAvailabilityPublisher;
import com.hotel.events.WaitlistSubscriber;
import com.hotel.repository.AddonRepository;
import com.hotel.repository.AdminUserRepository;
import com.hotel.repository.AuditLogRepository;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.FeedbackRepository;
import com.hotel.repository.GuestRepository;
import com.hotel.repository.LoyaltyAccountRepository;
import com.hotel.repository.LoyaltyConfigRepository;
import com.hotel.repository.LoyaltyTransactionRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.ReservationRepository;
import com.hotel.repository.RoomRepository;
import com.hotel.repository.WaitlistRepository;
import com.hotel.security.AuthService;
import com.hotel.security.BCryptPasswordHasher;
import com.hotel.service.ActivityLogService;
import com.hotel.service.BillingService;
import com.hotel.service.DiscountService;
import com.hotel.service.FeedbackService;
import com.hotel.service.LoyaltyService;
import com.hotel.service.PricingService;
import com.hotel.service.ReportingService;
import com.hotel.service.ReservationService;
import com.hotel.service.pricing.PricingStrategy;
import com.hotel.service.pricing.StandardPricingStrategy;
import com.hotel.util.CsvExporter;
import com.hotel.util.DataSeeder;
import com.hotel.util.LoggerService;
import com.hotel.util.PdfExporter;
import com.hotel.util.TxtExporter;

/**
 * The single composition root. Nothing outside this class (and tests) should ever call
 * `new XxxService(...)` or `new XxxRepository(...)` — every screen reaches its
 * dependencies through AdminShellController.getAppConfig() / KioskShellController.getAppConfig(),
 * both of which are handed the one AppConfig instance built in Main/AdminMain.
 */
public class AppConfig {

    private final PricingPolicy pricingPolicy = new PricingPolicy();
    private final DiscountPolicy discountPolicy = new DiscountPolicy();

    // Swap this one line to change the pricing algorithm for the whole app.
    private final PricingStrategy defaultPricingStrategy = new StandardPricingStrategy();

    private final GuestRepository guestRepository = new GuestRepository();
    private final RoomRepository roomRepository = new RoomRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final AddonRepository addonRepository = new AddonRepository();
    private final AdminUserRepository adminUserRepository = new AdminUserRepository();
    private final AuditLogRepository auditLogRepository = new AuditLogRepository();
    private final BillingRepository billingRepository = new BillingRepository();
    private final PaymentRepository paymentRepository = new PaymentRepository();
    private final LoyaltyAccountRepository loyaltyAccountRepository = new LoyaltyAccountRepository();
    private final LoyaltyConfigRepository loyaltyConfigRepository = new LoyaltyConfigRepository();
    private final LoyaltyTransactionRepository loyaltyTransactionRepository = new LoyaltyTransactionRepository();
    private final WaitlistRepository waitlistRepository = new WaitlistRepository();
    private final FeedbackRepository feedbackRepository = new FeedbackRepository();

    private final BCryptPasswordHasher passwordHasher = new BCryptPasswordHasher();
    private final LoggerService loggerService = LoggerService.getInstance();

    // Observer: one publisher for the whole app, with the waitlist subscriber attached once
    // here. Nothing else needs to know WaitlistSubscriber exists — services just publish().
    private final RoomAvailabilityPublisher roomAvailabilityPublisher = new RoomAvailabilityPublisher();

    private final PricingService pricingService = new PricingService(defaultPricingStrategy, pricingPolicy);
    // LoyaltyService is built before BillingService because BillingService earns points on payment.
    private final LoyaltyService loyaltyService = new LoyaltyService(
            loyaltyAccountRepository, loyaltyConfigRepository, loyaltyTransactionRepository, billingRepository);
    private final BillingService billingService = new BillingService(
            billingRepository, paymentRepository, reservationRepository, roomRepository, loyaltyService,
            roomAvailabilityPublisher);
    private final ReservationService reservationService = new ReservationService(
            guestRepository, roomRepository, reservationRepository, addonRepository, pricingService, billingService,
            roomAvailabilityPublisher);
    private final AuthService authService = new AuthService(adminUserRepository, passwordHasher);
    private final ActivityLogService activityLogService = new ActivityLogService(auditLogRepository, loggerService);
    private final DiscountService discountService = new DiscountService(billingRepository, discountPolicy);
    private final FeedbackService feedbackService = new FeedbackService(
            reservationRepository, guestRepository, feedbackRepository, billingService);
    private final ReportingService reportingService = new ReportingService(
            reservationRepository, roomRepository, billingRepository, auditLogRepository);

    private final CsvExporter csvExporter = new CsvExporter();
    private final TxtExporter txtExporter = new TxtExporter();
    private final PdfExporter pdfExporter = new PdfExporter();

    private final DataSeeder dataSeeder = new DataSeeder(roomRepository, addonRepository, adminUserRepository,
            loyaltyConfigRepository, passwordHasher);

    public AppConfig() {
        roomAvailabilityPublisher.attach(new WaitlistSubscriber(waitlistRepository));
    }

    public void seedData() {
        dataSeeder.seedIfEmpty();
    }

    public PricingService getPricingService() {
        return pricingService;
    }

    public ReservationService getReservationService() {
        return reservationService;
    }

    public BillingService getBillingService() {
        return billingService;
    }

    public LoyaltyService getLoyaltyService() {
        return loyaltyService;
    }

    public DiscountService getDiscountService() {
        return discountService;
    }

    public FeedbackService getFeedbackService() {
        return feedbackService;
    }

    public ReportingService getReportingService() {
        return reportingService;
    }

    public CsvExporter getCsvExporter() {
        return csvExporter;
    }

    public TxtExporter getTxtExporter() {
        return txtExporter;
    }

    public PdfExporter getPdfExporter() {
        return pdfExporter;
    }

    public RoomAvailabilityPublisher getRoomAvailabilityPublisher() {
        return roomAvailabilityPublisher;
    }

    public GuestRepository getGuestRepository() {
        return guestRepository;
    }

    public RoomRepository getRoomRepository() {
        return roomRepository;
    }

    public ReservationRepository getReservationRepository() {
        return reservationRepository;
    }

    public PaymentRepository getPaymentRepository() {
        return paymentRepository;
    }

    public BillingRepository getBillingRepository() {
        return billingRepository;
    }

    public LoyaltyAccountRepository getLoyaltyAccountRepository() {
        return loyaltyAccountRepository;
    }

    public LoyaltyConfigRepository getLoyaltyConfigRepository() {
        return loyaltyConfigRepository;
    }

    public LoyaltyTransactionRepository getLoyaltyTransactionRepository() {
        return loyaltyTransactionRepository;
    }

    public AuditLogRepository getAuditLogRepository() {
        return auditLogRepository;
    }

    public WaitlistRepository getWaitlistRepository() {
        return waitlistRepository;
    }

    public FeedbackRepository getFeedbackRepository() {
        return feedbackRepository;
    }

    public AddonRepository getAddonRepository() {
        return addonRepository;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public AdminUserRepository getAdminUserRepository() {
        return adminUserRepository;
    }

    public ActivityLogService getActivityLogService() {
        return activityLogService;
    }

    public LoggerService getLoggerService() {
        return loggerService;
    }
}
