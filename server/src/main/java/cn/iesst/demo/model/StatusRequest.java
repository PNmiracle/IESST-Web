package cn.iesst.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StatusRequest(
        @NotBlank(message = "请选择处理状态")
        @Pattern(regexp = "待处理|沟通中|已完成", message = "处理状态不正确")
        String status
) {}
