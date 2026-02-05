package com.nexashop.api.controller.billing;

import com.nexashop.api.dto.request.billing.WalletAdjustmentRequest;
import com.nexashop.api.dto.response.billing.TenantWalletResponse;
import com.nexashop.api.dto.response.billing.WalletTransactionResponse;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.domain.billing.entity.PlatformConfig;
import com.nexashop.domain.billing.entity.TenantWallet;
import com.nexashop.domain.billing.entity.WalletTransaction;
import com.nexashop.domain.billing.enums.WalletStatus;
import com.nexashop.domain.billing.enums.WalletTxnType;
import com.nexashop.infrastructure.persistence.jpa.PlatformConfigJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.TenantWalletJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.WalletTransactionJpaRepository;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/tenants/{tenantId}/wallet")
public class WalletController {

    private final TenantWalletJpaRepository walletRepository;
    private final WalletTransactionJpaRepository transactionRepository;
    private final PlatformConfigJpaRepository configRepository;
    private final TenantJpaRepository tenantRepository;

    public WalletController(
            TenantWalletJpaRepository walletRepository,
            WalletTransactionJpaRepository transactionRepository,
            PlatformConfigJpaRepository configRepository,
            TenantJpaRepository tenantRepository
    ) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.configRepository = configRepository;
        this.tenantRepository = tenantRepository;
    }

    @GetMapping
    public TenantWalletResponse getWallet(@PathVariable Long tenantId) {
        // Dev-mode relaxation: any authenticated user can view wallet.
        // TODO tighten when role model is finalized (likely requireOwnerOrAdmin).
        SecurityContextUtil.requireUser();
        TenantWallet wallet = walletRepository.findByTenantId(tenantId)
                .orElseGet(() -> createWalletForTenant(tenantId));
        return toResponse(wallet);
    }

    @GetMapping("/transactions")
    public List<WalletTransactionResponse> listTransactions(@PathVariable Long tenantId) {
        SecurityContextUtil.requireUser();
        TenantWallet wallet = walletRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Wallet not found"));
        return transactionRepository.findByWalletIdOrderByTransactionDateDesc(wallet.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/credit")
    public TenantWalletResponse credit(
            @PathVariable Long tenantId,
            @Valid @RequestBody WalletAdjustmentRequest request
    ) {
        SecurityContextUtil.requireUser();
        TenantWallet wallet = walletRepository.findByTenantId(tenantId)
                .orElseGet(() -> createWalletForTenant(tenantId));
        applyAdjustment(wallet, request, WalletTxnType.MANUAL_CREDIT);
        return toResponse(walletRepository.save(wallet));
    }

    @PostMapping("/debit")
    public TenantWalletResponse debit(
            @PathVariable Long tenantId,
            @Valid @RequestBody WalletAdjustmentRequest request
    ) {
        SecurityContextUtil.requireUser();
        TenantWallet wallet = walletRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Wallet not found"));
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Insufficient balance");
        }
        applyAdjustment(wallet, request, WalletTxnType.MANUAL_DEBIT);
        return toResponse(walletRepository.save(wallet));
    }

    private TenantWallet createWalletForTenant(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(NOT_FOUND, "Tenant not found");
        }
        BigDecimal initialBalance = getDecimalConfig("INITIAL_WALLET_BALANCE", BigDecimal.ZERO);
        TenantWallet wallet = new TenantWallet();
        wallet.setTenantId(tenantId);
        wallet.setBalance(initialBalance);
        wallet.setCurrency(getStringConfig("DEFAULT_CURRENCY", "TND"));
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setLastTransactionAt(LocalDateTime.now());
        wallet = walletRepository.save(wallet);
        createTransaction(wallet, WalletTxnType.INITIAL_CREDIT, initialBalance, "Initial credit for new tenant", null);
        return wallet;
    }

    private void applyAdjustment(TenantWallet wallet, WalletAdjustmentRequest request, WalletTxnType type) {
        BigDecimal before = wallet.getBalance();
        BigDecimal after = type == WalletTxnType.MANUAL_DEBIT
                ? before.subtract(request.getAmount())
                : before.add(request.getAmount());
        wallet.setBalance(after);
        wallet.setLastTransactionAt(LocalDateTime.now());
        walletRepository.save(wallet);
        createTransaction(wallet, type, request.getAmount(), request.getReason(), request.getReference());
    }

    private void createTransaction(
            TenantWallet wallet,
            WalletTxnType type,
            BigDecimal amount,
            String reason,
            String reference
    ) {
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        WalletTransaction txn = new WalletTransaction();
        txn.setTenantId(wallet.getTenantId());
        txn.setWalletId(wallet.getId());
        txn.setType(type);
        txn.setAmount(amount);
        txn.setBalanceBefore(wallet.getBalance().add(type == WalletTxnType.MANUAL_DEBIT ? amount : amount.negate()));
        txn.setBalanceAfter(wallet.getBalance());
        txn.setReason(reason);
        txn.setReference(reference);
        txn.setProcessedBy(user.getUserId());
        txn.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(txn);
    }

    private TenantWalletResponse toResponse(TenantWallet wallet) {
        return TenantWalletResponse.builder()
                .id(wallet.getId())
                .tenantId(wallet.getTenantId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus())
                .lastTransactionAt(wallet.getLastTransactionAt())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private WalletTransactionResponse toResponse(WalletTransaction txn) {
        return WalletTransactionResponse.builder()
                .id(txn.getId())
                .tenantId(txn.getTenantId())
                .walletId(txn.getWalletId())
                .type(txn.getType())
                .amount(txn.getAmount())
                .balanceBefore(txn.getBalanceBefore())
                .balanceAfter(txn.getBalanceAfter())
                .reason(txn.getReason())
                .reference(txn.getReference())
                .processedBy(txn.getProcessedBy())
                .transactionDate(txn.getTransactionDate())
                .createdAt(txn.getCreatedAt())
                .build();
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
