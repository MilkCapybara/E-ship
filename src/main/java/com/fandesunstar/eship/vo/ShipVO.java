package com.fandesunstar.eship.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 船舶详情VO
 */
@Data
public class ShipVO {

    private Long id;
    private Long ownerId;
    private String shipName;
    private String shipType;
    private BigDecimal tonnage;
    private Integer buildYear;
    private String classificationSociety;
    private BigDecimal dailyRent;
    private String status;
    private BigDecimal rating;
    private String imageUrl;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 船东信息
    private String ownerName;
    private Integer ownerCreditScore;
}
