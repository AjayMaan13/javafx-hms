package com.hotel.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "admin_user_id")
    private AdminUser adminUser;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    private String message;

    @Column(name = "ts", nullable = false)
    private LocalDateTime timestamp;

    public AuditLog() {
    }

    public AuditLog(AdminUser adminUser, String action, LocalDateTime timestamp) {
        this(adminUser, action, null, null, null, timestamp);
    }

    public AuditLog(AdminUser adminUser, String action, String entityType, String entityId, String message,
                     LocalDateTime timestamp) {
        this.adminUser = adminUser;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public AdminUser getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(AdminUser adminUser) {
        this.adminUser = adminUser;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
