package com.fandesunstar.eship.controller;

import com.fandesunstar.eship.common.result.Result;
import com.fandesunstar.eship.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 获取船东统计数据
     */
    @GetMapping("/owner")
    public Result<Map<String, Object>> getOwnerStatistics(@RequestParam Long ownerId) {
        Map<String, Object> statistics = statisticsService.getOwnerStatistics(ownerId);
        return Result.success(statistics);
    }

    /**
     * 获取租家统计数据
     */
    @GetMapping("/renter")
    public Result<Map<String, Object>> getRenterStatistics(@RequestParam Long renterId) {
        Map<String, Object> statistics = statisticsService.getRenterStatistics(renterId);
        return Result.success(statistics);
    }

    /**
     * 获取管理员统计数据
     */
    @GetMapping("/admin")
    public Result<Map<String, Object>> getAdminStatistics() {
        Map<String, Object> statistics = statisticsService.getAdminStatistics();
        return Result.success(statistics);
    }
}
