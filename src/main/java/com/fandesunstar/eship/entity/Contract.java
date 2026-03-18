package com.fandesunstar.eship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合约实体类
 */
@Data
@TableName("tb_contract")
public class Contract {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 船舶ID
     */
    private Long shipId;

    /**
     * 船东ID
     */
    private Long ownerId;

    /**
     * 租家ID
     */
    private Long renterId;

    /**
     * 租赁开始日期
     */
    private LocalDate startDate;

    /**
     * 租赁结束日期
     */
    private LocalDate endDate;

    /**
     * 日租金
     */
    private BigDecimal dailyRent;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 租赁用途
     */
    private String purpose;

    /**
     * 特殊要求
     */
    private String specialRequirements;

    /**
     * 状态：PENDING(待审核)/APPROVED(已同意)/REJECTED(已拒绝)/IN_PROGRESS(进行中)/COMPLETED(已完成)/CANCELLED(已取消)
     */
    private String status;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
