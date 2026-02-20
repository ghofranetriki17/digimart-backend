package com.nexashop.application.usecase;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.exception.*;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.StoreRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.audit.entity.AuditAction;
import com.nexashop.domain.store.entity.Store;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class StoreUseCase {

    private static final String STORE_VIEW_PERMISSION = "STORE_VIEW";
    private static final String STORE_CREATE_PERMISSION = "STORE_CREATE";
    private static final String STORE_UPDATE_PERMISSION = "STORE_UPDATE";
    private static final String STORE_DELETE_PERMISSION = "STORE_DELETE";
    private static final String AUDIT_ENTITY_TYPE_STORE = "STORE";

    private final CurrentUserProvider currentUserProvider;
    private final StoreRepository storeRepository;
    private final TenantRepository tenantRepository;
    private final AuthorizationUseCase authorizationUseCase;
    private final AuditEventUseCase auditEventUseCase;

    public StoreUseCase(
            CurrentUserProvider currentUserProvider,
            StoreRepository storeRepository,
            TenantRepository tenantRepository,
            AuthorizationUseCase authorizationUseCase,
            AuditEventUseCase auditEventUseCase
    ) {
        this.currentUserProvider = currentUserProvider;
        this.storeRepository = storeRepository;
        this.tenantRepository = tenantRepository;
        this.authorizationUseCase = authorizationUseCase;
        this.auditEventUseCase = auditEventUseCase;
    }

    public Store createStore(Store store, Long targetTenantId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Long tenantId = targetTenantId == null ? requesterTenantId : targetTenantId;
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        authorizationUseCase.requirePermission(STORE_CREATE_PERMISSION);
        if (!tenantRepository.existsById(tenantId)) {
            throw new NotFoundException("Tenant not found");
        }
        if (storeRepository.existsByTenantIdAndCode(tenantId, store.getCode())) {
            throw new ConflictException("Store code already exists");
        }
        store.setTenantId(tenantId);
        Store saved = storeRepository.save(store);
        auditEventUseCase.recordSuccess(
                AuditAction.CREATE,
                AUDIT_ENTITY_TYPE_STORE,
                saved.getId(),
                null,
                toAuditIdentityJson(saved),
                null
        );
        return saved;
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
        authorizationUseCase.requirePermission(STORE_VIEW_PERMISSION);
        return store;
    }

    public List<Store> listStores(Long tenantId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        authorizationUseCase.requirePermission(STORE_VIEW_PERMISSION);
        return storeRepository.findByTenantId(tenantId);
    }

    public PageResult<Store> listStores(PageRequest request, Long tenantId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        authorizationUseCase.requirePermission(STORE_VIEW_PERMISSION);
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
        authorizationUseCase.requirePermission(STORE_UPDATE_PERMISSION);
        Store before = copyStore(store);
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
        Store saved = storeRepository.save(store);
        AuditDiff diff = buildStoreDiff(before, saved);
        if (diff.hasChanges()) {
            auditEventUseCase.recordSuccess(
                    AuditAction.UPDATE,
                    AUDIT_ENTITY_TYPE_STORE,
                    saved.getId(),
                    diff.beforeJson(),
                    diff.afterJson(),
                    null
            );
        }
        return saved;
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
        authorizationUseCase.requirePermission(STORE_UPDATE_PERMISSION);
        Store before = copyStore(store);
        store.setImageUrl(imageUrl);
        Store saved = storeRepository.save(store);
        AuditDiff diff = buildStoreDiff(before, saved);
        if (diff.hasChanges()) {
            auditEventUseCase.recordSuccess(
                    AuditAction.UPDATE,
                    AUDIT_ENTITY_TYPE_STORE,
                    saved.getId(),
                    diff.beforeJson(),
                    diff.afterJson(),
                    null
            );
        }
        return saved;
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
        authorizationUseCase.requirePermission(STORE_UPDATE_PERMISSION);
        Store before = copyStore(store);
        store.setActive(active);
        Store saved = storeRepository.save(store);
        AuditDiff diff = buildStoreDiff(before, saved);
        if (diff.hasChanges()) {
            auditEventUseCase.recordSuccess(
                    AuditAction.UPDATE,
                    AUDIT_ENTITY_TYPE_STORE,
                    saved.getId(),
                    diff.beforeJson(),
                    diff.afterJson(),
                    null
            );
        }
        return saved;
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
        authorizationUseCase.requirePermission(STORE_DELETE_PERMISSION);
        Store before = copyStore(store);
        storeRepository.delete(store);
        auditEventUseCase.recordSuccess(
                AuditAction.DELETE,
                AUDIT_ENTITY_TYPE_STORE,
                before.getId(),
                toAuditIdentityJson(before),
                null,
                null
        );
    }

    private Store copyStore(Store source) {
        Store snapshot = new Store();
        snapshot.setId(source.getId());
        snapshot.setTenantId(source.getTenantId());
        snapshot.setName(source.getName());
        snapshot.setCode(source.getCode());
        snapshot.setAddress(source.getAddress());
        snapshot.setCity(source.getCity());
        snapshot.setPostalCode(source.getPostalCode());
        snapshot.setCountry(source.getCountry());
        snapshot.setPhone(source.getPhone());
        snapshot.setEmail(source.getEmail());
        snapshot.setImageUrl(source.getImageUrl());
        snapshot.setLatitude(source.getLatitude());
        snapshot.setLongitude(source.getLongitude());
        snapshot.setActive(source.isActive());
        snapshot.setCreatedAt(source.getCreatedAt());
        snapshot.setUpdatedAt(source.getUpdatedAt());
        return snapshot;
    }

    private AuditDiff buildStoreDiff(Store before, Store after) {
        Map<String, Object> beforeChanges = new LinkedHashMap<>();
        Map<String, Object> afterChanges = new LinkedHashMap<>();
        addChange(beforeChanges, afterChanges, "code", before.getCode(), after.getCode());
        addChange(beforeChanges, afterChanges, "name", before.getName(), after.getName());
        addChange(beforeChanges, afterChanges, "address", before.getAddress(), after.getAddress());
        addChange(beforeChanges, afterChanges, "city", before.getCity(), after.getCity());
        addChange(beforeChanges, afterChanges, "postalCode", before.getPostalCode(), after.getPostalCode());
        addChange(beforeChanges, afterChanges, "country", before.getCountry(), after.getCountry());
        addChange(beforeChanges, afterChanges, "phone", before.getPhone(), after.getPhone());
        addChange(beforeChanges, afterChanges, "email", before.getEmail(), after.getEmail());
        addChange(beforeChanges, afterChanges, "imageUrl", before.getImageUrl(), after.getImageUrl());
        addChange(beforeChanges, afterChanges, "latitude", before.getLatitude(), after.getLatitude());
        addChange(beforeChanges, afterChanges, "longitude", before.getLongitude(), after.getLongitude());
        addChange(beforeChanges, afterChanges, "active", before.isActive(), after.isActive());
        return new AuditDiff(
                toJsonObject(beforeChanges),
                toJsonObject(afterChanges),
                !beforeChanges.isEmpty()
        );
    }

    private void addChange(
            Map<String, Object> beforeChanges,
            Map<String, Object> afterChanges,
            String key,
            Object beforeValue,
            Object afterValue
    ) {
        if (valuesEqual(beforeValue, afterValue)) {
            return;
        }
        beforeChanges.put(key, beforeValue);
        afterChanges.put(key, afterValue);
    }

    private boolean valuesEqual(Object beforeValue, Object afterValue) {
        if (beforeValue instanceof BigDecimal beforeNumber && afterValue instanceof BigDecimal afterNumber) {
            return beforeNumber.compareTo(afterNumber) == 0;
        }
        return Objects.equals(beforeValue, afterValue);
    }

    private String toAuditIdentityJson(Store store) {
        if (store == null) {
            return null;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", store.getId());
        payload.put("tenantId", store.getTenantId());
        payload.put("code", store.getCode());
        payload.put("name", store.getName());
        return toJsonObject(payload);
    }

    private String toJsonObject(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return "{}";
        }
        StringBuilder json = new StringBuilder(256);
        json.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"').append(escapeJson(entry.getKey())).append('"').append(':').append(toJsonValue(entry.getValue()));
        }
        json.append('}');
        return json.toString();
    }

    private String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String text) {
            return "\"" + escapeJson(text) + "\"";
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof LocalDateTime dateTime) {
            return "\"" + escapeJson(dateTime.toString()) + "\"";
        }
        return "\"" + escapeJson(String.valueOf(value)) + "\"";
    }

    private record AuditDiff(String beforeJson, String afterJson, boolean hasChanges) {
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}


