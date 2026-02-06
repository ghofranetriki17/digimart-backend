package com.nexashop.application.port.out;

import com.nexashop.application.security.CurrentUser;

public interface CurrentUserProvider {
    CurrentUser getCurrentUser();

    CurrentUser requireUser();

    void requireAdminAny();

    void requireOwnerOrAdmin(Long tenantId);

    void requireSuperAdmin();
}
