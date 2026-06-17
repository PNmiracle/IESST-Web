package cn.iesst.demo.model;

import java.time.LocalDateTime;

public record AuditLog(
        Long id,
        String username,
        String action,
        String resourcePath,
        int responseStatus,
        String ipAddress,
        LocalDateTime createdAt
) {
}
