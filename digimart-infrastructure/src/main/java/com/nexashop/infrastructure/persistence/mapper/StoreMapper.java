package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.store.entity.Store;
import com.nexashop.infrastructure.persistence.model.store.StoreJpaEntity;

public final class StoreMapper {

    private StoreMapper() {
    }

    public static Store toDomain(StoreJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Store domain = new Store();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setName(entity.getName());
        domain.setCode(entity.getCode());
        domain.setAddress(entity.getAddress());
        domain.setCity(entity.getCity());
        domain.setPostalCode(entity.getPostalCode());
        domain.setCountry(entity.getCountry());
        domain.setPhone(entity.getPhone());
        domain.setEmail(entity.getEmail());
        domain.setImageUrl(entity.getImageUrl());
        domain.setLatitude(entity.getLatitude());
        domain.setLongitude(entity.getLongitude());
        domain.setActive(entity.isActive());
        return domain;
    }

    public static StoreJpaEntity toJpa(Store domain) {
        if (domain == null) {
            return null;
        }
        StoreJpaEntity entity = new StoreJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setName(domain.getName());
        entity.setCode(domain.getCode());
        entity.setAddress(domain.getAddress());
        entity.setCity(domain.getCity());
        entity.setPostalCode(domain.getPostalCode());
        entity.setCountry(domain.getCountry());
        entity.setPhone(domain.getPhone());
        entity.setEmail(domain.getEmail());
        entity.setImageUrl(domain.getImageUrl());
        entity.setLatitude(domain.getLatitude());
        entity.setLongitude(domain.getLongitude());
        entity.setActive(domain.isActive());
        return entity;
    }
}
