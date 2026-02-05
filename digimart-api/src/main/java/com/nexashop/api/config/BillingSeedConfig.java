package com.nexashop.api.config;

import com.nexashop.domain.billing.entity.PlanFeature;
import com.nexashop.domain.billing.entity.PlatformConfig;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import com.nexashop.domain.billing.enums.BillingCycle;
import com.nexashop.domain.billing.enums.FeatureCategory;
import com.nexashop.infrastructure.persistence.jpa.PlanFeatureJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.PlatformConfigJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.PremiumFeatureJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.SubscriptionPlanJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class BillingSeedConfig {

    private static final Logger log = LoggerFactory.getLogger(BillingSeedConfig.class);

    @Bean
    CommandLineRunner seedBillingData(
            PlatformConfigJpaRepository configRepository,
            PremiumFeatureJpaRepository featureRepository,
            SubscriptionPlanJpaRepository planRepository,
            PlanFeatureJpaRepository planFeatureRepository
    ) {
        return args -> seed(configRepository, featureRepository, planRepository, planFeatureRepository);
    }

    @Transactional
    void seed(
            PlatformConfigJpaRepository configRepository,
            PremiumFeatureJpaRepository featureRepository,
            SubscriptionPlanJpaRepository planRepository,
            PlanFeatureJpaRepository planFeatureRepository
    ) {
        seedPlatformConfigs(configRepository);
        seedPremiumFeatures(featureRepository);
        seedStandardPlan(planRepository, planFeatureRepository);
    }

    private void seedPlatformConfigs(PlatformConfigJpaRepository configRepository) {
        Map<String, String> defaults = Map.of(
                "COMMISSION_PERCENTAGE", "2.5",
                "INITIAL_WALLET_BALANCE", "500",
                "LOW_BALANCE_THRESHOLD", "50",
                "DEFAULT_CURRENCY", "TND"
        );
        defaults.forEach((key, value) -> configRepository.findByConfigKey(key)
                .or(() -> {
                    PlatformConfig cfg = new PlatformConfig();
                    cfg.setConfigKey(key);
                    cfg.setConfigValue(value);
                    cfg.setDescription(key.replace('_', ' ').toLowerCase());
                    cfg.setUpdatedAt(LocalDateTime.now());
                    return Optional.of(configRepository.save(cfg));
                }));
    }

    private void seedPremiumFeatures(PremiumFeatureJpaRepository featureRepository) {
        record FeatureSeed(String code, String name, String desc, FeatureCategory cat, int order) {}
        List<FeatureSeed> seeds = List.of(
                new FeatureSeed("ADVANCED_ANALYTICS", "Advanced Analytics", "Detailed sales reports and insights", FeatureCategory.ANALYTICS, 1),
                new FeatureSeed("MULTI_STORE", "Multi-Store Management", "Manage multiple store locations", FeatureCategory.SALES, 2),
                new FeatureSeed("CUSTOM_THEMES", "Custom Themes", "Personalize your storefront appearance", FeatureCategory.MARKETING, 3),
                new FeatureSeed("API_ACCESS", "API Access", "Integrate with external systems", FeatureCategory.TECHNICAL, 4),
                new FeatureSeed("BULK_OPERATIONS", "Bulk Operations", "Import/export data in bulk", FeatureCategory.TECHNICAL, 5),
                new FeatureSeed("PRIORITY_SUPPORT", "Priority Support", "24/7 dedicated support", FeatureCategory.SUPPORT, 6),
                new FeatureSeed("CUSTOM_DOMAIN", "Custom Domain", "Use your own domain name", FeatureCategory.TECHNICAL, 7),
                new FeatureSeed("LOYALTY_PROGRAM", "Loyalty Program", "Customer rewards and points", FeatureCategory.MARKETING, 8)
        );

        seeds.forEach(seed -> featureRepository.findByCode(seed.code()).orElseGet(() -> {
            PremiumFeature f = new PremiumFeature();
            f.setCode(seed.code());
            f.setName(seed.name());
            f.setDescription(seed.desc());
            f.setCategory(seed.cat());
            f.setActive(true);
            f.setDisplayOrder(seed.order());
            return featureRepository.save(f);
        }));
    }

    private void seedStandardPlan(
            SubscriptionPlanJpaRepository planRepository,
            PlanFeatureJpaRepository planFeatureRepository
    ) {
        SubscriptionPlan plan = planRepository.findByCode("STANDARD").orElseGet(() -> {
            SubscriptionPlan p = new SubscriptionPlan();
            p.setCode("STANDARD");
            p.setName("Standard Plan");
            p.setDescription("Commission-based plan with basic features");
            p.setPrice(BigDecimal.ZERO);
            p.setCurrency("TND");
            p.setBillingCycle(BillingCycle.MONTHLY);
            p.setStandard(true);
            p.setActive(true);
            return planRepository.save(p);
        });

        // For now, Standard has no premium features; ensure junction table is clear.
        List<PlanFeature> existing = planFeatureRepository.findByPlanId(plan.getId());
        if (!existing.isEmpty()) {
            planFeatureRepository.deleteAll(existing);
        }
    }
}
