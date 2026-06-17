package cn.iesst.demo.model;

import jakarta.validation.constraints.NotBlank;

public record SimpleStatusRequest(
        @NotBlank(message = "请选择状态")
        String status
) {}
