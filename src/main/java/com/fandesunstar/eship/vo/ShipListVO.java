package com.fandesunstar.eship.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 船舶列表VO（简化版）
 */
@Data
public class ShipListVO {

    private Long id;
    private String shipName;
    private Long ownerId;
    private String ownerName;
    private String shipType;
    private BigDecimal tonnage;
    private Integer buildYear;
    private BigDecimal dailyRent;
    private String status;
    private BigDecimal rating;
    private String imageUrl;
}
