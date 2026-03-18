package com.fandesunstar.eship.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 船舶DTO
 */
@Data
public class ShipDTO {

    @NotBlank(message = "船舶名称不能为空")
    @Size(max = 100, message = "船舶名称长度不能超过100个字符")
    private String shipName;

    @NotBlank(message = "船舶类型不能为空")
    @Pattern(regexp = "^(CONTAINER|BULK|TANKER|PASSENGER)$",
             message = "船舶类型只能是CONTAINER、BULK、TANKER或PASSENGER")
    private String shipType;

    @NotNull(message = "载重吨位不能为空")
    @DecimalMin(value = "0.01", message = "载重吨位必须大于0")
    private BigDecimal tonnage;

    @NotNull(message = "建造年份不能为空")
    @Min(value = 1900, message = "建造年份不能早于1900年")
    @Max(value = 2100, message = "建造年份不能晚于2100年")
    private Integer buildYear;

    @Size(max = 100, message = "船级社认证长度不能超过100个字符")
    private String classificationSociety;

    @NotNull(message = "日租金不能为空")
    @DecimalMin(value = "0.01", message = "日租金必须大于0")
    private BigDecimal dailyRent;

    @Size(max = 500, message = "图片URL长度不能超过500个字符")
    private String imageUrl;

    @Size(max = 1000, message = "描述长度不能超过1000个字符")
    private String description;
}
