package com.hotel.repository;

import com.hotel.model.AuditLog;

import java.util.UUID;

public class AuditLogRepository extends BaseRepository<AuditLog, UUID> {

    public AuditLogRepository() {
        super(AuditLog.class);
    }
}
