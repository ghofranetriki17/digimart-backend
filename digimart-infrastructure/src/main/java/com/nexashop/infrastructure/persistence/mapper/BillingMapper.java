package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.billing.entity.PlanFeature;
import com.nexashop.domain.billing.entity.PlatformConfig;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.entity.SubscriptionHistory;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import com.nexashop.domain.billing.entity.TenantSubscription;
import com.nexashop.domain.billing.entity.TenantWallet;
import com.nexashop.domain.billing.entity.WalletTransaction;
import com.nexashop.infrastructure.persistence.model.billing.PlanFeatureJpaEntity;
import com.nexashop.infrastructure.persistence.model.billing.PlatformConfigJpaEntity;
import com.nexashop.infrastructure.persistence.model.billing.PremiumFeatureJpaEntity;
import com.nexashop.infrastructure.persistence.model.billing.SubscriptionHistoryJpaEntity;
import com.nexashop.infrastructure.persistence.model.billing.SubscriptionPlanJpaEntity;
import com.nexashop.infrastructure.persistence.model.billing.TenantSubscriptionJpaEntity;
import com.nexashop.infrastructure.persistence.model.billing.TenantWalletJpaEntity;
import com.nexashop.infrastructure.persistence.model.billing.WalletTransactionJpaEntity;

public final class BillingMapper {

    private BillingMapper() {
    }

    public static SubscriptionPlan toDomain(SubscriptionPlanJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        SubscriptionPlan domain = new SubscriptionPlan();
        MapperUtils.mapAuditableToDomain(entity, domain);
        domain.setCode(entity.getCode());
        domain.setName(entity.getName());
        domain.setDescription(entity.getDescription());
        domain.setPrice(entity.getPrice());
        domain.setCurrency(entity.getCurrency());
        domain.setBillingCycle(entity.getBillingCycle());
        domain.setDiscountPercentage(entity.getDiscountPercentage());
        domain.setStandard(entity.isStandard());
        domain.setActive(entity.isActive());
        domain.setStartDate(entity.getStartDate());
        domain.setEndDate(entity.getEndDate());
        domain.setCreatedBy(entity.getCreatedBy());
        return domain;
    }

    public static SubscriptionPlanJpaEntity toJpa(SubscriptionPlan domain) {
        if (domain == null) {
            return null;
        }
        SubscriptionPlanJpaEntity entity = new SubscriptionPlanJpaEntity();
        MapperUtils.mapAuditableToJpa(domain, entity);
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setPrice(domain.getPrice());
        entity.setCurrency(domain.getCurrency());
        entity.setBillingCycle(domain.getBillingCycle());
        entity.setDiscountPercentage(domain.getDiscountPercentage());
        entity.setStandard(domain.isStandard());
        entity.setActive(domain.isActive());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setCreatedBy(domain.getCreatedBy());
        return entity;
    }

    public static PremiumFeature toDomain(PremiumFeatureJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        PremiumFeature domain = new PremiumFeature();
        MapperUtils.mapAuditableToDomain(entity, domain);
        domain.setCode(entity.getCode());
        domain.setName(entity.getName());
        domain.setDescription(entity.getDescription());
        domain.setCategory(entity.getCategory());
        domain.setActive(entity.isActive());
        domain.setDisplayOrder(entity.getDisplayOrder());
        return domain;
    }

    public static PremiumFeatureJpaEntity toJpa(PremiumFeature domain) {
        if (domain == null) {
            return null;
        }
        PremiumFeatureJpaEntity entity = new PremiumFeatureJpaEntity();
        MapperUtils.mapAuditableToJpa(domain, entity);
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setCategory(domain.getCategory());
        entity.setActive(domain.isActive());
        entity.setDisplayOrder(domain.getDisplayOrder());
        return entity;
    }

    public static PlanFeature toDomain(PlanFeatureJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        PlanFeature domain = new PlanFeature();
        MapperUtils.mapAuditableToDomain(entity, domain);
        domain.setPlanId(entity.getPlanId());
        domain.setFeatureId(entity.getFeatureId());
        return domain;
    }

    public static PlanFeatureJpaEntity toJpa(PlanFeature domain) {
        if (domain == null) {
            return null;
        }
        PlanFeatureJpaEntity entity = new PlanFeatureJpaEntity();
        MapperUtils.mapAuditableToJpa(domain, entity);
        entity.setPlanId(domain.getPlanId());
        entity.setFeatureId(domain.getFeatureId());
        return entity;
    }

    public static TenantSubscription toDomain(TenantSubscriptionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        TenantSubscription domain = new TenantSubscription();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setPlanId(entity.getPlanId());
        domain.setStatus(entity.getStatus());
        domain.setStartDate(entity.getStartDate());
        domain.setEndDate(entity.getEndDate());
        domain.setNextBillingDate(entity.getNextBillingDate());
        domain.setPricePaid(entity.getPricePaid());
        domain.setPaymentReference(entity.getPaymentReference());
        domain.setActivatedBy(entity.getActivatedBy());
        domain.setActivatedAt(entity.getActivatedAt());
        domain.setCancelledAt(entity.getCancelledAt());
        domain.setCancellationReason(entity.getCancellationReason());
        return domain;
    }

    public static TenantSubscriptionJpaEntity toJpa(TenantSubscription domain) {
        if (domain == null) {
            return null;
        }
        TenantSubscriptionJpaEntity entity = new TenantSubscriptionJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setPlanId(domain.getPlanId());
        entity.setStatus(domain.getStatus());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setNextBillingDate(domain.getNextBillingDate());
        entity.setPricePaid(domain.getPricePaid());
        entity.setPaymentReference(domain.getPaymentReference());
        entity.setActivatedBy(domain.getActivatedBy());
        entity.setActivatedAt(domain.getActivatedAt());
        entity.setCancelledAt(domain.getCancelledAt());
        entity.setCancellationReason(domain.getCancellationReason());
        return entity;
    }

    public static SubscriptionHistory toDomain(SubscriptionHistoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        SubscriptionHistory domain = new SubscriptionHistory();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setSubscriptionId(entity.getSubscriptionId());
        domain.setOldPlanId(entity.getOldPlanId());
        domain.setNewPlanId(entity.getNewPlanId());
        domain.setAction(entity.getAction());
        domain.setNotes(entity.getNotes());
        domain.setPerformedBy(entity.getPerformedBy());
        domain.setPerformedAt(entity.getPerformedAt());
        return domain;
    }

    public static SubscriptionHistoryJpaEntity toJpa(SubscriptionHistory domain) {
        if (domain == null) {
            return null;
        }
        SubscriptionHistoryJpaEntity entity = new SubscriptionHistoryJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setSubscriptionId(domain.getSubscriptionId());
        entity.setOldPlanId(domain.getOldPlanId());
        entity.setNewPlanId(domain.getNewPlanId());
        entity.setAction(domain.getAction());
        entity.setNotes(domain.getNotes());
        entity.setPerformedBy(domain.getPerformedBy());
        entity.setPerformedAt(domain.getPerformedAt());
        return entity;
    }

    public static TenantWallet toDomain(TenantWalletJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        TenantWallet domain = new TenantWallet();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setBalance(entity.getBalance());
        domain.setCurrency(entity.getCurrency());
        domain.setStatus(entity.getStatus());
        domain.setLastTransactionAt(entity.getLastTransactionAt());
        return domain;
    }

    public static TenantWalletJpaEntity toJpa(TenantWallet domain) {
        if (domain == null) {
            return null;
        }
        TenantWalletJpaEntity entity = new TenantWalletJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setBalance(domain.getBalance());
        entity.setCurrency(domain.getCurrency());
        entity.setStatus(domain.getStatus());
        entity.setLastTransactionAt(domain.getLastTransactionAt());
        return entity;
    }

    public static WalletTransaction toDomain(WalletTransactionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        WalletTransaction domain = new WalletTransaction();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setWalletId(entity.getWalletId());
        domain.setType(entity.getType());
        domain.setAmount(entity.getAmount());
        domain.setBalanceBefore(entity.getBalanceBefore());
        domain.setBalanceAfter(entity.getBalanceAfter());
        domain.setReason(entity.getReason());
        domain.setReference(entity.getReference());
        domain.setProcessedBy(entity.getProcessedBy());
        domain.setTransactionDate(entity.getTransactionDate());
        return domain;
    }

    public static WalletTransactionJpaEntity toJpa(WalletTransaction domain) {
        if (domain == null) {
            return null;
        }
        WalletTransactionJpaEntity entity = new WalletTransactionJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setWalletId(domain.getWalletId());
        entity.setType(domain.getType());
        entity.setAmount(domain.getAmount());
        entity.setBalanceBefore(domain.getBalanceBefore());
        entity.setBalanceAfter(domain.getBalanceAfter());
        entity.setReason(domain.getReason());
        entity.setReference(domain.getReference());
        entity.setProcessedBy(domain.getProcessedBy());
        entity.setTransactionDate(domain.getTransactionDate());
        return entity;
    }

    public static PlatformConfig toDomain(PlatformConfigJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        PlatformConfig domain = new PlatformConfig();
        MapperUtils.mapAuditableToDomain(entity, domain);
        domain.setConfigKey(entity.getConfigKey());
        domain.setConfigValue(entity.getConfigValue());
        domain.setDescription(entity.getDescription());
        domain.setUpdatedBy(entity.getUpdatedBy());
        return domain;
    }

    public static PlatformConfigJpaEntity toJpa(PlatformConfig domain) {
        if (domain == null) {
            return null;
        }
        PlatformConfigJpaEntity entity = new PlatformConfigJpaEntity();
        MapperUtils.mapAuditableToJpa(domain, entity);
        entity.setConfigKey(domain.getConfigKey());
        entity.setConfigValue(domain.getConfigValue());
        entity.setDescription(domain.getDescription());
        entity.setUpdatedBy(domain.getUpdatedBy());
        return entity;
    }
}
