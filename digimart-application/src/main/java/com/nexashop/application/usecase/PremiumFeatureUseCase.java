package com.nexashop.application.usecase;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.exception.ConflictException;
import com.nexashop.application.exception.NotFoundException;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.PlanFeatureRepository;
import com.nexashop.application.port.out.PremiumFeatureRepository;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.enums.FeatureCategory;
import java.util.List;

public class PremiumFeatureUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final PremiumFeatureRepository featureRepository;
    private final PlanFeatureRepository planFeatureRepository;

    public PremiumFeatureUseCase(
            CurrentUserProvider currentUserProvider,
            PremiumFeatureRepository featureRepository,
            PlanFeatureRepository planFeatureRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.featureRepository = featureRepository;
        this.planFeatureRepository = planFeatureRepository;
    }

    public record FeatureUpdate(
            String code,
            String name,
            String description,
            FeatureCategory category,
            Boolean active,
            Integer displayOrder
    ) {}

    public List<PremiumFeature> list(boolean includeInactive, FeatureCategory category) {
        currentUserProvider.requireAdminAny();
        if (category != null) {
            return includeInactive
                    ? featureRepository.findByCategoryOrderByDisplayOrderAsc(category)
                    : featureRepository.findByCategoryAndActiveTrueOrderByDisplayOrderAsc(category);
        }
        return includeInactive
                ? featureRepository.findAll()
                : featureRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    public PageResult<PremiumFeature> list(PageRequest request, boolean includeInactive, FeatureCategory category) {
        currentUserProvider.requireAdminAny();
        PageRequest resolved = PageRequest.of(request.page(), request.size());
        if (category != null) {
            return includeInactive
                    ? featureRepository.findByCategoryOrderByDisplayOrderAsc(resolved, category)
                    : featureRepository.findByCategoryAndActiveTrueOrderByDisplayOrderAsc(resolved, category);
        }
        return includeInactive
                ? featureRepository.findAll(resolved)
                : featureRepository.findByActiveTrueOrderByDisplayOrderAsc(resolved);
    }

    public PremiumFeature create(PremiumFeature feature) {
        currentUserProvider.requireSuperAdmin();
        if (featureRepository.findByCode(feature.getCode()).isPresent()) {
            throw new ConflictException("Feature code already exists");
        }
        if (feature.getDisplayOrder() == null || feature.getDisplayOrder() <= 0) {
            int nextDisplayOrder = featureRepository.findAll().stream()
                    .map(PremiumFeature::getDisplayOrder)
                    .filter(order -> order != null && order > 0)
                    .max(Integer::compareTo)
                    .orElse(0) + 1;
            feature.setDisplayOrder(nextDisplayOrder);
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

    public void delete(Long id) {
        currentUserProvider.requireSuperAdmin();
        PremiumFeature existing = featureRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Feature not found"));
        if (planFeatureRepository.existsByFeatureId(id)) {
            throw new ConflictException("Feature is used by at least one plan and cannot be deleted");
        }
        featureRepository.delete(existing);
    }
}

