package com.hotel.service;

import com.hotel.model.AdminUser;
import com.hotel.model.AuditLog;
import com.hotel.model.enums.Role;
import com.hotel.repository.AdminUserRepository;
import com.hotel.repository.AuditLogRepository;
import com.hotel.util.LoggerService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityLogServiceTest {

    @Test
    void recordPersistsAnAuditLogRowWithAllFields() {
        AdminUserRepository adminUserRepository = new AdminUserRepository();
        AuditLogRepository auditLogRepository = new AuditLogRepository();
        ActivityLogService activityLogService = new ActivityLogService(auditLogRepository, LoggerService.getInstance());

        AdminUser adminUser = new AdminUser("activity-test-" + UUID.randomUUID(), "hash", Role.ADMIN);
        adminUserRepository.save(adminUser);

        activityLogService.record(adminUser, "LOGIN_SUCCESS", "AdminUser", adminUser.getId().toString(),
                "Admin logged in.");

        boolean found = auditLogRepository.findAll().stream()
                .anyMatch(log -> log.getAdminUser().getId().equals(adminUser.getId())
                        && "LOGIN_SUCCESS".equals(log.getAction())
                        && "AdminUser".equals(log.getEntityType())
                        && "Admin logged in.".equals(log.getMessage()));

        assertTrue(found);
    }
}
