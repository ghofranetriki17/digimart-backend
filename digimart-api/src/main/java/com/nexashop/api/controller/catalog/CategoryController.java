package com.nexashop.api.controller.catalog;

import com.nexashop.api.dto.request.category.CategoryDescriptionAiRequest;
import com.nexashop.api.dto.request.category.CreateCategoryRequest;
import com.nexashop.api.dto.request.category.UpdateCategoryRequest;
import com.nexashop.api.dto.response.PageResponse;
import com.nexashop.api.dto.response.category.CategoryDescriptionAiResponse;
import com.nexashop.api.dto.response.category.CategoryResponse;
import com.nexashop.application.common.PageRequest;
import com.nexashop.application.usecase.CategoryUseCase;
import com.nexashop.domain.catalog.entity.Category;
import jakarta.validation.Valid;
import java.net.URI;
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
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    public CategoryController(CategoryUseCase categoryUseCase) {
        this.categoryUseCase = categoryUseCase;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setParentCategoryId(request.getParentCategoryId());
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        Category saved = categoryUseCase.createCategory(category, request.getTenantId());
        return ResponseEntity
                .created(URI.create("/api/categories/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategory(@PathVariable Long id) {
        Category category = categoryUseCase.getCategory(id);
        return toResponse(category);
    }

    @GetMapping
    public List<CategoryResponse> listCategories(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(name = "rootOnly", defaultValue = "false") boolean rootOnly
    ) {
        return categoryUseCase.listCategories(tenantId, parentId, rootOnly).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/paged")
    public PageResponse<CategoryResponse> listCategoriesPaged(
            @RequestParam Long tenantId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(name = "rootOnly", defaultValue = "false") boolean rootOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        PageRequest request = PageRequest.of(page, size);
        return PageResponse.from(
                categoryUseCase.listCategories(request, tenantId, parentId, rootOnly),
                this::toResponse
        );
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        Category updates = new Category();
        updates.setName(request.getName());
        updates.setSlug(request.getSlug());
        updates.setDescription(request.getDescription());
        updates.setParentCategoryId(request.getParentCategoryId());
        updates.setDisplayOrder(request.getDisplayOrder());

        Category saved = categoryUseCase.updateCategory(
                id,
                request.getSlug(),
                updates,
                request.getActive()
        );
        return toResponse(saved);
    }

    @PostMapping("/{id}/activate")
    public CategoryResponse activateCategory(@PathVariable Long id) {
        Category saved = categoryUseCase.setCategoryActive(id, true);
        return toResponse(saved);
    }

    @PostMapping("/{id}/deactivate")
    public CategoryResponse deactivateCategory(@PathVariable Long id) {
        Category saved = categoryUseCase.setCategoryActive(id, false);
        return toResponse(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryUseCase.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ai-description")
    public CategoryDescriptionAiResponse suggestCategoryDescription(
            @PathVariable Long id,
            @RequestBody(required = false) CategoryDescriptionAiRequest request
    ) {
        CategoryDescriptionAiRequest resolvedRequest = request == null
                ? new CategoryDescriptionAiRequest()
                : request;
        String suggestion = categoryUseCase.suggestCategoryDescription(
                id,
                resolvedRequest.getLanguage(),
                resolvedRequest.getMaxSentences(),
                resolvedRequest.getTone()
        );
        return CategoryDescriptionAiResponse.builder()
                .categoryId(id)
                .suggestion(suggestion)
                .build();
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .tenantId(category.getTenantId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategoryId())
                .displayOrder(category.getDisplayOrder())
                .active(category.isActive())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
