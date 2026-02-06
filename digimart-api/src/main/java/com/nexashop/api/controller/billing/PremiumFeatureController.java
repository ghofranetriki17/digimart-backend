package com.nexashop.api.controller.billing;

import com.nexashop.api.dto.response.billing.PremiumFeatureResponse;
import com.nexashop.application.usecase.PremiumFeatureUseCase;
import com.nexashop.domain.billing.entity.PremiumFeature;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
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
