package com.nexashop.api.controller.billing;

import com.nexashop.api.dto.request.billing.ActivateSubscriptionRequest;
import com.nexashop.api.dto.response.billing.SubscriptionHistoryResponse;
import com.nexashop.api.dto.response.billing.TenantSubscriptionResponse;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.domain.billing.entity.SubscriptionHistory;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import com.nexashop.domain.billing.entity.TenantSubscription;
import com.nexashop.domain.billing.enums.BillingCycle;
import com.nexashop.domain.billing.enums.SubscriptionAction;
import com.nexashop.domain.billing.enums.SubscriptionStatus;
import com.nexashop.api.service.TenantProvisioningService;
import com.nexashop.infrastructure.persistence.jpa.SubscriptionHistoryJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.SubscriptionPlanJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.TenantSubscriptionJpaRepository;
import jakarta.validation.Valid;
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

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/tenants/{tenantId}/subscriptions")
public class TenantSubscriptionController {

    private final TenantSubscriptionJpaRepository subscriptionRepository;
    private final SubscriptionPlanJpaRepository planRepository;
    private final SubscriptionHistoryJpaRepository historyRepository;
    private final TenantJpaRepository tenantRepository;
    private final TenantProvisioningService provisioningService;

    public TenantSubscriptionController(
            TenantSubscriptionJpaRepository subscriptionRepository,
            SubscriptionPlanJpaRepository planRepository,
            SubscriptionHistoryJpaRepository historyRepository,
            TenantJpaRepository tenantRepository,
            TenantProvisioningService provisioningService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.historyRepository = historyRepository;
        this.tenantRepository = tenantRepository;
        this.provisioningService = provisioningService;
    }

    @GetMapping("/current")
    public TenantSubscriptionResponse getCurrent(@PathVariable Long tenantId) {
        SecurityContextUtil.requireUser();
        TenantSubscription sub = subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .orElseGet(() -> subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.PENDING_ACTIVATION).orElse(null));
        if (sub == null) {
            provisioningService.ensureSubscription(tenantId);
            sub = subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                    .orElseGet(() -> subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.PENDING_ACTIVATION).orElse(null));
        }
        if (sub == null) {
            throw new ResponseStatusException(NOT_FOUND, "Subscription not found");
        }
        SubscriptionPlan plan = planRepository.findById(sub.getPlanId())
                .orElse(null);
        return toResponse(sub, plan);
    }

    @GetMapping("/history")
    public List<SubscriptionHistoryResponse> history(@PathVariable Long tenantId) {
        SecurityContextUtil.requireUser();
        return historyRepository.findByTenantIdOrderByPerformedAtDesc(tenantId).stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/activate")
    public TenantSubscriptionResponse activate(
            @PathVariable Long tenantId,
            @Valid @RequestBody ActivateSubscriptionRequest request
    ) {
        // Allow a tenant owner/admin (or super admin) to switch their own plan
        SecurityContextUtil.requireOwnerOrAdmin(tenantId);
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(NOT_FOUND, "Tenant not found");
        }

        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Plan not found"));

        // expire current
        subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .ifPresent(active -> {
                    active.setStatus(SubscriptionStatus.EXPIRED);
                    subscriptionRepository.save(active);
                    historyRepository.save(historyEntry(active, plan.getId(), SubscriptionAction.UPGRADED, null));
                });

        TenantSubscription sub = new TenantSubscription();
        sub.setTenantId(tenantId);
        sub.setPlanId(plan.getId());
        sub.setStatus(SubscriptionStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        sub.setStartDate(now);
        sub.setEndDate(calculateEndDate(now, plan.getBillingCycle()));
        sub.setNextBillingDate(sub.getEndDate());
        sub.setPricePaid(request.getPricePaid() != null ? request.getPricePaid() : plan.getPrice());
        sub.setPaymentReference(request.getPaymentReference());
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        sub.setActivatedBy(user.getUserId());
        sub.setActivatedAt(now);

        TenantSubscription saved = subscriptionRepository.save(sub);
        historyRepository.save(historyEntry(saved, null, SubscriptionAction.CREATED, null));
        return toResponse(saved, plan);
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

    private SubscriptionHistory historyEntry(TenantSubscription sub, Long newPlanId, SubscriptionAction action, String notes) {
        SubscriptionHistory h = new SubscriptionHistory();
        h.setTenantId(sub.getTenantId());
        h.setSubscriptionId(sub.getId());
        h.setOldPlanId(sub.getPlanId());
        h.setNewPlanId(newPlanId != null ? newPlanId : sub.getPlanId());
        h.setAction(action);
        h.setNotes(notes);
        h.setPerformedBy(SecurityContextUtil.requireUser().getUserId());
        h.setPerformedAt(LocalDateTime.now());
        return h;
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
