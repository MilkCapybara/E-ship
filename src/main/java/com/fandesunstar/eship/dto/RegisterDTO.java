package com.fandesunstar.eship.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 用户注册DTO
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 12, max = 50, message = "密码长度必须在12-50个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]+$",
             message = "密码必须包含大小写字母、数字和特殊字符")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 6, message = "验证码长度不正确")
    private String code;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(SHIP_OWNER|RENTER)$", message = "角色只能是SHIP_OWNER或RENTER")
    private String role;

    @Size(max = 100, message = "公司名称长度不能超过100个字符")
    private String companyName;
}
