package cn.iesst.demo.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmissionAuthor(
        Long id,
        @NotBlank(message = "请填写作者姓名")
        @Size(max = 255, message = "作者姓名长度不能超过255个字符")
        String name,
        @Email(message = "作者邮箱格式不正确")
        @Size(max = 255, message = "作者邮箱长度不能超过255个字符")
        String email,
        @Size(max = 500, message = "作者单位长度不能超过500个字符")
        String institution,
        boolean correspondingAuthor,
        Integer sortOrder) {
}
