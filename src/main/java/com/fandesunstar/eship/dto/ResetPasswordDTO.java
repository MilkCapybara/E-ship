package com.fandesunstar.eship.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 重置密码DTO
 */
@Data
public class ResetPasswordDTO {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 6, message = "验证码长度不正确")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 12, max = 50, message = "密码长度必须在12-50个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]+$",
             message = "密码必须包含大小写字母、数字和特殊字符")
    private String newPassword;
}
