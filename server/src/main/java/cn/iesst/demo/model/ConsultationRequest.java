package cn.iesst.demo.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConsultationRequest(
        @NotBlank(message = "请填写联系人姓名")
        @Size(max = 255, message = "联系人姓名长度不能超过255个字符")
        String contactName,
        @Size(max = 50, message = "联系电话长度不能超过50个字符")
        String mobile,
        @NotBlank(message = "请填写联系邮箱")
        @Email(message = "联系邮箱格式不正确")
        @Size(max = 255, message = "联系邮箱长度不能超过255个字符")
        String email,
        @NotBlank(message = "请填写咨询主题")
        @Size(max = 255, message = "咨询主题长度不能超过255个字符")
        String subject,
        @Pattern(regexp = "SCI|EI|翻译润色|科学编辑|其他", message = "咨询目标类型不正确")
        String targetType,
        @Size(max = 5000, message = "咨询内容不能超过5000个字符")
        String content
) {}
