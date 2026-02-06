package com.nexashop.application.usecase;

import com.nexashop.application.exception.*;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.SubscriptionHistoryRepository;
import com.nexashop.application.port.out.SubscriptionPlanRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.port.out.TenantSubscriptionRepository;
import com.nexashop.application.service.TenantProvisioningService;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.billing.entity.SubscriptionHistory;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import com.nexashop.domain.billing.entity.TenantSubscription;
import com.nexashop.domain.billing.enums.BillingCycle;
import com.nexashop.domain.billing.enums.SubscriptionAction;
import com.nexashop.domain.billing.enums.SubscriptionStatus;
import java.time.LocalDateTime;
import java.util.List;


public class AdminTenantSubscriptionUseCase {

    public record SubscriptionDetails(TenantSubscription subscription, SubscriptionPlan plan) {}

    private final CurrentUserProvider currentUserProvider;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionHistoryRepository historyRepository;
    private final TenantRepository tenantRepository;
    private final TenantProvisioningService provisioningService;

    public AdminTenantSubscriptionUseCase(
            CurrentUserProvider currentUserProvider,
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository planRepository,
            SubscriptionHistoryRepository historyRepository,
            TenantRepository tenantRepository,
            TenantProvisioningService provisioningService
    ) {
        this.currentUserProvider = currentUserProvider;
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.historyRepository = historyRepository;
        this.tenantRepository = tenantRepository;
        this.provisioningService = provisioningService;
    }

    public SubscriptionDetails getCurrent(Long tenantId) {
        TenantSubscription sub = subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .orElseGet(() -> subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.PENDING_ACTIVATION)
                        .orElse(null));
        if (sub == null) {
            provisioningService.ensureSubscription(tenantId);
            sub = subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                    .orElseGet(() -> subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.PENDING_ACTIVATION)
                            .orElse(null));
        }
        if (sub == null) {
            throw new NotFoundException("Subscription not found");
        }
        SubscriptionPlan plan = planRepository.findById(sub.getPlanId()).orElse(null);
        return new SubscriptionDetails(sub, plan);
    }

    public List<SubscriptionHistory> history(Long tenantId) {
        return historyRepository.findByTenantIdOrderByPerformedAtDesc(tenantId);
    }

    public SubscriptionDetails activate(
            Long tenantId,
            Long planId,
            java.math.BigDecimal pricePaid,
            String paymentReference
    ) {
        Long actorId = null;
        CurrentUser actor = currentUserProvider.getCurrentUser();
        if (actor != null) {
            actorId = actor.userId();
        }
        if (!tenantRepository.existsById(tenantId)) {
            throw new NotFoundException("Tenant not found");
        }

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan not found"));

        TenantSubscription active = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .orElse(null);
        if (active != null) {
            subscriptionRepository
                    .findByTenantIdAndStatus(tenantId, SubscriptionStatus.EXPIRED)
                    .filter(existingExpired -> !existingExpired.getId().equals(active.getId()))
                    .ifPresent(existingExpired -> subscriptionRepository.deleteById(existingExpired.getId()));
            if (active.getPlanId() != null && active.getPlanId().equals(plan.getId())) {
                return new SubscriptionDetails(active, plan);
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
        sub.setPricePaid(pricePaid != null ? pricePaid : plan.getPrice());
        sub.setPaymentReference(paymentReference);
        TenantSubscription saved = subscriptionRepository.save(sub);
        historyRepository.save(historyEntry(
                saved,
                saved.getPlanId(),
                SubscriptionAction.CREATED,
                null,
                actorId
        ));
        return new SubscriptionDetails(saved, plan);
    }

    public SubscriptionDetails deactivate(Long tenantId) {
        Long actorId = null;
        CurrentUser actor = currentUserProvider.getCurrentUser();
        if (actor != null) {
            actorId = actor.userId();
        }
        TenantSubscription active = subscriptionRepository.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("No active subscription"));
        subscriptionRepository
                .findByTenantIdAndStatus(tenantId, SubscriptionStatus.EXPIRED)
                .filter(existingExpired -> !existingExpired.getId().equals(active.getId()))
                .ifPresent(existingExpired -> subscriptionRepository.deleteById(existingExpired.getId()));
        active.setStatus(SubscriptionStatus.EXPIRED);
        subscriptionRepository.save(active);

        historyRepository.save(historyEntry(
                active,
                active.getPlanId(),
                SubscriptionAction.EXPIRED,
                "Deactivated",
                actorId
        ));
        SubscriptionPlan plan = planRepository.findById(active.getPlanId()).orElse(null);
        return new SubscriptionDetails(active, plan);
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
}


