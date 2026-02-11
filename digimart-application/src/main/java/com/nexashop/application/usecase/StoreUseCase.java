package com.nexashop.application.usecase;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.exception.*;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.StoreRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.store.entity.Store;
import java.util.List;


public class StoreUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final StoreRepository storeRepository;
    private final TenantRepository tenantRepository;

    public StoreUseCase(
            CurrentUserProvider currentUserProvider,
            StoreRepository storeRepository,
            TenantRepository tenantRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.storeRepository = storeRepository;
        this.tenantRepository = tenantRepository;
    }

    public Store createStore(Store store, Long targetTenantId) {
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
        if (storeRepository.existsByTenantIdAndCode(tenantId, store.getCode())) {
            throw new ConflictException("Store code already exists");
        }
        store.setTenantId(tenantId);
        return storeRepository.save(store);
    }

    public Store getStore(Long id) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Store not found"));
        if (!isSuperAdmin && !store.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        return store;
    }

    public List<Store> listStores(Long tenantId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        return storeRepository.findByTenantId(tenantId);
    }

    public PageResult<Store> listStores(PageRequest request, Long tenantId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        PageRequest resolved = PageRequest.of(request.page(), request.size());
        return storeRepository.findByTenantId(resolved, tenantId);
    }

    public Store updateStore(
            Long id,
            String code,
            Store updates,
            Boolean active
    ) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Store not found"));
        if (!isSuperAdmin && !store.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        if (code != null && !code.isBlank()) {
            if (!code.equals(store.getCode())
                    && storeRepository.existsByTenantIdAndCode(store.getTenantId(), code)) {
                throw new ConflictException("Store code already exists");
            }
            store.setCode(code);
        }
        store.setName(updates.getName());
        store.setAddress(updates.getAddress());
        store.setCity(updates.getCity());
        store.setPostalCode(updates.getPostalCode());
        store.setCountry(updates.getCountry());
        store.setPhone(updates.getPhone());
        store.setEmail(updates.getEmail());
        if (updates.getImageUrl() != null && !updates.getImageUrl().isBlank()) {
            store.setImageUrl(updates.getImageUrl());
        }
        store.setLatitude(updates.getLatitude());
        store.setLongitude(updates.getLongitude());
        if (active != null) {
            store.setActive(active);
        }
        return storeRepository.save(store);
    }

    public Store updateStoreImage(Long id, String imageUrl) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Store not found"));
        if (!isSuperAdmin && !store.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        store.setImageUrl(imageUrl);
        return storeRepository.save(store);
    }

    public Store setStoreActive(Long id, boolean active) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Store not found"));
        if (!isSuperAdmin && !store.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        store.setActive(active);
        return storeRepository.save(store);
    }

    public void deleteStore(Long id) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Store not found"));
        if (!isSuperAdmin && !store.getTenantId().equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        storeRepository.delete(store);
    }
}


