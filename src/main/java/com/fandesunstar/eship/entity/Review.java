package com.fandesunstar.eship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评价实体类
 */
@Data
@TableName("tb_review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 合约ID
     */
    private Long contractId;

    /**
     * 评价人ID
     */
    private Long reviewerId;

    /**
     * 被评价人ID
     */
    private Long revieweeId;

    /**
     * 评分（1-5）
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String comment;

    /**
     * 评价时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
