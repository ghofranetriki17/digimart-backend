package com.nexashop.application.usecase;

import com.nexashop.application.exception.*;
import com.nexashop.application.port.out.PlanFeatureRepository;
import com.nexashop.application.port.out.PremiumFeatureRepository;
import com.nexashop.application.port.out.SubscriptionPlanRepository;
import com.nexashop.domain.billing.entity.PlanFeature;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class SubscriptionPlanUseCase {

    public record PlanDetails(SubscriptionPlan plan, List<PremiumFeature> features) {}

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

    private final SubscriptionPlanRepository planRepository;
    private final PremiumFeatureRepository featureRepository;
    private final PlanFeatureRepository planFeatureRepository;

    public SubscriptionPlanUseCase(
            SubscriptionPlanRepository planRepository,
            PremiumFeatureRepository featureRepository,
            PlanFeatureRepository planFeatureRepository
    ) {
        this.planRepository = planRepository;
        this.featureRepository = featureRepository;
        this.planFeatureRepository = planFeatureRepository;
    }

    public List<PlanDetails> listPlans(boolean onlyActive) {
        List<SubscriptionPlan> plans = onlyActive
                ? planRepository.findByActiveTrueOrderByNameAsc()
                : planRepository.findAll();
        return plans.stream()
                .map(this::toDetails)
                .collect(Collectors.toList());
    }

    public PlanDetails getPlan(Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plan not found"));
        return toDetails(plan);
    }

    public PlanDetails createPlan(SubscriptionPlan plan, List<Long> featureIds) {
        if (planRepository.findByCode(plan.getCode()).isPresent()) {
            throw new ConflictException("Plan code already exists");
        }
        SubscriptionPlan saved = planRepository.save(plan);
        savePlanFeatures(saved.getId(), featureIds);
        return toDetails(saved);
    }

    public PlanDetails updatePlan(Long id, PlanUpdate update) {
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
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Plan not found"));
        plan.setActive(active);
        return toDetails(planRepository.save(plan));
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
        return new PlanDetails(plan, features);
    }
}


