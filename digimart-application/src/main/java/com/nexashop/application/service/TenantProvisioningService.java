package com.nexashop.application.service;

import com.nexashop.application.port.out.PlatformConfigRepository;
import com.nexashop.application.port.out.SubscriptionHistoryRepository;
import com.nexashop.application.port.out.SubscriptionPlanRepository;
import com.nexashop.application.port.out.TenantSubscriptionRepository;
import com.nexashop.application.port.out.TenantWalletRepository;
import com.nexashop.application.port.out.WalletTransactionRepository;
import com.nexashop.domain.billing.entity.PlatformConfig;
import com.nexashop.domain.billing.entity.SubscriptionHistory;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import com.nexashop.domain.billing.entity.TenantSubscription;
import com.nexashop.domain.billing.entity.TenantWallet;
import com.nexashop.domain.billing.entity.WalletTransaction;
import com.nexashop.domain.billing.enums.BillingCycle;
import com.nexashop.domain.billing.enums.SubscriptionAction;
import com.nexashop.domain.billing.enums.SubscriptionStatus;
import com.nexashop.domain.billing.enums.WalletStatus;
import com.nexashop.domain.billing.enums.WalletTxnType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TenantProvisioningService {

    private final PlatformConfigRepository configRepository;
    private final SubscriptionPlanRepository planRepository;
    private final TenantWalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository historyRepository;

    public TenantProvisioningService(
            PlatformConfigRepository configRepository,
            SubscriptionPlanRepository planRepository,
            TenantWalletRepository walletRepository,
            WalletTransactionRepository transactionRepository,
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionHistoryRepository historyRepository
    ) {
        this.configRepository = configRepository;
        this.planRepository = planRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void provisionTenant(Long tenantId) {
        createWalletIfNeeded(tenantId);
        activateStandardSubscription(tenantId);
    }

    @Transactional
    public void ensureSubscription(Long tenantId) {
        if (subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE).isPresent()) {
            return;
        }
        activateStandardSubscription(tenantId);
    }

    private void createWalletIfNeeded(Long tenantId) {
        if (walletRepository.existsByTenantId(tenantId)) {
            return;
        }
        BigDecimal initial = getDecimalConfig("INITIAL_WALLET_BALANCE", BigDecimal.ZERO);
        String currency = getStringConfig("DEFAULT_CURRENCY", "TND");
        TenantWallet wallet = new TenantWallet();
        wallet.setTenantId(tenantId);
        wallet.setBalance(initial);
        wallet.setCurrency(currency);
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setLastTransactionAt(LocalDateTime.now());
        wallet = walletRepository.save(wallet);

        WalletTransaction txn = new WalletTransaction();
        txn.setTenantId(tenantId);
        txn.setWalletId(wallet.getId());
        txn.setType(WalletTxnType.INITIAL_CREDIT);
        txn.setAmount(initial);
        txn.setBalanceBefore(BigDecimal.ZERO);
        txn.setBalanceAfter(initial);
        txn.setReason("Initial credit for new tenant");
        txn.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(txn);
    }

    private void activateStandardSubscription(Long tenantId) {
        subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .ifPresent(active -> {
                    active.setStatus(SubscriptionStatus.EXPIRED);
                    subscriptionRepository.save(active);
                });

        SubscriptionPlan standard = planRepository.findByCode("STANDARD")
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Standard plan not found"));

        TenantSubscription sub = new TenantSubscription();
        sub.setTenantId(tenantId);
        sub.setPlanId(standard.getId());
        sub.setStatus(SubscriptionStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        sub.setStartDate(now);
        sub.setEndDate(calculateEndDate(now, standard.getBillingCycle()));
        sub.setNextBillingDate(sub.getEndDate());
        sub.setPricePaid(standard.getPrice());
        TenantSubscription saved = subscriptionRepository.save(sub);

        SubscriptionHistory history = new SubscriptionHistory();
        history.setTenantId(tenantId);
        history.setSubscriptionId(saved.getId());
        history.setOldPlanId(null);
        history.setNewPlanId(standard.getId());
        history.setAction(SubscriptionAction.CREATED);
        history.setPerformedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private LocalDateTime calculateEndDate(LocalDateTime start, BillingCycle cycle) {
        if (cycle == null) return null;
        return switch (cycle) {
            case MONTHLY -> start.plusDays(30);
            case QUARTERLY -> start.plusDays(90);
            case YEARLY -> start.plusDays(365);
            case ONE_TIME -> null;
        };
    }

    private BigDecimal getDecimalConfig(String key, BigDecimal defaultValue) {
        return configRepository.findByConfigKey(key)
                .map(PlatformConfig::getConfigValue)
                .map(BigDecimal::new)
                .orElse(defaultValue);
    }

    private String getStringConfig(String key, String defaultValue) {
        return configRepository.findByConfigKey(key)
                .map(PlatformConfig::getConfigValue)
                .orElse(defaultValue);
    }
}
