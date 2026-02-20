package com.nexashop.api.dto.response.audit;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditLogResponse {

    private Long id;
    private Long tenantId;
    private String tenantName;
    private String entityType;
    private Long entityId;
    private String action;
    private String beforeJson;
    private String afterJson;
    private Long actorUserId;
    private String actorEmail;
    private String actorName;
    private String correlationId;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}
