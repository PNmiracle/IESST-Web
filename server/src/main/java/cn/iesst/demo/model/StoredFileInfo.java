package cn.iesst.demo.model;

import java.time.LocalDateTime;

public record StoredFileInfo(
        Long id,
        String fileName,
        long size,
        String contentType,
        LocalDateTime createdAt) {
}
