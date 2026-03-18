package com.fandesunstar.eship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 船舶收藏实体类
 */
@Data
@TableName("tb_ship_favorite")
public class ShipFavorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 船舶ID
     */
    private Long shipId;

    /**
     * 收藏时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
