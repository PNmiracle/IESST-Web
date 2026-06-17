package cn.iesst.demo.model;

public record Journal(
        Long id,
        String type,
        String title,
        String field,
        String indexType,
        String cycle,
        String description,
        String documentName,
        String documentUrl,
        boolean published
) {}
