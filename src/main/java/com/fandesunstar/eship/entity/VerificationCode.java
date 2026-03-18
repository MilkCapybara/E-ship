package com.fandesunstar.eship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 验证码实体类
 */
@Data
@TableName("tb_verification_code")
public class VerificationCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

    /**
     * 类型：LOGIN(登录)/RESET_PASSWORD(重置密码)
     */
    private String type;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 过期时间
     */
    private LocalDateTime expireAt;

    /**
     * 是否已使用
     */
    private Boolean used;
}
