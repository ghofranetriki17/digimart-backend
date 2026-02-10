package com.nexashop.application.usecase;

import com.nexashop.application.exception.ConflictException;
import com.nexashop.application.exception.NotFoundException;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.PremiumFeatureRepository;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.enums.FeatureCategory;
import java.util.List;

public class PremiumFeatureUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final PremiumFeatureRepository featureRepository;

    public PremiumFeatureUseCase(CurrentUserProvider currentUserProvider, PremiumFeatureRepository featureRepository) {
        this.currentUserProvider = currentUserProvider;
        this.featureRepository = featureRepository;
    }

    public record FeatureUpdate(
            String code,
            String name,
            String description,
            FeatureCategory category,
            Boolean active,
            Integer displayOrder
    ) {}

    public List<PremiumFeature> list(boolean includeInactive) {
        currentUserProvider.requireAdminAny();
        return includeInactive
                ? featureRepository.findAll()
                : featureRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    public PremiumFeature create(PremiumFeature feature) {
        currentUserProvider.requireSuperAdmin();
        if (featureRepository.findByCode(feature.getCode()).isPresent()) {
            throw new ConflictException("Feature code already exists");
        }
        if (feature.getDisplayOrder() == null) {
            feature.setDisplayOrder(0);
        }
        return featureRepository.save(feature);
    }

    public PremiumFeature update(Long id, FeatureUpdate update) {
        currentUserProvider.requireSuperAdmin();
        PremiumFeature existing = featureRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Feature not found"));

        if (update.code() != null && !update.code().equals(existing.getCode())) {
            if (featureRepository.findByCode(update.code()).isPresent()) {
                throw new ConflictException("Feature code already exists");
            }
            existing.setCode(update.code());
        }
        if (update.name() != null) existing.setName(update.name());
        if (update.description() != null) existing.setDescription(update.description());
        if (update.category() != null) existing.setCategory(update.category());
        if (update.active() != null) existing.setActive(update.active());
        if (update.displayOrder() != null) existing.setDisplayOrder(update.displayOrder());

        return featureRepository.save(existing);
    }
}

