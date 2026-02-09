package com.nexashop.api.controller;

import com.nexashop.api.controller.catalog.CategoryController;
import com.nexashop.api.dto.request.category.CategoryDescriptionAiRequest;
import com.nexashop.api.dto.request.category.CreateCategoryRequest;
import com.nexashop.api.dto.request.category.UpdateCategoryRequest;
import com.nexashop.api.dto.response.category.CategoryDescriptionAiResponse;
import com.nexashop.api.dto.response.category.CategoryResponse;
import com.nexashop.application.usecase.CategoryUseCase;
import com.nexashop.domain.catalog.entity.Category;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class CategoryControllerTest {

    @Test
    void listCategoriesReturnsResponses() {
        CategoryUseCase useCase = Mockito.mock(CategoryUseCase.class);
        CategoryController controller = new CategoryController(useCase);

        Category category = new Category();
        category.setId(1L);
        category.setTenantId(2L);
        category.setName("Shoes");
        category.setSlug("shoes");
        category.setActive(true);

        when(useCase.listCategories(2L, null, false))
                .thenReturn(List.of(category));

        List<CategoryResponse> responses = controller.listCategories(2L, null, false);
        assertEquals(1, responses.size());
        assertEquals("Shoes", responses.get(0).getName());
        assertEquals("shoes", responses.get(0).getSlug());
    }

    @Test
    void createCategoryDelegates() {
        CategoryUseCase useCase = Mockito.mock(CategoryUseCase.class);
        CategoryController controller = new CategoryController(useCase);

        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setTenantId(3L);
        request.setName("Electronics");
        request.setSlug("electronics");
        request.setDescription("Devices");

        Category saved = new Category();
        saved.setId(10L);
        saved.setTenantId(3L);
        saved.setName("Electronics");
        saved.setSlug("electronics");

        when(useCase.createCategory(Mockito.any(Category.class), Mockito.eq(3L)))
                .thenReturn(saved);

        CategoryResponse response = controller.createCategory(request).getBody();
        assertEquals(10L, response.getId());
        assertEquals("Electronics", response.getName());
    }

    @Test
    void updateCategoryDelegates() {
        CategoryUseCase useCase = Mockito.mock(CategoryUseCase.class);
        CategoryController controller = new CategoryController(useCase);

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("Men Shoes");
        request.setSlug("men-shoes");
        request.setDescription("Updated");

        Category saved = new Category();
        saved.setId(5L);
        saved.setTenantId(1L);
        saved.setName("Men Shoes");
        saved.setSlug("men-shoes");

        when(useCase.updateCategory(Mockito.eq(5L), Mockito.eq("men-shoes"), Mockito.any(Category.class), Mockito.isNull()))
                .thenReturn(saved);

        CategoryResponse response = controller.updateCategory(5L, request);
        assertEquals("Men Shoes", response.getName());
        assertEquals("men-shoes", response.getSlug());
    }

    @Test
    void suggestCategoryDescriptionDelegates() {
        CategoryUseCase useCase = Mockito.mock(CategoryUseCase.class);
        CategoryController controller = new CategoryController(useCase);

        CategoryDescriptionAiRequest request = new CategoryDescriptionAiRequest();
        request.setLanguage("FR");
        request.setMaxSentences(2);
        request.setTone("neutre");

        when(useCase.suggestCategoryDescription(7L, "FR", 2, "neutre"))
                .thenReturn("Description amelioree.");

        CategoryDescriptionAiResponse response = controller.suggestCategoryDescription(7L, request);
        assertEquals(7L, response.getCategoryId());
        assertEquals("Description amelioree.", response.getSuggestion());
    }
}
