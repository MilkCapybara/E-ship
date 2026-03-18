package com.fandesunstar.eship.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 合约审核DTO
 */
@Data
public class ContractReviewDTO {

    private Boolean approved;

    @Size(max = 500, message = "拒绝原因长度不能超过500个字符")
    private String rejectReason;
}
