package com.fandesunstar.eship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fandesunstar.eship.common.result.Result;
import com.fandesunstar.eship.dto.UpdateCreditScoreDTO;
import com.fandesunstar.eship.dto.UpdateUserStatusDTO;
import com.fandesunstar.eship.service.AdminService;
import com.fandesunstar.eship.vo.ContractListVO;
import com.fandesunstar.eship.vo.ShipListVO;
import com.fandesunstar.eship.vo.StatisticsVO;
import com.fandesunstar.eship.vo.UserVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 查询所有用户
     */
    @GetMapping("/users")
    public Result<IPage<UserVO>> getAllUsers(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) String role) {
        IPage<UserVO> users = adminService.getAllUsers(page, size, role);
        return Result.success(users);
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/user/status")
    public Result<String> updateUserStatus(@Valid @RequestBody UpdateUserStatusDTO dto) {
        adminService.updateUserStatus(dto.getUserId(), dto.getStatus());
        return Result.success("用户状态更新成功", null);
    }

    /**
     * 更新信用评分
     */
    @PutMapping("/user/credit")
    public Result<String> updateCreditScore(@Valid @RequestBody UpdateCreditScoreDTO dto) {
        adminService.updateCreditScore(dto.getUserId(), dto.getCreditScore());
        return Result.success("信用评分更新成功", null);
    }

    /**
     * 用户详情
     */
    @GetMapping("/user/{id}")
    public Result<UserVO> getUserDetail(@PathVariable Long id) {
        UserVO userVO = adminService.getUserDetail(id);
        return Result.success(userVO);
    }

    /**
     * 查询所有船舶
     */
    @GetMapping("/ships")
    public Result<IPage<ShipListVO>> getAllShips(@RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        IPage<ShipListVO> ships = adminService.getAllShips(page, size);
        return Result.success(ships);
    }

    /**
     * 更新船舶状态
     */
    @PutMapping("/ship/status")
    public Result<String> updateShipStatus(@RequestParam Long shipId, @RequestParam String status) {
        adminService.updateShipStatus(shipId, status);
        return Result.success("船舶状态更新成功", null);
    }

    /**
     * 查询所有合约
     */
    @GetMapping("/contracts")
    public Result<IPage<ContractListVO>> getAllContracts(@RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size) {
        IPage<ContractListVO> contracts = adminService.getAllContracts(page, size);
        return Result.success(contracts);
    }

    /**
     * 强制终止合约
     */
    @PostMapping("/contract/{id}/terminate")
    public Result<String> forceTerminateContract(@PathVariable Long id, @RequestParam String reason) {
        adminService.forceTerminateContract(id, reason);
        return Result.success("合约已强制终止", null);
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public Result<StatisticsVO> getStatistics() {
        StatisticsVO statistics = adminService.getStatistics();
        return Result.success(statistics);
    }
}
