package com.fandesunstar.eship.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 更新信用评分DTO
 */
@Data
public class UpdateCreditScoreDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "信用评分不能为空")
    @Min(value = 0, message = "信用评分不能低于0")
    @Max(value = 100, message = "信用评分不能高于100")
    private Integer creditScore;
}
