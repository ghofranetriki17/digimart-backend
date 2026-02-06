package com.nexashop.api.controller.billing;

import com.nexashop.api.dto.request.billing.CreatePlanRequest;
import com.nexashop.api.dto.request.billing.UpdatePlanRequest;
import com.nexashop.api.dto.response.billing.PremiumFeatureResponse;
import com.nexashop.api.dto.response.billing.SubscriptionPlanResponse;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.application.usecase.SubscriptionPlanUseCase;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class SubscriptionPlanController {

    private final SubscriptionPlanUseCase planUseCase;

    public SubscriptionPlanController(SubscriptionPlanUseCase planUseCase) {
        this.planUseCase = planUseCase;
    }

    @GetMapping
    public List<SubscriptionPlanResponse> listPlans(@RequestParam(defaultValue = "true") boolean onlyActive) {
        return planUseCase.listPlans(onlyActive).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public SubscriptionPlanResponse getPlan(@PathVariable Long id) {
        return toResponse(planUseCase.getPlan(id));
    }

    @PostMapping
    public SubscriptionPlanResponse createPlan(@Valid @RequestBody CreatePlanRequest request) {
        SecurityContextUtil.requireAdminAny();
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

        SubscriptionPlanUseCase.PlanDetails details = planUseCase.createPlan(plan, request.getFeatureIds());
        return toResponse(details);
    }

    @PutMapping("/{id}")
    public SubscriptionPlanResponse updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlanRequest request
    ) {
        SecurityContextUtil.requireAdminAny();
        SubscriptionPlanUseCase.PlanUpdate update = new SubscriptionPlanUseCase.PlanUpdate(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getCurrency(),
                request.getBillingCycle(),
                request.getDiscountPercentage(),
                request.getActive(),
                request.getStartDate(),
                request.getEndDate(),
                request.getFeatureIds()
        );
        SubscriptionPlanUseCase.PlanDetails details = planUseCase.updatePlan(id, update);
        return toResponse(details);
    }

    @PostMapping("/{id}/activate")
    public SubscriptionPlanResponse activate(@PathVariable Long id) {
        SecurityContextUtil.requireAdminAny();
        return toResponse(planUseCase.setPlanActive(id, true));
    }

    @PostMapping("/{id}/deactivate")
    public SubscriptionPlanResponse deactivate(@PathVariable Long id) {
        SecurityContextUtil.requireAdminAny();
        return toResponse(planUseCase.setPlanActive(id, false));
    }

    private SubscriptionPlanResponse toResponse(SubscriptionPlanUseCase.PlanDetails details) {
        List<PremiumFeatureResponse> features = details.features().stream()
                .map(this::toFeatureResponse)
                .collect(Collectors.toList());
        SubscriptionPlan plan = details.plan();

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
