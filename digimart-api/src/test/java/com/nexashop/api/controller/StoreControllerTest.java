package com.nexashop.api.controller;

import com.nexashop.api.controller.store.StoreController;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.domain.store.entity.Store;
import com.nexashop.infrastructure.persistence.jpa.StoreJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class StoreControllerTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listStoresForbiddenWhenTenantMismatch() {
        StoreController controller = new StoreController(
                Mockito.mock(StoreJpaRepository.class),
                Mockito.mock(TenantJpaRepository.class)
        );
        setAuth(1L, "USER");
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.listStores(2L)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getStoreForbiddenAcrossTenants() {
        StoreJpaRepository storeRepo = Mockito.mock(StoreJpaRepository.class);
        StoreController controller = new StoreController(
                storeRepo,
                Mockito.mock(TenantJpaRepository.class)
        );
        setAuth(1L, "USER");
        Store store = new Store();
        store.setId(9L);
        store.setTenantId(2L);
        when(storeRepo.findById(9L)).thenReturn(Optional.of(store));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.getStore(9L)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    private void setAuth(Long tenantId, String... roles) {
        AuthenticatedUser user = new AuthenticatedUser(1L, tenantId, Set.of(roles));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );
    }
}
