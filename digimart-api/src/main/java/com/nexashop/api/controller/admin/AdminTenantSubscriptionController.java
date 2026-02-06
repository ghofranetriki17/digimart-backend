package com.nexashop.api.controller.admin;

import com.nexashop.api.dto.request.billing.ActivateSubscriptionRequest;
import com.nexashop.api.dto.response.billing.SubscriptionHistoryResponse;
import com.nexashop.api.dto.response.billing.TenantSubscriptionResponse;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.application.usecase.AdminTenantSubscriptionUseCase;
import com.nexashop.domain.billing.entity.SubscriptionHistory;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import com.nexashop.domain.billing.entity.TenantSubscription;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tenants/{tenantId}/subscriptions")
public class AdminTenantSubscriptionController {

    private final AdminTenantSubscriptionUseCase subscriptionUseCase;

    public AdminTenantSubscriptionController(AdminTenantSubscriptionUseCase subscriptionUseCase) {
        this.subscriptionUseCase = subscriptionUseCase;
    }

    // Temporary: open access (no role check) so UI can function while RBAC is finalized.
    private void requirePlatformOrOwner() {
        // no-op
    }

    @GetMapping("/current")
    public TenantSubscriptionResponse getCurrent(@PathVariable Long tenantId) {
        requirePlatformOrOwner();
        AdminTenantSubscriptionUseCase.SubscriptionDetails details = subscriptionUseCase.getCurrent(tenantId);
        return toResponse(details.subscription(), details.plan());
    }

    @GetMapping("/history")
    public List<SubscriptionHistoryResponse> history(@PathVariable Long tenantId) {
        requirePlatformOrOwner();
        return subscriptionUseCase.history(tenantId).stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/activate")
    public TenantSubscriptionResponse activate(
            @PathVariable Long tenantId,
            @Valid @RequestBody ActivateSubscriptionRequest request
    ) {
        requirePlatformOrOwner();
        Long actorId = currentUserId();
        AdminTenantSubscriptionUseCase.SubscriptionDetails details = subscriptionUseCase.activate(
                tenantId,
                request.getPlanId(),
                request.getPricePaid(),
                request.getPaymentReference(),
                actorId
        );
        return toResponse(details.subscription(), details.plan());
    }

    @PostMapping("/deactivate")
    public TenantSubscriptionResponse deactivate(@PathVariable Long tenantId) {
        requirePlatformOrOwner();
        Long actorId = currentUserId();
        AdminTenantSubscriptionUseCase.SubscriptionDetails details = subscriptionUseCase.deactivate(tenantId, actorId);
        return toResponse(details.subscription(), details.plan());
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.getUserId();
        }
        return null;
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
