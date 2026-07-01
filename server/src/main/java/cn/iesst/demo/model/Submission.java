package cn.iesst.demo.model;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record Submission(
        Long id,
        @NotBlank(message = "请填写作者姓名")
        @Size(max = 255, message = "作者姓名长度不能超过255个字符")
        String authorName,
        @NotBlank(message = "请填写联系邮箱")
        @Email(message = "联系邮箱格式不正确")
        @Size(max = 255, message = "联系邮箱长度不能超过255个字符")
        String email,
        @NotBlank(message = "请填写论文标题")
        @Size(max = 500, message = "论文标题长度不能超过500个字符")
        String paperTitle,
        @Pattern(regexp = "SCI|EI|翻译润色|科学编辑|其他", message = "投稿目标类型不正确")
        String targetType,
        @Size(max = 5000, message = "补充说明不能超过5000个字符")
        String message,
        @Size(max = 100, message = "服务类型长度不能超过100个字符")
        String serviceType,
        Boolean expedited,
        @Size(max = 50, message = "联系电话长度不能超过50个字符")
        String contactPhone,
        @Size(max = 5000, message = "特殊要求不能超过5000个字符")
        String specialRequirements,
        @Size(max = 20, message = "作者人数不能超过20人")
        List<@Valid SubmissionAuthor> authors,
        String status,
        LocalDateTime createdAt
) {}
