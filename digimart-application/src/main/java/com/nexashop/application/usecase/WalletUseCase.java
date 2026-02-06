package com.nexashop.application.usecase;

import com.nexashop.application.exception.*;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.PlatformConfigRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.port.out.TenantWalletRepository;
import com.nexashop.application.port.out.WalletTransactionRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.billing.entity.PlatformConfig;
import com.nexashop.domain.billing.entity.TenantWallet;
import com.nexashop.domain.billing.entity.WalletTransaction;
import com.nexashop.domain.billing.enums.WalletStatus;
import com.nexashop.domain.billing.enums.WalletTxnType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


public class WalletUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final TenantWalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final PlatformConfigRepository configRepository;
    private final TenantRepository tenantRepository;

    public WalletUseCase(
            CurrentUserProvider currentUserProvider,
            TenantWalletRepository walletRepository,
            WalletTransactionRepository transactionRepository,
            PlatformConfigRepository configRepository,
            TenantRepository tenantRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.configRepository = configRepository;
        this.tenantRepository = tenantRepository;
    }

    public TenantWallet getWallet(Long tenantId) {
        CurrentUser actor = currentUserProvider.requireUser();
        return walletRepository.findByTenantId(tenantId)
                .orElseGet(() -> createWalletForTenant(tenantId, actor.userId()));
    }

    public List<WalletTransaction> listTransactions(Long tenantId) {
        currentUserProvider.requireUser();
        TenantWallet wallet = walletRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));
        return transactionRepository.findByWalletIdOrderByTransactionDateDesc(wallet.getId());
    }

    public TenantWallet credit(
            Long tenantId,
            BigDecimal amount,
            String reason,
            String reference
    ) {
        CurrentUser actor = currentUserProvider.requireUser();
        TenantWallet wallet = walletRepository.findByTenantId(tenantId)
                .orElseGet(() -> createWalletForTenant(tenantId, actor.userId()));
        applyAdjustment(wallet, amount, reason, reference, WalletTxnType.MANUAL_CREDIT, actor.userId());
        return walletRepository.save(wallet);
    }

    public TenantWallet debit(
            Long tenantId,
            BigDecimal amount,
            String reason,
            String reference
    ) {
        CurrentUser actor = currentUserProvider.requireUser();
        TenantWallet wallet = walletRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Wallet not found"));
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance");
        }
        applyAdjustment(wallet, amount, reason, reference, WalletTxnType.MANUAL_DEBIT, actor.userId());
        return walletRepository.save(wallet);
    }

    private TenantWallet createWalletForTenant(Long tenantId, Long actorUserId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new NotFoundException("Tenant not found");
        }
        BigDecimal initialBalance = getDecimalConfig("INITIAL_WALLET_BALANCE", BigDecimal.ZERO);
        TenantWallet wallet = new TenantWallet();
        wallet.setTenantId(tenantId);
        wallet.setBalance(initialBalance);
        wallet.setCurrency(getStringConfig("DEFAULT_CURRENCY", "TND"));
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setLastTransactionAt(LocalDateTime.now());
        wallet = walletRepository.save(wallet);
        createTransaction(wallet, WalletTxnType.INITIAL_CREDIT, initialBalance, "Initial credit for new tenant", null, actorUserId);
        return wallet;
    }

    private void applyAdjustment(
            TenantWallet wallet,
            BigDecimal amount,
            String reason,
            String reference,
            WalletTxnType type,
            Long actorUserId
    ) {
        BigDecimal before = wallet.getBalance();
        BigDecimal after = type == WalletTxnType.MANUAL_DEBIT
                ? before.subtract(amount)
                : before.add(amount);
        wallet.setBalance(after);
        wallet.setLastTransactionAt(LocalDateTime.now());
        walletRepository.save(wallet);
        createTransaction(wallet, type, amount, reason, reference, actorUserId);
    }

    private void createTransaction(
            TenantWallet wallet,
            WalletTxnType type,
            BigDecimal amount,
            String reason,
            String reference,
            Long actorUserId
    ) {
        WalletTransaction txn = new WalletTransaction();
        txn.setTenantId(wallet.getTenantId());
        txn.setWalletId(wallet.getId());
        txn.setType(type);
        txn.setAmount(amount);
        txn.setBalanceBefore(wallet.getBalance().add(type == WalletTxnType.MANUAL_DEBIT ? amount : amount.negate()));
        txn.setBalanceAfter(wallet.getBalance());
        txn.setReason(reason);
        txn.setReference(reference);
        txn.setProcessedBy(actorUserId);
        txn.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(txn);
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


