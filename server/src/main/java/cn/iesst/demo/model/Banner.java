package cn.iesst.demo.model;

public record Banner(
        Long id,
        String title,
        String imageUrl,
        String linkUrl,
        int sortOrder,
        boolean enabled
) {}
