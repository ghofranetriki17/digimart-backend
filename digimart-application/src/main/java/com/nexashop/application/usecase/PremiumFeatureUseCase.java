package com.nexashop.application.usecase;

import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.PremiumFeatureRepository;
import com.nexashop.domain.billing.entity.PremiumFeature;
import java.util.List;

public class PremiumFeatureUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final PremiumFeatureRepository featureRepository;

    public PremiumFeatureUseCase(CurrentUserProvider currentUserProvider, PremiumFeatureRepository featureRepository) {
        this.currentUserProvider = currentUserProvider;
        this.featureRepository = featureRepository;
    }

    public List<PremiumFeature> list(boolean includeInactive) {
        currentUserProvider.requireAdminAny();
        return includeInactive
                ? featureRepository.findAll()
                : featureRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }
}

