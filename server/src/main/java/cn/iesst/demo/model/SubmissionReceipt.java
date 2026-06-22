package cn.iesst.demo.model;

import java.time.LocalDateTime;

public record SubmissionReceipt(
        Long id,
        String status,
        LocalDateTime createdAt,
        String uploadToken) {
}
