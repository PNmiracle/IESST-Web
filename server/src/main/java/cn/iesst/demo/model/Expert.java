package cn.iesst.demo.model;

public record Expert(
        Long id,
        String name,
        String institution,
        String role,
        String imageUrl,
        boolean published
) {
}
