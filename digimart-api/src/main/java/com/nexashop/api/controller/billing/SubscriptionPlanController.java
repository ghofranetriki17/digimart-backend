package com.nexashop.api.controller.billing;

import com.nexashop.api.dto.request.billing.CreatePlanRequest;
import com.nexashop.api.dto.request.billing.UpdatePlanRequest;
import com.nexashop.api.dto.response.PageResponse;
import com.nexashop.api.dto.response.billing.PremiumFeatureResponse;
import com.nexashop.api.dto.response.billing.SubscriptionPlanResponse;
import com.nexashop.application.common.PageRequest;
import com.nexashop.application.usecase.SubscriptionPlanUseCase;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @GetMapping("/paged")
    public PageResponse<SubscriptionPlanResponse> listPlansPaged(
            @RequestParam(defaultValue = "true") boolean onlyActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        PageRequest request = PageRequest.of(page, size);
        return PageResponse.from(
                planUseCase.listPlans(request, onlyActive),
                this::toResponse
        );
    }

    @GetMapping("/{id}")
    public SubscriptionPlanResponse getPlan(@PathVariable Long id) {
        return toResponse(planUseCase.getPlan(id));
    }

    @PostMapping
    public SubscriptionPlanResponse createPlan(@Valid @RequestBody CreatePlanRequest request) {
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

        SubscriptionPlanUseCase.PlanDetails details = planUseCase.createPlan(plan, request.getFeatureIds());
        return toResponse(details);
    }

    @PutMapping("/{id}")
    public SubscriptionPlanResponse updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlanRequest request
    ) {
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
        return toResponse(planUseCase.setPlanActive(id, true));
    }

    @PostMapping("/{id}/deactivate")
    public SubscriptionPlanResponse deactivate(@PathVariable Long id) {
        return toResponse(planUseCase.setPlanActive(id, false));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        planUseCase.deletePlan(id);
        return ResponseEntity.noContent().build();
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
                .tenantSubscriptionsCount(details.tenantSubscriptionsCount())
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
