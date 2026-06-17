package cn.iesst.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StudentOrder(
        Long id,
        String orderNo,
        Long studentUserId,
        Long sourceSubmissionId,
        String serviceCategory,
        String targetType,
        String title,
        String currentStage,
        BigDecimal amount,
        String currencyCode,
        String paymentStatus,
        String orderStatus,
        String consultantName,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
