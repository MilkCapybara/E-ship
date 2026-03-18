package com.fandesunstar.eship.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 修改密码DTO
 */
@Data
public class ChangePasswordDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 12, max = 50, message = "密码长度必须在12-50个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]+$",
             message = "密码必须包含大小写字母、数字和特殊字符")
    private String newPassword;
}
