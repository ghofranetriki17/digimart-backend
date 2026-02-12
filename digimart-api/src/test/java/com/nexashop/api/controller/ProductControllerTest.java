package com.nexashop.api.controller;

import com.nexashop.api.controller.catalog.ProductController;
import com.nexashop.api.dto.request.product.CreateProductRequest;
import com.nexashop.api.dto.request.product.ProductDescriptionAiRequest;
import com.nexashop.api.dto.response.PageResponse;
import com.nexashop.api.dto.response.product.ProductDescriptionAiResponse;
import com.nexashop.api.dto.response.product.ProductImportResponse;
import com.nexashop.api.dto.response.product.ProductPriceHistoryResponse;
import com.nexashop.api.dto.response.product.ProductResponse;
import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.usecase.ProductUseCase;
import com.nexashop.domain.catalog.entity.Product;
import com.nexashop.domain.catalog.entity.ProductImage;
import com.nexashop.domain.catalog.entity.ProductPriceHistory;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ProductControllerTest {

    @Test
    void createProductReturnsResponse() {
        ProductUseCase useCase = Mockito.mock(ProductUseCase.class);
        ProductController controller = new ProductController(useCase, "");

        CreateProductRequest request = new CreateProductRequest();
        request.setTenantId(5L);
        request.setName("Bracelet");
        request.setInitialPrice(BigDecimal.valueOf(10));
        request.setShippingPrice(BigDecimal.valueOf(2));

        Product saved = new Product();
        saved.setId(1L);
        saved.setTenantId(5L);
        saved.setName("Bracelet");
        saved.setSlug("bracelet");
        saved.setInitialPrice(BigDecimal.valueOf(10));
        saved.setShippingPrice(BigDecimal.valueOf(2));

        when(useCase.createProduct(any(Product.class), eq(5L), any(), any(), any()))
                .thenReturn(saved);

        ResponseEntity<ProductResponse> response = controller.createProduct(request);
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Bracelet", response.getBody().getName());
    }

    @Test
    void listProductsPagedReturnsPage() {
        ProductUseCase useCase = Mockito.mock(ProductUseCase.class);
        ProductController controller = new ProductController(useCase, "");

        Product product = new Product();
        product.setId(11L);
        product.setTenantId(7L);
        product.setName("Bague");
        product.setShowLowestPrice(false);

        ProductImage primary = new ProductImage();
        primary.setImageUrl("/uploads/products/primary.png");
        primary.setPrimary(true);

        when(useCase.listProducts(
                any(PageRequest.class),
                eq(7L),
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull(),
                Mockito.isNull()
        )).thenReturn(PageResult.of(List.of(product), 0, 8, 1));

        when(useCase.listProductImages(11L)).thenReturn(List.of(primary));

        PageResponse<ProductResponse> response = controller.listProductsPaged(
                7L,
                0,
                8,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(1, response.items().size());
        assertEquals("Bague", response.items().get(0).getName());
        assertEquals("/uploads/products/primary.png", response.items().get(0).getImageUrl());
    }

    @Test
    void listPriceHistoryMapsResponse() {
        ProductUseCase useCase = Mockito.mock(ProductUseCase.class);
        ProductController controller = new ProductController(useCase, "");

        ProductPriceHistory history = new ProductPriceHistory();
        history.setInitialPrice(BigDecimal.valueOf(10));
        history.setFinalPrice(BigDecimal.valueOf(8));
        history.setChangedAt(LocalDateTime.of(2026, 2, 1, 10, 30));
        history.setChangedBy(4L);

        when(useCase.listPriceHistory(any(PageRequest.class), eq(9L)))
                .thenReturn(PageResult.of(List.of(history), 0, 20, 1));

        PageResponse<ProductPriceHistoryResponse> response = controller.listPriceHistory(9L, 0, 20);
        assertEquals(1, response.items().size());
        assertEquals(BigDecimal.valueOf(8), response.items().get(0).getFinalPrice());
    }

    @Test
    void suggestProductDescriptionDelegates() {
        ProductUseCase useCase = Mockito.mock(ProductUseCase.class);
        ProductController controller = new ProductController(useCase, "");

        ProductDescriptionAiRequest request = new ProductDescriptionAiRequest();
        request.setLanguage("FR");
        request.setMaxSentences(2);
        request.setTone("neutre");

        when(useCase.suggestProductDescription(3L, "FR", 2, "neutre"))
                .thenReturn("Texte propose.");

        ProductDescriptionAiResponse response = controller.suggestProductDescription(3L, request);
        assertEquals(3L, response.getProductId());
        assertEquals("Texte propose.", response.getSuggestion());
    }

    @Test
    void exportTemplateContainsHeaders() throws Exception {
        ProductUseCase useCase = Mockito.mock(ProductUseCase.class);
        ProductController controller = new ProductController(useCase, "");

        ResponseEntity<String> response = controller.exportProductsTemplate();
        assertTrue(response.getBody().contains(
                "name,description,initial_price,final_price,cost_price,shipping_price,shipping_cost_price"
        ));
    }

    @Test
    void importRejectsMissingHeaders() throws Exception {
        ProductUseCase useCase = Mockito.mock(ProductUseCase.class);
        ProductController controller = new ProductController(useCase, "");

        String csv = "name,initial_price\nProduit,10\n";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "products.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        ProductImportResponse response = controller.importProducts(1L, file);
        assertEquals(0, response.getImported());
        assertEquals(1, response.getFailed());
        assertTrue(response.getErrors().get(0).getMessage().contains("Colonnes manquantes"));
    }
}
