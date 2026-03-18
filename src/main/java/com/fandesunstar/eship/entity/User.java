package com.fandesunstar.eship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("tb_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String email;

    /**
     * 角色：SHIP_OWNER(船东)/RENTER(租家)/ADMIN(管理员)
     */
    private String role;

    private String companyName;

    /**
     * 信用评分（0-100）
     */
    private Integer creditScore;

    /**
     * 账号状态：ACTIVE(正常)/LOCKED(锁定)/DISABLED(禁用)
     */
    private String status;

    /**
     * 锁定截止时间
     */
    private LocalDateTime lockUntil;

    /**
     * 登录失败次数
     */
    private Integer failedLoginAttempts;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
