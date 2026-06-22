package cn.iesst.demo.service;

import java.time.LocalDateTime;

public record StoredFileRecord(
        Long id,
        String fileName,
        String storageKey,
        long size,
        String contentType,
        LocalDateTime createdAt) {
}
