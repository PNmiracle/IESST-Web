package cn.iesst.demo.model;

public record StudentAccount(
        Long id,
        String mobile,
        String displayName,
        boolean enabled) {
}
