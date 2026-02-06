package com.nexashop.api.controller.admin;

import com.nexashop.api.dto.request.billing.ActivateSubscriptionRequest;
import com.nexashop.api.dto.response.billing.SubscriptionHistoryResponse;
import com.nexashop.api.dto.response.billing.TenantSubscriptionResponse;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.api.service.TenantProvisioningService;
import com.nexashop.domain.billing.entity.SubscriptionHistory;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import com.nexashop.domain.billing.entity.TenantSubscription;
import com.nexashop.domain.billing.enums.BillingCycle;
import com.nexashop.domain.billing.enums.SubscriptionAction;
import com.nexashop.domain.billing.enums.SubscriptionStatus;
import com.nexashop.application.port.out.SubscriptionHistoryRepository;
import com.nexashop.application.port.out.SubscriptionPlanRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.port.out.TenantSubscriptionRepository;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/admin/tenants/{tenantId}/subscriptions")
public class AdminTenantSubscriptionController {

    private final TenantSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionHistoryRepository historyRepository;
    private final TenantRepository tenantRepository;
    private final TenantProvisioningService provisioningService;

    public AdminTenantSubscriptionController(
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository planRepository,
            SubscriptionHistoryRepository historyRepository,
            TenantRepository tenantRepository,
            TenantProvisioningService provisioningService
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.historyRepository = historyRepository;
        this.tenantRepository = tenantRepository;
        this.provisioningService = provisioningService;
    }

    // Temporary: open access (no role check) so UI can function while RBAC is finalized.
    private void requirePlatformOrOwner() {
        // no-op
    }

    @GetMapping("/current")
    public TenantSubscriptionResponse getCurrent(@PathVariable Long tenantId) {
        requirePlatformOrOwner();
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
        requirePlatformOrOwner();
        return historyRepository.findByTenantIdOrderByPerformedAtDesc(tenantId).stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    @PostMapping("/activate")
    @Transactional
    public TenantSubscriptionResponse activate(
            @PathVariable Long tenantId,
            @Valid @RequestBody ActivateSubscriptionRequest request
    ) {
        requirePlatformOrOwner();
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(NOT_FOUND, "Tenant not found");
        }

        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Plan not found"));

        Long actorId = currentUserId();

        // expire current (and avoid duplicate ACTIVE)
        TenantSubscription active = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .orElse(null);
        if (active != null) {
            subscriptionRepository
                    .findByTenantIdAndStatus(tenantId, SubscriptionStatus.EXPIRED)
                    .filter(existingExpired -> !existingExpired.getId().equals(active.getId()))
                    .ifPresent(existingExpired -> subscriptionRepository.deleteById(existingExpired.getId()));
            if (active.getPlanId() != null && active.getPlanId().equals(plan.getId())) {
                return toResponse(active, plan);
            }
            active.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.saveAndFlush(active);
            historyRepository.save(historyEntry(
                    active,
                    active.getPlanId(),
                    SubscriptionAction.EXPIRED,
                    "Replaced by " + plan.getCode(),
                    actorId
            ));
        }

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
        TenantSubscription saved = subscriptionRepository.save(sub);
        historyRepository.save(historyEntry(
                saved,
                saved.getPlanId(),
                SubscriptionAction.CREATED,
                null,
                actorId
        ));
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

    @PostMapping("/deactivate")
    @Transactional
    public TenantSubscriptionResponse deactivate(@PathVariable Long tenantId) {
        requirePlatformOrOwner();
        TenantSubscription active = subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No active subscription"));
        subscriptionRepository
                .findByTenantIdAndStatus(tenantId, SubscriptionStatus.EXPIRED)
                .filter(existingExpired -> !existingExpired.getId().equals(active.getId()))
                .ifPresent(existingExpired -> subscriptionRepository.deleteById(existingExpired.getId()));
        active.setStatus(SubscriptionStatus.EXPIRED);
        subscriptionRepository.save(active);

        Long actorId = currentUserId();
        historyRepository.save(historyEntry(
                active,
                active.getPlanId(),
                SubscriptionAction.EXPIRED,
                "Deactivated",
                actorId
        ));
        SubscriptionPlan plan = planRepository.findById(active.getPlanId()).orElse(null);
        return toResponse(active, plan);
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.getUserId();
        }
        return null;
    }

    private SubscriptionHistory historyEntry(
            TenantSubscription sub,
            Long newPlanId,
            SubscriptionAction action,
            String notes,
            Long performedBy
    ) {
        SubscriptionHistory h = new SubscriptionHistory();
        h.setTenantId(sub.getTenantId());
        h.setSubscriptionId(sub.getId());
        h.setOldPlanId(sub.getPlanId());
        h.setNewPlanId(newPlanId != null ? newPlanId : sub.getPlanId());
        h.setAction(action);
        h.setNotes(notes);
        h.setPerformedBy(performedBy);
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


