package com.hotel.service;

import com.hotel.model.AdminUser;
import com.hotel.model.AuditLog;
import com.hotel.repository.AuditLogRepository;
import com.hotel.util.LoggerService;

import java.time.LocalDateTime;

public class ActivityLogService {

    private final AuditLogRepository auditLogRepository;
    private final LoggerService loggerService;

    public ActivityLogService(AuditLogRepository auditLogRepository, LoggerService loggerService) {
        this.auditLogRepository = auditLogRepository;
        this.loggerService = loggerService;
    }

    public void record(AdminUser actor, String action, String entityType, String entityId, String message) {
        LocalDateTime timestamp = LocalDateTime.now();

        loggerService.info(String.format("actor=%s action=%s entityType=%s entityId=%s message=%s",
                actor.getUsername(), action, entityType, entityId, message));

        auditLogRepository.save(new AuditLog(actor, action, entityType, entityId, message, timestamp));
    }
}
