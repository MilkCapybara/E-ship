package com.fandesunstar.eship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fandesunstar.eship.common.result.Result;
import com.fandesunstar.eship.service.ShipFavoriteService;
import com.fandesunstar.eship.vo.ShipListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 船舶收藏控制器
 */
@Slf4j
@RestController
@RequestMapping("/favorite")
public class ShipFavoriteController {

    private final ShipFavoriteService shipFavoriteService;

    public ShipFavoriteController(ShipFavoriteService shipFavoriteService) {
        this.shipFavoriteService = shipFavoriteService;
    }

    /**
     * 添加收藏
     */
    @PostMapping
    public Result<String> addFavorite(@RequestParam Long userId, @RequestParam Long shipId) {
        shipFavoriteService.addFavorite(userId, shipId);
        return Result.success("收藏成功", null);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{shipId}")
    public Result<String> removeFavorite(@PathVariable Long shipId, @RequestParam Long userId) {
        shipFavoriteService.removeFavorite(userId, shipId);
        return Result.success("取消收藏成功", null);
    }

    /**
     * 我的收藏
     */
    @GetMapping("/my")
    public Result<IPage<ShipListVO>> getMyFavorites(@RequestParam Long userId,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size) {
        IPage<ShipListVO> favorites = shipFavoriteService.getMyFavorites(userId, page, size);
        return Result.success(favorites);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check/{shipId}")
    public Result<Boolean> checkFavorite(@PathVariable Long shipId, @RequestParam Long userId) {
        boolean isFavorite = shipFavoriteService.isFavorite(userId, shipId);
        return Result.success(isFavorite);
    }
}
