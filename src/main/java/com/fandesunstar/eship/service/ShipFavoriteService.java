package com.fandesunstar.eship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fandesunstar.eship.common.exception.BusinessException;
import com.fandesunstar.eship.entity.Ship;
import com.fandesunstar.eship.entity.ShipFavorite;
import com.fandesunstar.eship.mapper.ShipFavoriteMapper;
import com.fandesunstar.eship.mapper.ShipMapper;
import com.fandesunstar.eship.vo.ShipListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 船舶收藏服务类
 */
@Slf4j
@Service
public class ShipFavoriteService {

    private final ShipFavoriteMapper shipFavoriteMapper;
    private final ShipMapper shipMapper;

    public ShipFavoriteService(ShipFavoriteMapper shipFavoriteMapper, ShipMapper shipMapper) {
        this.shipFavoriteMapper = shipFavoriteMapper;
        this.shipMapper = shipMapper;
    }

    /**
     * 添加收藏
     */
    @Transactional
    public void addFavorite(Long userId, Long shipId) {
        // 验证船舶存在
        Ship ship = shipMapper.selectById(shipId);
        if (ship == null) {
            throw new BusinessException("船舶不存在");
        }

        // 检查是否已收藏
        ShipFavorite existing = shipFavoriteMapper.selectByUserIdAndShipId(userId, shipId);
        if (existing != null) {
            throw new BusinessException("已收藏该船舶");
        }

        // 添加收藏
        ShipFavorite favorite = new ShipFavorite();
        favorite.setUserId(userId);
        favorite.setShipId(shipId);
        shipFavoriteMapper.insert(favorite);

        log.info("添加收藏成功：用户ID={}, 船舶ID={}", userId, shipId);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void removeFavorite(Long userId, Long shipId) {
        int result = shipFavoriteMapper.deleteByUserIdAndShipId(userId, shipId);
        if (result == 0) {
            throw new BusinessException("未收藏该船舶");
        }

        log.info("取消收藏成功：用户ID={}, 船舶ID={}", userId, shipId);
    }

    /**
     * 我的收藏
     */
    public IPage<ShipListVO> getMyFavorites(Long userId, Integer page, Integer size) {
        // 获取收藏的船舶ID列表
        List<Long> shipIds = shipFavoriteMapper.selectShipIdsByUserId(userId);

        if (shipIds.isEmpty()) {
            return new Page<>(page, size);
        }

        // 查询船舶详情
        List<Ship> ships = shipIds.stream()
            .map(shipMapper::selectById)
            .filter(ship -> ship != null)
            .collect(Collectors.toList());

        // 手动分页
        int start = (page - 1) * size;
        int end = Math.min(start + size, ships.size());
        List<Ship> pagedShips = ships.subList(start, end);

        // 转换为VO
        List<ShipListVO> voList = pagedShips.stream()
            .map(this::convertToShipListVO)
            .collect(Collectors.toList());

        // 构建分页结果
        Page<ShipListVO> result = new Page<>(page, size, ships.size());
        result.setRecords(voList);

        return result;
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorite(Long userId, Long shipId) {
        ShipFavorite favorite = shipFavoriteMapper.selectByUserIdAndShipId(userId, shipId);
        return favorite != null;
    }

    /**
     * 转换为ShipListVO
     */
    private ShipListVO convertToShipListVO(Ship ship) {
        ShipListVO vo = new ShipListVO();
        BeanUtils.copyProperties(ship, vo);
        return vo;
    }
}
