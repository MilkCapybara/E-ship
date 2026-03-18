package com.fandesunstar.eship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 登录日志实体类
 */
@Data
@TableName("tb_login_log")
public class LoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 登录时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime loginTime;

    /**
     * 状态：SUCCESS(成功)/FAILED(失败)
     */
    private String status;

    /**
     * 失败原因
     */
    private String failReason;
}
