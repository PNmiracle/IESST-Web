package cn.iesst.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "请输入当前密码")
        String currentPassword,
        @NotBlank(message = "请输入新密码")
        @Size(min = 8, max = 72, message = "新密码长度需为 8-72 位")
        String newPassword,
        @NotBlank(message = "请再次输入新密码")
        String confirmPassword
) {
}
