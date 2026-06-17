package cn.iesst.demo.model;

public record ServiceOffering(
        Long id,
        String category,
        String title,
        String price,
        String description,
        String features,
        boolean published
) {
}
