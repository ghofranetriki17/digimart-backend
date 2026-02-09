package com.nexashop.api.config;

import com.nexashop.application.port.out.ActivitySectorRepository;
import com.nexashop.application.port.out.AiTextProvider;
import com.nexashop.application.port.out.CategoryRepository;
import com.nexashop.application.port.out.CurrentUserProvider;
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
import com.nexashop.application.usecase.AiTextUseCase;
import com.nexashop.application.usecase.CategoryUseCase;
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
    public AiTextUseCase aiTextUseCase(
            CurrentUserProvider currentUserProvider,
            AiTextProvider aiTextProvider
    ) {
        return new AiTextUseCase(currentUserProvider, aiTextProvider);
    }

    @Bean
    public CategoryUseCase categoryUseCase(
            CurrentUserProvider currentUserProvider,
            CategoryRepository categoryRepository,
            TenantRepository tenantRepository,
            AiTextProvider aiTextProvider
    ) {
        return new CategoryUseCase(currentUserProvider, categoryRepository, tenantRepository, aiTextProvider);
    }

    @Bean
    public AdminProvisionUseCase adminProvisionUseCase(
            CurrentUserProvider currentUserProvider,
            TenantRepository tenantRepository,
            TenantProvisioningService provisioningService
    ) {
        return new AdminProvisionUseCase(currentUserProvider, tenantRepository, provisioningService);
    }

    @Bean
    public AdminTenantSubscriptionUseCase adminTenantSubscriptionUseCase(
            CurrentUserProvider currentUserProvider,
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository planRepository,
            SubscriptionHistoryRepository historyRepository,
            TenantRepository tenantRepository,
            TenantProvisioningService provisioningService
    ) {
        return new AdminTenantSubscriptionUseCase(
                currentUserProvider,
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
    public PermissionUseCase permissionUseCase(
            CurrentUserProvider currentUserProvider,
            PermissionRepository permissionRepository
    ) {
        return new PermissionUseCase(currentUserProvider, permissionRepository);
    }

    @Bean
    public PlatformConfigUseCase platformConfigUseCase(
            CurrentUserProvider currentUserProvider,
            PlatformConfigRepository configRepository
    ) {
        return new PlatformConfigUseCase(currentUserProvider, configRepository);
    }

    @Bean
    public PremiumFeatureUseCase premiumFeatureUseCase(
            CurrentUserProvider currentUserProvider,
            PremiumFeatureRepository featureRepository
    ) {
        return new PremiumFeatureUseCase(currentUserProvider, featureRepository);
    }

    @Bean
    public RoleUseCase roleUseCase(
            CurrentUserProvider currentUserProvider,
            RoleRepository roleRepository,
            RolePermissionRepository rolePermissionRepository,
            PermissionRepository permissionRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository
    ) {
        return new RoleUseCase(
                currentUserProvider,
                roleRepository,
                rolePermissionRepository,
                permissionRepository,
                userRoleAssignmentRepository
        );
    }

    @Bean
    public StoreUseCase storeUseCase(
            CurrentUserProvider currentUserProvider,
            StoreRepository storeRepository,
            TenantRepository tenantRepository
    ) {
        return new StoreUseCase(currentUserProvider, storeRepository, tenantRepository);
    }

    @Bean
    public SubscriptionPlanUseCase subscriptionPlanUseCase(
            CurrentUserProvider currentUserProvider,
            SubscriptionPlanRepository planRepository,
            PremiumFeatureRepository featureRepository,
            PlanFeatureRepository planFeatureRepository
    ) {
        return new SubscriptionPlanUseCase(currentUserProvider, planRepository, featureRepository, planFeatureRepository);
    }

    @Bean
    public TenantSubscriptionUseCase tenantSubscriptionUseCase(
            CurrentUserProvider currentUserProvider,
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository planRepository,
            SubscriptionHistoryRepository historyRepository,
            TenantRepository tenantRepository,
            TenantProvisioningService provisioningService
    ) {
        return new TenantSubscriptionUseCase(
                currentUserProvider,
                subscriptionRepository,
                planRepository,
                historyRepository,
                tenantRepository,
                provisioningService
        );
    }

    @Bean
    public TenantUseCase tenantUseCase(
            CurrentUserProvider currentUserProvider,
            TenantRepository tenantRepository,
            ActivitySectorRepository sectorRepository,
            TenantProvisioningService provisioningService
    ) {
        return new TenantUseCase(currentUserProvider, tenantRepository, sectorRepository, provisioningService);
    }

    @Bean
    public UserUseCase userUseCase(
            CurrentUserProvider currentUserProvider,
            UserRepository userRepository,
            RoleRepository roleRepository,
            TenantRepository tenantRepository,
            UserRoleAssignmentRepository assignmentRepository
    ) {
        return new UserUseCase(currentUserProvider, tenantRepository, userRepository, roleRepository, assignmentRepository);
    }

    @Bean
    public WalletUseCase walletUseCase(
            CurrentUserProvider currentUserProvider,
            TenantWalletRepository walletRepository,
            WalletTransactionRepository transactionRepository,
            PlatformConfigRepository configRepository,
            TenantRepository tenantRepository
    ) {
        return new WalletUseCase(currentUserProvider, walletRepository, transactionRepository, configRepository, tenantRepository);
    }
}
