package com.fandesunstar.eship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 船舶实体类
 */
@Data
@TableName("tb_ship")
public class Ship {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 船东ID
     */
    private Long ownerId;

    /**
     * 船舶名称
     */
    private String shipName;

    /**
     * 船舶类型：CONTAINER(集装箱船)/BULK(散货船)/TANKER(油船)/PASSENGER(客船)
     */
    private String shipType;

    /**
     * 载重吨位
     */
    private BigDecimal tonnage;

    /**
     * 建造年份
     */
    private Integer buildYear;

    /**
     * 船级社认证
     */
    private String classificationSociety;

    /**
     * 日租金
     */
    private BigDecimal dailyRent;

    /**
     * 状态：AVAILABLE(可租)/RENTED(已租)/MAINTENANCE(维护中)
     */
    private String status;

    /**
     * 综合评分（0-5）
     */
    private BigDecimal rating;

    /**
     * 船舶图片URL
     */
    private String imageUrl;

    /**
     * 船舶描述
     */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
