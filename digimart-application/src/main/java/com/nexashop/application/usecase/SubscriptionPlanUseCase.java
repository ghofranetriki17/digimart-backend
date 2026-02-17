package com.nexashop.application.usecase;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.exception.*;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.PlanFeatureRepository;
import com.nexashop.application.port.out.PremiumFeatureRepository;
import com.nexashop.application.port.out.SubscriptionPlanRepository;
import com.nexashop.application.port.out.TenantSubscriptionRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.billing.entity.PlanFeature;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class SubscriptionPlanUseCase {

    public record PlanDetails(
            SubscriptionPlan plan,
            List<PremiumFeature> features,
            long tenantSubscriptionsCount
    ) {}

    public record PlanUpdate(
            String name,
            String description,
            java.math.BigDecimal price,
            String currency,
            com.nexashop.domain.billing.enums.BillingCycle billingCycle,
            java.math.BigDecimal discountPercentage,
            Boolean active,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate,
            List<Long> featureIds
    ) {}

    private final CurrentUserProvider currentUserProvider;
    private final SubscriptionPlanRepository planRepository;
    private final PremiumFeatureRepository featureRepository;
    private final PlanFeatureRepository planFeatureRepository;
    private final TenantSubscriptionRepository tenantSubscriptionRepository;

    public SubscriptionPlanUseCase(
            CurrentUserProvider currentUserProvider,
            SubscriptionPlanRepository planRepository,
            PremiumFeatureRepository featureRepository,
            PlanFeatureRepository planFeatureRepository,
            TenantSubscriptionRepository tenantSubscriptionRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.planRepository = planRepository;
        this.featureRepository = featureRepository;
        this.planFeatureRepository = planFeatureRepository;
        this.tenantSubscriptionRepository = tenantSubscriptionRepository;
    }

    public List<PlanDetails> listPlans(boolean onlyActive) {
        List<SubscriptionPlan> plans = onlyActive
                ? planRepository.findByActiveTrueOrderByNameAsc()
                : planRepository.findAll();
        return plans.stream()
                .map(this::toDetails)
                .collect(Collectors.toList());
    }

    public PageResult<PlanDetails> listPlans(PageRequest request, boolean onlyActive) {
        PageRequest resolved = PageRequest.of(request.page(), request.size());
        PageResult<SubscriptionPlan> page = onlyActive
                ? planRepository.findByActiveTrueOrderByNameAsc(resolved)
                : planRepository.findAll(resolved);
        List<PlanDetails> details = page.items().stream()
                .map(this::toDetails)
                .collect(Collectors.toList());
        return PageResult.of(details, page.page(), page.size(), page.totalItems());
    }

    public PlanDetails getPlan(Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plan not found"));
        return toDetails(plan);
    }

    public PlanDetails createPlan(SubscriptionPlan plan, List<Long> featureIds) {
        currentUserProvider.requireAdminAny();
        CurrentUser currentUser = currentUserProvider.requireUser();
        if (plan.getCreatedBy() == null) {
            plan.setCreatedBy(currentUser.userId());
        }
        if (planRepository.findByCode(plan.getCode()).isPresent()) {
            throw new ConflictException("Plan code already exists");
        }
        SubscriptionPlan saved = planRepository.save(plan);
        savePlanFeatures(saved.getId(), featureIds);
        return toDetails(saved);
    }

    public PlanDetails updatePlan(Long id, PlanUpdate update) {
        currentUserProvider.requireAdminAny();
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plan not found"));

        if (update.name() != null) plan.setName(update.name());
        if (update.description() != null) plan.setDescription(update.description());
        if (update.price() != null) plan.setPrice(update.price());
        if (update.currency() != null) plan.setCurrency(update.currency());
        if (update.billingCycle() != null) plan.setBillingCycle(update.billingCycle());
        if (update.discountPercentage() != null) plan.setDiscountPercentage(update.discountPercentage());
        if (update.active() != null) plan.setActive(update.active());
        if (update.startDate() != null) plan.setStartDate(update.startDate());
        if (update.endDate() != null) plan.setEndDate(update.endDate());

        SubscriptionPlan saved = planRepository.save(plan);
        if (update.featureIds() != null) {
            savePlanFeatures(saved.getId(), update.featureIds());
        }
        return toDetails(saved);
    }

    public PlanDetails setPlanActive(Long id, boolean active) {
        currentUserProvider.requireAdminAny();
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plan not found"));
        plan.setActive(active);
        return toDetails(planRepository.save(plan));
    }

    public void deletePlan(Long id) {
        currentUserProvider.requireAdminAny();
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plan not found"));
        if (plan.isStandard()) {
            throw new ConflictException("Standard plan cannot be deleted");
        }
        if (tenantSubscriptionRepository.countByPlanId(id) > 0) {
            throw new ConflictException("Plan has tenant subscriptions and cannot be deleted");
        }
        List<PlanFeature> links = planFeatureRepository.findByPlanId(id);
        if (!links.isEmpty()) {
            planFeatureRepository.deleteAll(links);
        }
        planRepository.delete(plan);
    }

    private void savePlanFeatures(Long planId, List<Long> featureIds) {
        List<PlanFeature> existing = planFeatureRepository.findByPlanId(planId);
        planFeatureRepository.deleteAll(existing);
        if (featureIds == null || featureIds.isEmpty()) {
            return;
        }
        Set<Long> availableFeatures = featureRepository.findAll().stream()
                .map(PremiumFeature::getId)
                .collect(Collectors.toSet());
        List<PlanFeature> toSave = new ArrayList<>();
        for (Long fid : featureIds) {
            if (!availableFeatures.contains(fid)) {
                throw new NotFoundException("Feature not found: " + fid);
            }
            PlanFeature pf = new PlanFeature();
            pf.setPlanId(planId);
            pf.setFeatureId(fid);
            toSave.add(pf);
        }
        planFeatureRepository.saveAll(toSave);
    }

    private PlanDetails toDetails(SubscriptionPlan plan) {
        List<PremiumFeature> features = planFeatureRepository.findByPlanId(plan.getId()).stream()
                .map(pf -> featureRepository.findById(pf.getFeatureId()).orElse(null))
                .filter(f -> f != null && f.isActive())
                .sorted(Comparator.comparing(PremiumFeature::getDisplayOrder))
                .collect(Collectors.toList());
        long tenantSubscriptionsCount = tenantSubscriptionRepository.countByPlanId(plan.getId());
        return new PlanDetails(plan, features, tenantSubscriptionsCount);
    }
}


