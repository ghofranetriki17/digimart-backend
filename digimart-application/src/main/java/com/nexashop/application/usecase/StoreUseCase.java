package com.nexashop.application.usecase;

import com.nexashop.application.port.out.StoreRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.domain.store.entity.Store;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StoreUseCase {

    private final StoreRepository storeRepository;
    private final TenantRepository tenantRepository;

    public StoreUseCase(
            StoreRepository storeRepository,
            TenantRepository tenantRepository
    ) {
        this.storeRepository = storeRepository;
        this.tenantRepository = tenantRepository;
    }

    public Store createStore(Store store, Long targetTenantId, Long requesterTenantId, boolean isSuperAdmin) {
        Long tenantId = targetTenantId == null ? requesterTenantId : targetTenantId;
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(NOT_FOUND, "Tenant not found");
        }
        if (storeRepository.existsByTenantIdAndCode(tenantId, store.getCode())) {
            throw new ResponseStatusException(CONFLICT, "Store code already exists");
        }
        store.setTenantId(tenantId);
        return storeRepository.save(store);
    }

    public Store getStore(Long id, Long requesterTenantId, boolean isSuperAdmin) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Store not found"));
        if (!isSuperAdmin && !store.getTenantId().equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        return store;
    }

    public List<Store> listStores(Long tenantId, Long requesterTenantId, boolean isSuperAdmin) {
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        return storeRepository.findByTenantId(tenantId);
    }

    public Store updateStore(
            Long id,
            String code,
            Store updates,
            Boolean active,
            Long requesterTenantId,
            boolean isSuperAdmin
    ) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Store not found"));
        if (!isSuperAdmin && !store.getTenantId().equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        if (code != null && !code.isBlank()) {
            if (!code.equals(store.getCode())
                    && storeRepository.existsByTenantIdAndCode(store.getTenantId(), code)) {
                throw new ResponseStatusException(CONFLICT, "Store code already exists");
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
        store.setImageUrl(updates.getImageUrl());
        store.setLatitude(updates.getLatitude());
        store.setLongitude(updates.getLongitude());
        if (active != null) {
            store.setActive(active);
        }
        return storeRepository.save(store);
    }

    public Store updateStoreImage(Long id, String imageUrl, Long requesterTenantId, boolean isSuperAdmin) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Store not found"));
        if (!isSuperAdmin && !store.getTenantId().equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        store.setImageUrl(imageUrl);
        return storeRepository.save(store);
    }

    public Store setStoreActive(Long id, boolean active, Long requesterTenantId, boolean isSuperAdmin) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Store not found"));
        if (!isSuperAdmin && !store.getTenantId().equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        store.setActive(active);
        return storeRepository.save(store);
    }

    public void deleteStore(Long id, Long requesterTenantId, boolean isSuperAdmin) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Store not found"));
        if (!isSuperAdmin && !store.getTenantId().equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        storeRepository.delete(store);
    }
}
