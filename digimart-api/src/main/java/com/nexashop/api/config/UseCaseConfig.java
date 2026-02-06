package com.nexashop.api.config;

import com.nexashop.application.port.out.ActivitySectorRepository;
import com.nexashop.application.port.out.PermissionRepository;
import com.nexashop.application.port.out.PlanFeatureRepository;
import com.nexashop.application.port.out.PlatformConfigRepository;
import com.nexashop.application.port.out.PremiumFeatureRepository;
import com.nexashop.application.port.out.RefreshTokenRepository;
import com.nexashop.application.port.out.RolePermissionRepository;
import com.nexashop.application.port.out.RoleRepository;
import com.nexashop.application.port.out.StoreRepository;
import com.nexashop.application.port.out.SubscriptionHistoryRepository;
import com.nexashop.application.port.out.SubscriptionPlanRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.port.out.TenantSubscriptionRepository;
import com.nexashop.application.port.out.TenantWalletRepository;
import com.nexashop.application.port.out.UserRepository;
import com.nexashop.application.port.out.UserRoleAssignmentRepository;
import com.nexashop.application.port.out.WalletTransactionRepository;
import com.nexashop.application.service.AuthTokenService;
import com.nexashop.application.service.TenantProvisioningService;
import com.nexashop.application.usecase.ActivitySectorUseCase;
import com.nexashop.application.usecase.AdminProvisionUseCase;
import com.nexashop.application.usecase.AdminTenantSubscriptionUseCase;
import com.nexashop.application.usecase.AuthUseCase;
import com.nexashop.application.usecase.PermissionUseCase;
import com.nexashop.application.usecase.PlatformConfigUseCase;
import com.nexashop.application.usecase.PremiumFeatureUseCase;
import com.nexashop.application.usecase.RoleUseCase;
import com.nexashop.application.usecase.StoreUseCase;
import com.nexashop.application.usecase.SubscriptionPlanUseCase;
import com.nexashop.application.usecase.TenantSubscriptionUseCase;
import com.nexashop.application.usecase.TenantUseCase;
import com.nexashop.application.usecase.UserUseCase;
import com.nexashop.application.usecase.WalletUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public AuthTokenService authTokenService(RefreshTokenRepository refreshTokenRepository) {
        return new AuthTokenService(refreshTokenRepository);
    }

    @Bean
    public TenantProvisioningService tenantProvisioningService(
            PlatformConfigRepository configRepository,
            SubscriptionPlanRepository planRepository,
            TenantWalletRepository walletRepository,
            WalletTransactionRepository transactionRepository,
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionHistoryRepository historyRepository
    ) {
        return new TenantProvisioningService(
                configRepository,
                planRepository,
                walletRepository,
                transactionRepository,
                subscriptionRepository,
                historyRepository
        );
    }

    @Bean
    public ActivitySectorUseCase activitySectorUseCase(
            ActivitySectorRepository sectorRepository,
            TenantRepository tenantRepository
    ) {
        return new ActivitySectorUseCase(sectorRepository, tenantRepository);
    }

    @Bean
    public AdminProvisionUseCase adminProvisionUseCase(
            TenantRepository tenantRepository,
            TenantProvisioningService provisioningService
    ) {
        return new AdminProvisionUseCase(tenantRepository, provisioningService);
    }

    @Bean
    public AdminTenantSubscriptionUseCase adminTenantSubscriptionUseCase(
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository planRepository,
            SubscriptionHistoryRepository historyRepository,
            TenantRepository tenantRepository,
            TenantProvisioningService provisioningService
    ) {
        return new AdminTenantSubscriptionUseCase(
                subscriptionRepository,
                planRepository,
                historyRepository,
                tenantRepository,
                provisioningService
        );
    }

    @Bean
    public AuthUseCase authUseCase(
            UserRepository userRepository,
            UserRoleAssignmentRepository assignmentRepository,
            RoleRepository roleRepository,
            TenantRepository tenantRepository,
            ActivitySectorRepository sectorRepository,
            AuthTokenService tokenService,
            TenantProvisioningService provisioningService
    ) {
        return new AuthUseCase(
                userRepository,
                assignmentRepository,
                roleRepository,
                tenantRepository,
                sectorRepository,
                tokenService,
                provisioningService
        );
    }

    @Bean
    public PermissionUseCase permissionUseCase(PermissionRepository permissionRepository) {
        return new PermissionUseCase(permissionRepository);
    }

    @Bean
    public PlatformConfigUseCase platformConfigUseCase(PlatformConfigRepository configRepository) {
        return new PlatformConfigUseCase(configRepository);
    }

    @Bean
    public PremiumFeatureUseCase premiumFeatureUseCase(PremiumFeatureRepository featureRepository) {
        return new PremiumFeatureUseCase(featureRepository);
    }

    @Bean
    public RoleUseCase roleUseCase(
            RoleRepository roleRepository,
            RolePermissionRepository rolePermissionRepository,
            PermissionRepository permissionRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository
    ) {
        return new RoleUseCase(
                roleRepository,
                rolePermissionRepository,
                permissionRepository,
                userRoleAssignmentRepository
        );
    }

    @Bean
    public StoreUseCase storeUseCase(
            StoreRepository storeRepository,
            TenantRepository tenantRepository
    ) {
        return new StoreUseCase(storeRepository, tenantRepository);
    }

    @Bean
    public SubscriptionPlanUseCase subscriptionPlanUseCase(
            SubscriptionPlanRepository planRepository,
            PremiumFeatureRepository featureRepository,
            PlanFeatureRepository planFeatureRepository
    ) {
        return new SubscriptionPlanUseCase(planRepository, featureRepository, planFeatureRepository);
    }

    @Bean
    public TenantSubscriptionUseCase tenantSubscriptionUseCase(
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository planRepository,
            SubscriptionHistoryRepository historyRepository,
            TenantRepository tenantRepository,
            TenantProvisioningService provisioningService
    ) {
        return new TenantSubscriptionUseCase(
                subscriptionRepository,
                planRepository,
                historyRepository,
                tenantRepository,
                provisioningService
        );
    }

    @Bean
    public TenantUseCase tenantUseCase(
            TenantRepository tenantRepository,
            ActivitySectorRepository sectorRepository,
            TenantProvisioningService provisioningService
    ) {
        return new TenantUseCase(tenantRepository, sectorRepository, provisioningService);
    }

    @Bean
    public UserUseCase userUseCase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            TenantRepository tenantRepository,
            UserRoleAssignmentRepository assignmentRepository
    ) {
        return new UserUseCase(tenantRepository, userRepository, roleRepository, assignmentRepository);
    }

    @Bean
    public WalletUseCase walletUseCase(
            TenantWalletRepository walletRepository,
            WalletTransactionRepository transactionRepository,
            PlatformConfigRepository configRepository,
            TenantRepository tenantRepository
    ) {
        return new WalletUseCase(walletRepository, transactionRepository, configRepository, tenantRepository);
    }
}
