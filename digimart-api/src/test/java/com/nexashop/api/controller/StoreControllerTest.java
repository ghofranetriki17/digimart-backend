package com.nexashop.api.controller;

import com.nexashop.api.controller.store.StoreController;
import com.nexashop.api.dto.request.store.CreateStoreRequest;
import com.nexashop.api.dto.response.store.StoreResponse;
import com.nexashop.application.usecase.StoreUseCase;
import com.nexashop.domain.store.entity.Store;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class StoreControllerTest {

    @Test
    void listStoresReturnsResponses() {
        StoreUseCase useCase = Mockito.mock(StoreUseCase.class);
        StoreController controller = new StoreController(useCase, "");

        Store store = new Store();
        store.setId(1L);
        store.setTenantId(2L);
        store.setName("Alpha");

        when(useCase.listStores(2L)).thenReturn(List.of(store));

        List<StoreResponse> responses = controller.listStores(2L);
        assertEquals(1, responses.size());
        assertEquals("Alpha", responses.get(0).getName());
    }

    @Test
    void createStoreReturnsResponse() {
        StoreUseCase useCase = Mockito.mock(StoreUseCase.class);
        StoreController controller = new StoreController(useCase, "");

        CreateStoreRequest request = new CreateStoreRequest();
        request.setTenantId(2L);
        request.setName("Alpha");
        request.setCode("ALP-01");

        Store saved = new Store();
        saved.setId(10L);
        saved.setTenantId(2L);
        saved.setName("Alpha");
        saved.setCode("ALP-01");

        when(useCase.createStore(Mockito.any(Store.class), Mockito.eq(2L))).thenReturn(saved);

        StoreResponse response = controller.createStore(request).getBody();
        assertEquals(10L, response.getId());
        assertEquals("ALP-01", response.getCode());
    }
}
