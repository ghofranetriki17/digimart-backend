package com.nexashop.api.controller.billing;

import com.nexashop.api.dto.request.billing.CreatePremiumFeatureRequest;
import com.nexashop.api.dto.request.billing.UpdatePremiumFeatureRequest;
import com.nexashop.api.dto.response.billing.PremiumFeatureResponse;
import com.nexashop.application.usecase.PremiumFeatureUseCase;
import com.nexashop.domain.billing.entity.PremiumFeature;
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
@RequestMapping("/api/premium-features")
public class PremiumFeatureController {

    private final PremiumFeatureUseCase featureUseCase;

    public PremiumFeatureController(PremiumFeatureUseCase featureUseCase) {
        this.featureUseCase = featureUseCase;
    }

    @GetMapping
    public List<PremiumFeatureResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return featureUseCase.list(includeInactive).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    public PremiumFeatureResponse create(@Valid @RequestBody CreatePremiumFeatureRequest request) {
        PremiumFeature feature = new PremiumFeature();
        feature.setCode(request.getCode());
        feature.setName(request.getName());
        feature.setDescription(request.getDescription());
        feature.setCategory(request.getCategory());
        feature.setActive(request.isActive());
        feature.setDisplayOrder(request.getDisplayOrder() == null ? 0 : request.getDisplayOrder());
        return toResponse(featureUseCase.create(feature));
    }

    @PutMapping("/{id}")
    public PremiumFeatureResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePremiumFeatureRequest request
    ) {
        PremiumFeatureUseCase.FeatureUpdate update = new PremiumFeatureUseCase.FeatureUpdate(
                request.getCode(),
                request.getName(),
                request.getDescription(),
                request.getCategory(),
                request.getActive(),
                request.getDisplayOrder()
        );
        return toResponse(featureUseCase.update(id, update));
    }

    private PremiumFeatureResponse toResponse(PremiumFeature f) {
        return PremiumFeatureResponse.builder()
                .id(f.getId())
                .code(f.getCode())
                .name(f.getName())
                .description(f.getDescription())
                .category(f.getCategory())
                .active(f.isActive())
                .displayOrder(f.getDisplayOrder())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
