package com.nexashop.application.usecase;

import com.nexashop.application.port.out.PremiumFeatureRepository;
import com.nexashop.domain.billing.entity.PremiumFeature;
import java.util.List;

public class PremiumFeatureUseCase {

    private final PremiumFeatureRepository featureRepository;

    public PremiumFeatureUseCase(PremiumFeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    public List<PremiumFeature> list(boolean includeInactive) {
        return includeInactive
                ? featureRepository.findAll()
                : featureRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }
}

