package com.hotel.repository;

import com.hotel.model.AdminUser;
import com.hotel.model.AuditLog;
import com.hotel.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditLogRepositoryTest {

    @Test
    void savesAndFindsAllAuditLogEntries() {
        AdminUserRepository adminUserRepository = new AdminUserRepository();
        AuditLogRepository auditLogRepository = new AuditLogRepository();

        AdminUser adminUser = new AdminUser("audit-test-" + UUID.randomUUID(), "hash", Role.ADMIN);
        adminUserRepository.save(adminUser);

        AuditLog log = new AuditLog(adminUser, "LOGIN", LocalDateTime.now());
        auditLogRepository.save(log);

        assertTrue(auditLogRepository.findAll().stream().anyMatch(a -> a.getId().equals(log.getId())));
    }
}
