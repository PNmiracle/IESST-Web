package cn.iesst.demo.model;

import java.time.LocalDateTime;

public record StudentOrderProgress(
        Long id,
        Long orderId,
        String stageCode,
        String stageLabel,
        String progressNote,
        boolean visibleToStudent,
        String operatorName,
        LocalDateTime createdAt) {
}
