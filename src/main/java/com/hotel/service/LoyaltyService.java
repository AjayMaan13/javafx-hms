package com.hotel.service;

import com.hotel.model.Billing;
import com.hotel.model.Guest;
import com.hotel.model.LoyaltyAccount;
import com.hotel.model.LoyaltyConfig;
import com.hotel.model.LoyaltyTransaction;
import com.hotel.model.enums.LoyaltyTransactionType;
import com.hotel.repository.BillingRepository;
import com.hotel.repository.LoyaltyAccountRepository;
import com.hotel.repository.LoyaltyConfigRepository;
import com.hotel.repository.LoyaltyTransactionRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LoyaltyService {

    private static final double EPSILON = 0.005;

    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyConfigRepository configRepository;
    private final LoyaltyTransactionRepository transactionRepository;
    private final BillingRepository billingRepository;

    public LoyaltyService(LoyaltyAccountRepository accountRepository, LoyaltyConfigRepository configRepository,
                           LoyaltyTransactionRepository transactionRepository, BillingRepository billingRepository) {
        this.accountRepository = accountRepository;
        this.configRepository = configRepository;
        this.transactionRepository = transactionRepository;
        this.billingRepository = billingRepository;
    }

    public Optional<LoyaltyAccount> findAccount(Guest guest) {
        return accountRepository.findByGuestId(guest.getId());
    }

    /** Enrols a guest, or returns their existing account if already enrolled (idempotent). */
    public LoyaltyAccount enroll(Guest guest) {
        return accountRepository.findByGuestId(guest.getId())
                .orElseGet(() -> accountRepository.createFor(guest.getId(), "STANDARD", "ACTIVE"));
    }

    public List<LoyaltyTransaction> history(LoyaltyAccount account) {
        return transactionRepository.findByAccountId(account.getId());
    }

    /**
     * Awards points for a paid amount at the active config's earn rate. Returns the points
     * earned (0 if the amount rounds to nothing). Refunds (non-positive amounts) don't earn.
     */
    public int earn(LoyaltyAccount account, double paidAmount, UUID paymentId) {
        if (paidAmount <= 0) {
            return 0;
        }
        LoyaltyConfig config = activeConfig();
        int points = (int) Math.round(paidAmount * config.getEarnRate());
        if (points <= 0) {
            return 0;
        }
        transactionRepository.recordAndUpdateBalance(account.getId(), config.getId(), null, paymentId, null,
                LoyaltyTransactionType.EARN, points, paidAmount, 0.0, 0.0);
        return points;
    }

    /**
     * Redeems points for a bill discount, enforcing both the point balance and the
     * configured redemption cap (max share of the bill). Records a REDEEM transaction and
     * lowers the bill's balance. Returns the discount applied.
     */
    public double redeem(LoyaltyAccount account, int pointsRequested, Billing billing, UUID adminUserId) {
        if (pointsRequested <= 0) {
            throw new LoyaltyException("Enter a positive number of points to redeem.");
        }
        if (pointsRequested > account.getPointsBalance()) {
            throw new LoyaltyException("Not enough points — balance is " + account.getPointsBalance() + ".");
        }

        LoyaltyConfig config = activeConfig();
        double requestedDiscount = pointsRequested * config.getRedeemRate();
        double capAmount = config.getRedemptionCapPercent() * billing.getTotalDue();

        if (requestedDiscount > capAmount + EPSILON) {
            throw new LoyaltyException(String.format(
                    "Redemption is capped at %.0f%% of the bill ($%.2f). Redeem fewer points.",
                    config.getRedemptionCapPercent() * 100, capAmount));
        }

        transactionRepository.recordAndUpdateBalance(account.getId(), config.getId(), billing.getId(), null,
                adminUserId, LoyaltyTransactionType.REDEEM, -pointsRequested, 0.0, requestedDiscount, capAmount);
        billingRepository.applyLoyaltyDiscount(billing.getId(), requestedDiscount, pointsRequested);
        return requestedDiscount;
    }

    private LoyaltyConfig activeConfig() {
        return configRepository.findActive()
                .orElseThrow(() -> new LoyaltyException("No active loyalty configuration is set up."));
    }
}
