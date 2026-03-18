package com.fandesunstar.eship.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合约详情VO
 */
@Data
public class ContractVO {

    private Long id;
    private Long shipId;
    private Long ownerId;
    private Long renterId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal dailyRent;
    private BigDecimal totalAmount;
    private String purpose;
    private String specialRequirements;
    private String status;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 船舶信息
    private String shipName;
    private String shipType;

    // 租家信息
    private String renterName;
    private Integer renterCreditScore;
}
