package com.fandesunstar.eship.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 合约列表VO（简化版）
 */
@Data
public class ContractListVO {

    private Long id;
    private Long shipId;
    private String shipName;
    private String ownerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal dailyRent;
    private BigDecimal totalAmount;
    private String status;
    private String renterName;
    private Integer renterCreditScore;
}
