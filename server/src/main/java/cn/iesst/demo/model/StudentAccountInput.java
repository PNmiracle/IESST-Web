package cn.iesst.demo.model;

import jakarta.validation.constraints.NotBlank;

public record StudentAccountInput(
        Long id,
        @NotBlank(message = "请填写手机号") String mobile,
        @NotBlank(message = "请填写显示名称") String displayName,
        String password,
        boolean enabled) {
}
