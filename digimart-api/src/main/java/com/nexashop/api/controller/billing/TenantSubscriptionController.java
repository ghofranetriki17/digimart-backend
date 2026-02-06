package com.nexashop.api.controller.billing;

import com.nexashop.api.dto.request.billing.ActivateSubscriptionRequest;
import com.nexashop.api.dto.response.billing.SubscriptionHistoryResponse;
import com.nexashop.api.dto.response.billing.TenantSubscriptionResponse;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.application.usecase.TenantSubscriptionUseCase;
import com.nexashop.domain.billing.entity.SubscriptionHistory;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import com.nexashop.domain.billing.entity.TenantSubscription;
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
@RequestMapping("/api/tenants/{tenantId}/subscriptions")
public class TenantSubscriptionController {

    private final TenantSubscriptionUseCase subscriptionUseCase;

    public TenantSubscriptionController(TenantSubscriptionUseCase subscriptionUseCase) {
        this.subscriptionUseCase = subscriptionUseCase;
    }

    @GetMapping("/current")
    public TenantSubscriptionResponse getCurrent(@PathVariable Long tenantId) {
        SecurityContextUtil.requireUser();
        TenantSubscriptionUseCase.SubscriptionDetails details = subscriptionUseCase.getCurrent(tenantId);
        return toResponse(details.subscription(), details.plan());
    }

    @GetMapping("/history")
    public List<SubscriptionHistoryResponse> history(@PathVariable Long tenantId) {
        SecurityContextUtil.requireUser();
        return subscriptionUseCase.history(tenantId).stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/activate")
    public TenantSubscriptionResponse activate(
            @PathVariable Long tenantId,
            @Valid @RequestBody ActivateSubscriptionRequest request
    ) {
        SecurityContextUtil.requireOwnerOrAdmin(tenantId);
        Long actorId = SecurityContextUtil.requireUser().getUserId();
        TenantSubscriptionUseCase.SubscriptionDetails details = subscriptionUseCase.activate(
                tenantId,
                request.getPlanId(),
                request.getPricePaid(),
                request.getPaymentReference(),
                actorId
        );
        return toResponse(details.subscription(), details.plan());
    }

    private TenantSubscriptionResponse toResponse(TenantSubscription sub, SubscriptionPlan plan) {
        return TenantSubscriptionResponse.builder()
                .id(sub.getId())
                .tenantId(sub.getTenantId())
                .planId(sub.getPlanId())
                .planCode(plan != null ? plan.getCode() : null)
                .planName(plan != null ? plan.getName() : null)
                .status(sub.getStatus())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .nextBillingDate(sub.getNextBillingDate())
                .pricePaid(sub.getPricePaid())
                .paymentReference(sub.getPaymentReference())
                .activatedBy(sub.getActivatedBy())
                .activatedAt(sub.getActivatedAt())
                .cancelledAt(sub.getCancelledAt())
                .cancellationReason(sub.getCancellationReason())
                .createdAt(sub.getCreatedAt())
                .updatedAt(sub.getUpdatedAt())
                .build();
    }

    private SubscriptionHistoryResponse toHistoryResponse(SubscriptionHistory h) {
        return SubscriptionHistoryResponse.builder()
                .id(h.getId())
                .tenantId(h.getTenantId())
                .subscriptionId(h.getSubscriptionId())
                .oldPlanId(h.getOldPlanId())
                .newPlanId(h.getNewPlanId())
                .action(h.getAction())
                .notes(h.getNotes())
                .performedBy(h.getPerformedBy())
                .performedAt(h.getPerformedAt())
                .build();
    }
}
