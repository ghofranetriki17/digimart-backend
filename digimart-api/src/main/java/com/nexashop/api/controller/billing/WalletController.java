package com.nexashop.api.controller.billing;

import com.nexashop.api.dto.request.billing.WalletAdjustmentRequest;
import com.nexashop.api.dto.response.billing.TenantWalletResponse;
import com.nexashop.api.dto.response.billing.WalletTransactionResponse;
import com.nexashop.application.usecase.WalletUseCase;
import com.nexashop.domain.billing.entity.TenantWallet;
import com.nexashop.domain.billing.entity.WalletTransaction;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants/{tenantId}/wallet")
public class WalletController {

    private final WalletUseCase walletUseCase;

    public WalletController(WalletUseCase walletUseCase) {
        this.walletUseCase = walletUseCase;
    }

    @GetMapping
    public TenantWalletResponse getWallet(@PathVariable Long tenantId) {
        TenantWallet wallet = walletUseCase.getWallet(tenantId);
        return toResponse(wallet);
    }

    @GetMapping("/transactions")
    public List<WalletTransactionResponse> listTransactions(@PathVariable Long tenantId) {
        return walletUseCase.listTransactions(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/credit")
    public TenantWalletResponse credit(
            @PathVariable Long tenantId,
            @Valid @RequestBody WalletAdjustmentRequest request
    ) {
        TenantWallet wallet = walletUseCase.credit(
                tenantId,
                request.getAmount(),
                request.getReason(),
                request.getReference()
        );
        return toResponse(wallet);
    }

    @PostMapping("/debit")
    public TenantWalletResponse debit(
            @PathVariable Long tenantId,
            @Valid @RequestBody WalletAdjustmentRequest request
    ) {
        TenantWallet wallet = walletUseCase.debit(
                tenantId,
                request.getAmount(),
                request.getReason(),
                request.getReference()
        );
        return toResponse(wallet);
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
}
