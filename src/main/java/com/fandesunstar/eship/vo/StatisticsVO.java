package com.fandesunstar.eship.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 统计数据VO
 */
@Data
public class StatisticsVO {

    // 总用户数
    private Long totalUsers;

    // 总船舶数
    private Long totalShips;

    // 活跃合约数
    private Long activeContracts;

    // 总交易额
    private BigDecimal totalAmount;

    // 按角色统计用户数
    private Map<String, Long> usersByRole;

    // 按类型统计船舶数
    private Map<String, Long> shipsByType;

    // 月度统计数据
    private java.util.List<Map<String, Object>> monthlyStatistics;
}
