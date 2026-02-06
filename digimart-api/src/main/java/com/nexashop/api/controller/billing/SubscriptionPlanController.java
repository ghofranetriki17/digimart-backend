package com.nexashop.api.controller.billing;

import com.nexashop.api.dto.request.billing.CreatePlanRequest;
import com.nexashop.api.dto.request.billing.UpdatePlanRequest;
import com.nexashop.api.dto.response.billing.PremiumFeatureResponse;
import com.nexashop.api.dto.response.billing.SubscriptionPlanResponse;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.domain.billing.entity.PlanFeature;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import com.nexashop.application.port.out.PlanFeatureRepository;
import com.nexashop.application.port.out.PremiumFeatureRepository;
import com.nexashop.application.port.out.SubscriptionPlanRepository;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/plans")
public class SubscriptionPlanController {

    private final SubscriptionPlanRepository planRepository;
    private final PremiumFeatureRepository featureRepository;
    private final PlanFeatureRepository planFeatureRepository;

    public SubscriptionPlanController(
            SubscriptionPlanRepository planRepository,
            PremiumFeatureRepository featureRepository,
            PlanFeatureRepository planFeatureRepository
    ) {
        this.planRepository = planRepository;
        this.featureRepository = featureRepository;
        this.planFeatureRepository = planFeatureRepository;
    }

    @GetMapping
    public List<SubscriptionPlanResponse> listPlans(@RequestParam(defaultValue = "true") boolean onlyActive) {
        List<SubscriptionPlan> plans = onlyActive
                ? planRepository.findByActiveTrueOrderByNameAsc()
                : planRepository.findAll();
        return plans.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public SubscriptionPlanResponse getPlan(@PathVariable Long id) {
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Plan not found"));
        return toResponse(plan);
    }

    @PostMapping
    public SubscriptionPlanResponse createPlan(@Valid @RequestBody CreatePlanRequest request) {
        SecurityContextUtil.requireAdminAny();
        if (planRepository.findByCode(request.getCode()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Plan code already exists");
        }
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setCode(request.getCode());
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPrice(request.getPrice());
        plan.setCurrency(request.getCurrency());
        plan.setBillingCycle(request.getBillingCycle());
        plan.setDiscountPercentage(request.getDiscountPercentage());
        plan.setStandard(request.isStandard());
        plan.setActive(request.isActive());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setCreatedBy(SecurityContextUtil.requireUser().getUserId());

        SubscriptionPlan saved = planRepository.save(plan);
        savePlanFeatures(saved.getId(), request.getFeatureIds());
        return toResponse(saved);
    }

    @PutMapping("/{id}")
    public SubscriptionPlanResponse updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlanRequest request
    ) {
        SecurityContextUtil.requireAdminAny();
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Plan not found"));

        if (request.getName() != null) plan.setName(request.getName());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getPrice() != null) plan.setPrice(request.getPrice());
        if (request.getCurrency() != null) plan.setCurrency(request.getCurrency());
        if (request.getBillingCycle() != null) plan.setBillingCycle(request.getBillingCycle());
        if (request.getDiscountPercentage() != null) plan.setDiscountPercentage(request.getDiscountPercentage());
        if (request.getActive() != null) plan.setActive(request.getActive());
        if (request.getStartDate() != null) plan.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) plan.setEndDate(request.getEndDate());

        SubscriptionPlan saved = planRepository.save(plan);
        if (request.getFeatureIds() != null) {
            savePlanFeatures(saved.getId(), request.getFeatureIds());
        }
        return toResponse(saved);
    }

    @PostMapping("/{id}/activate")
    public SubscriptionPlanResponse activate(@PathVariable Long id) {
        SecurityContextUtil.requireAdminAny();
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Plan not found"));
        plan.setActive(true);
        return toResponse(planRepository.save(plan));
    }

    @PostMapping("/{id}/deactivate")
    public SubscriptionPlanResponse deactivate(@PathVariable Long id) {
        SecurityContextUtil.requireAdminAny();
        SubscriptionPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Plan not found"));
        plan.setActive(false);
        return toResponse(planRepository.save(plan));
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
                throw new ResponseStatusException(NOT_FOUND, "Feature not found: " + fid);
            }
            PlanFeature pf = new PlanFeature();
            pf.setPlanId(planId);
            pf.setFeatureId(fid);
            toSave.add(pf);
        }
        planFeatureRepository.saveAll(toSave);
    }

    private SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        List<PremiumFeatureResponse> features = planFeatureRepository.findByPlanId(plan.getId()).stream()
                .map(pf -> featureRepository.findById(pf.getFeatureId())
                        .orElse(null))
                .filter(f -> f != null && f.isActive())
                .sorted(Comparator.comparing(PremiumFeature::getDisplayOrder))
                .map(this::toFeatureResponse)
                .collect(Collectors.toList());

        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .billingCycle(plan.getBillingCycle())
                .discountPercentage(plan.getDiscountPercentage())
                .standard(plan.isStandard())
                .active(plan.isActive())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .features(features)
                .build();
    }

    private PremiumFeatureResponse toFeatureResponse(PremiumFeature feature) {
        return PremiumFeatureResponse.builder()
                .id(feature.getId())
                .code(feature.getCode())
                .name(feature.getName())
                .description(feature.getDescription())
                .category(feature.getCategory())
                .active(feature.isActive())
                .displayOrder(feature.getDisplayOrder())
                .createdAt(feature.getCreatedAt())
                .build();
    }
}


