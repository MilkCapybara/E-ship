package com.fandesunstar.eship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fandesunstar.eship.common.result.Result;
import com.fandesunstar.eship.dto.ShipDTO;
import com.fandesunstar.eship.dto.ShipSearchDTO;
import com.fandesunstar.eship.service.ShipService;
import com.fandesunstar.eship.vo.ShipListVO;
import com.fandesunstar.eship.vo.ShipVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 船舶控制器
 */
@Slf4j
@RestController
@RequestMapping("/ship")
public class ShipController {

    private final ShipService shipService;

    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    /**
     * 添加船舶
     */
    @PostMapping
    public Result<ShipVO> addShip(@RequestParam Long ownerId, @Valid @RequestBody ShipDTO shipDTO) {
        ShipVO shipVO = shipService.addShip(ownerId, shipDTO);
        return Result.success("船舶添加成功", shipVO);
    }

    /**
     * 修改船舶
     */
    @PutMapping("/{id}")
    public Result<ShipVO> updateShip(@PathVariable Long id,
                                     @RequestParam Long ownerId,
                                     @Valid @RequestBody ShipDTO shipDTO) {
        ShipVO shipVO = shipService.updateShip(id, ownerId, shipDTO);
        return Result.success("船舶修改成功", shipVO);
    }

    /**
     * 删除船舶
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteShip(@PathVariable Long id, @RequestParam Long ownerId) {
        shipService.deleteShip(id, ownerId);
        return Result.success("船舶删除成功", null);
    }

    /**
     * 我的船舶列表
     */
    @GetMapping("/my")
    public Result<IPage<ShipListVO>> getMyShips(@RequestParam Long ownerId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String shipType) {
        IPage<ShipListVO> ships = shipService.getMyShips(ownerId, page, size, shipType);
        return Result.success(ships);
    }

    /**
     * 船舶详情
     */
    @GetMapping("/{id}")
    public Result<ShipVO> getShipDetail(@PathVariable Long id) {
        ShipVO shipVO = shipService.getShipDetail(id);
        return Result.success(shipVO);
    }

    /**
     * 出租中的船舶
     */
    @GetMapping("/rented")
    public Result<IPage<ShipListVO>> getRentedShips(@RequestParam Long ownerId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        IPage<ShipListVO> ships = shipService.getRentedShips(ownerId, page, size);
        return Result.success(ships);
    }

    /**
     * 搜索船舶
     */
    @PostMapping("/search")
    public Result<IPage<ShipListVO>> searchShips(@RequestBody ShipSearchDTO searchDTO) {
        IPage<ShipListVO> ships = shipService.searchShips(searchDTO);
        return Result.success(ships);
    }

    /**
     * 推荐船舶
     */
    @GetMapping("/recommended")
    public Result<List<ShipListVO>> getRecommendedShips(@RequestParam(defaultValue = "10") Integer limit) {
        List<ShipListVO> ships = shipService.getRecommendedShips(limit);
        return Result.success(ships);
    }

    /**
     * 可租赁船舶
     */
    @GetMapping("/available")
    public Result<IPage<ShipListVO>> getAvailableShips(@RequestParam(defaultValue = "1") Integer page,
                                                        @RequestParam(defaultValue = "10") Integer size) {
        IPage<ShipListVO> ships = shipService.getAvailableShips(page, size);
        return Result.success(ships);
    }
}
