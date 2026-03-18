package com.fandesunstar.eship.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String email;

    private String role;

    private String companyName;

    private Integer creditScore;

    private String status;

    private LocalDateTime createdAt;
}
