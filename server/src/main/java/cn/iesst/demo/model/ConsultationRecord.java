package cn.iesst.demo.model;

import java.time.LocalDateTime;

public record ConsultationRecord(
        Long id,
        Long studentUserId,
        Long sourceSubmissionId,
        String contactName,
        String mobile,
        String email,
        String sourceChannel,
        String subject,
        String content,
        String consultantName,
        String followUpStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
