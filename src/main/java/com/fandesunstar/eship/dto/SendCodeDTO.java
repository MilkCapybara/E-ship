package com.fandesunstar.eship.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 发送验证码DTO
 */
@Data
public class SendCodeDTO {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "验证码类型不能为空")
    @Pattern(regexp = "^(REGISTER|LOGIN|RESET_PASSWORD)$", message = "验证码类型不正确")
    private String type;
}
