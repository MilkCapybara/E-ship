package com.fandesunstar.eship.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建合约DTO
 */
@Data
public class CreateContractDTO {

    @NotNull(message = "船舶ID不能为空")
    private Long shipId;

    @NotNull(message = "开始日期不能为空")
    @Future(message = "开始日期必须是未来日期")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    @Future(message = "结束日期必须是未来日期")
    private LocalDate endDate;

    @NotNull(message = "日租金不能为空")
    @DecimalMin(value = "0.01", message = "日租金必须大于0")
    private BigDecimal dailyRent;

    @NotBlank(message = "租赁用途不能为空")
    @Size(max = 500, message = "租赁用途长度不能超过500个字符")
    private String purpose;

    @Size(max = 1000, message = "特殊要求长度不能超过1000个字符")
    private String specialRequirements;
}
