package com.nexashop.application.usecase;

import com.nexashop.application.exception.BadRequestException;
import com.nexashop.application.exception.ConflictException;
import com.nexashop.application.exception.ForbiddenException;
import com.nexashop.application.exception.NotFoundException;
import com.nexashop.application.port.out.CategoryRepository;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.catalog.entity.Category;
import java.util.List;
import java.text.Normalizer;

public class CategoryUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final CategoryRepository categoryRepository;
    private final TenantRepository tenantRepository;

    public CategoryUseCase(
            CurrentUserProvider currentUserProvider,
            CategoryRepository categoryRepository,
            TenantRepository tenantRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.categoryRepository = categoryRepository;
        this.tenantRepository = tenantRepository;
    }

    public Category createCategory(Category category, Long targetTenantId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Long tenantId = targetTenantId == null ? requesterTenantId : targetTenantId;

        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        if (!tenantRepository.existsById(tenantId)) {
            throw new NotFoundException("Tenant not found");
        }
        String slugInput = category.getSlug();
        if (slugInput == null || slugInput.isBlank()) {
            slugInput = category.getName();
        }
        String baseSlug = slugify(slugInput);
        if (baseSlug.isBlank()) {
            throw new BadRequestException("Category slug cannot be empty");
        }
        String resolvedSlug = resolveUniqueSlug(tenantId, baseSlug, null);
        category.setSlug(resolvedSlug);
        if (category.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(category.getParentCategoryId())
                    .orElseThrow(() -> new NotFoundException("Parent category not found"));
            if (!tenantId.equals(parent.getTenantId())) {
                throw new ForbiddenException("Parent category belongs to another tenant");
            }
        }

        category.setTenantId(tenantId);
        if (category.getCreatedBy() == null) {
            category.setCreatedBy(currentUser.userId());
        }
        category.setUpdatedBy(currentUser.userId());
        return categoryRepository.save(category);
    }

    public Category getCategory(Long id) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        if (!isSuperAdmin && !category.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        return category;
    }

    public List<Category> listCategories(Long tenantId, Long parentId, boolean rootOnly) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        if (rootOnly) {
            return categoryRepository.findByTenantIdAndParentCategoryIdIsNull(tenantId);
        }
        if (parentId != null) {
            return categoryRepository.findByTenantIdAndParentCategoryId(tenantId, parentId);
        }
        return categoryRepository.findByTenantId(tenantId);
    }

    public Category updateCategory(
            Long id,
            String slug,
            Category updates,
            Boolean active
    ) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        if (!isSuperAdmin && !category.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        if (slug != null && !slug.isBlank()) {
            String baseSlug = slugify(slug);
            if (baseSlug.isBlank()) {
                throw new BadRequestException("Category slug cannot be empty");
            }
            if (!baseSlug.equals(category.getSlug())) {
                String resolvedSlug = resolveUniqueSlug(category.getTenantId(), baseSlug, id);
                category.setSlug(resolvedSlug);
            }
        }
        if (updates.getParentCategoryId() != null) {
            if (updates.getParentCategoryId().equals(id)) {
                throw new ConflictException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(updates.getParentCategoryId())
                    .orElseThrow(() -> new NotFoundException("Parent category not found"));
            if (!category.getTenantId().equals(parent.getTenantId())) {
                throw new ForbiddenException("Parent category belongs to another tenant");
            }
        }

        category.setName(updates.getName());
        category.setDescription(updates.getDescription());
        category.setParentCategoryId(updates.getParentCategoryId());
        if (updates.getDisplayOrder() != null) {
            category.setDisplayOrder(updates.getDisplayOrder());
        }
        if (active != null) {
            category.setActive(active);
        }
        category.setUpdatedBy(currentUser.userId());
        return categoryRepository.save(category);
    }

    public Category setCategoryActive(Long id, boolean active) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        if (!isSuperAdmin && !category.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        category.setActive(active);
        category.setUpdatedBy(currentUser.userId());
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        if (!isSuperAdmin && !category.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        categoryRepository.delete(category);
    }

    private String resolveUniqueSlug(Long tenantId, String baseSlug, Long currentId) {
        String candidate = baseSlug;
        int suffix = 2;
        while (true) {
            Category existing = categoryRepository.findByTenantIdAndSlug(tenantId, candidate).orElse(null);
            if (existing == null || (currentId != null && existing.getId().equals(currentId))) {
                return candidate;
            }
            candidate = baseSlug + "-" + suffix;
            suffix += 1;
            if (suffix > 9999) {
                throw new ConflictException("Unable to generate unique slug");
            }
        }
    }

    private String slugify(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = normalized
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .replaceAll("-{2,}", "-");
        return slug;
    }
}
