package com.fandesunstar.eship.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 船舶搜索DTO
 */
@Data
public class ShipSearchDTO {

    private String shipType;
    private BigDecimal minRent;
    private BigDecimal maxRent;
    private BigDecimal minTonnage;
    private BigDecimal maxTonnage;
    private String keyword;
    private Integer page = 1;
    private Integer size = 10;
    private String sortBy = "rating"; // rating, dailyRent, createdAt
}
