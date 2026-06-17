package cn.iesst.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "请输入管理员账号")
        @Size(max = 100, message = "管理员账号长度不能超过100个字符")
        String username,
        @NotBlank(message = "请输入管理员密码")
        @Size(max = 200, message = "管理员密码长度不能超过200个字符")
        String password
) {}
