package cn.iesst.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudentRegisterRequest(
        @NotBlank(message = "请填写手机号")
        @Pattern(regexp = "1\\d{10}", message = "请输入 11 位手机号")
        String mobile,
        @NotBlank(message = "请填写姓名")
        @Size(max = 80, message = "姓名长度不能超过80个字符")
        String displayName,
        @NotBlank(message = "请设置登录密码")
        @Size(min = 6, max = 50, message = "密码长度需为6到50个字符")
        String password,
        @NotBlank(message = "请再次输入密码")
        String confirmPassword) {
}
