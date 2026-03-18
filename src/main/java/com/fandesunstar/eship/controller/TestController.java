package com.fandesunstar.eship.controller;

import com.fandesunstar.eship.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("application", "E-ship");
        data.put("timestamp", LocalDateTime.now());
        return Result.success("系统运行正常", data);
    }

    /**
     * 测试接口
     */
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("欢迎使用E-ship船舶租赁平台！");
    }
}
