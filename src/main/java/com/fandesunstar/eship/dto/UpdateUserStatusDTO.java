package com.fandesunstar.eship.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 更新用户状态DTO
 */
@Data
public class UpdateUserStatusDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(ACTIVE|LOCKED|DISABLED)$",
             message = "状态只能是ACTIVE、LOCKED或DISABLED")
    private String status;
}
