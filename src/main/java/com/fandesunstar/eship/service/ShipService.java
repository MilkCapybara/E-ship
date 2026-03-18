package com.fandesunstar.eship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fandesunstar.eship.common.exception.BusinessException;
import com.fandesunstar.eship.dto.ShipDTO;
import com.fandesunstar.eship.dto.ShipSearchDTO;
import com.fandesunstar.eship.entity.Ship;
import com.fandesunstar.eship.entity.User;
import com.fandesunstar.eship.mapper.ShipMapper;
import com.fandesunstar.eship.mapper.UserMapper;
import com.fandesunstar.eship.vo.ShipListVO;
import com.fandesunstar.eship.vo.ShipVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 船舶服务类
 */
@Slf4j
@Service
public class ShipService {

    private final ShipMapper shipMapper;
    private final UserMapper userMapper;

    public ShipService(ShipMapper shipMapper, UserMapper userMapper) {
        this.shipMapper = shipMapper;
        this.userMapper = userMapper;
    }

    /**
     * 添加船舶
     */
    @Transactional
    public ShipVO addShip(Long ownerId, ShipDTO dto) {
        // 验证船东存在
        User owner = userMapper.selectById(ownerId);
        if (owner == null) {
            throw new BusinessException("船东不存在");
        }

        // 创建船舶
        Ship ship = new Ship();
        BeanUtils.copyProperties(dto, ship);
        ship.setOwnerId(ownerId);
        ship.setStatus("AVAILABLE");
        ship.setRating(BigDecimal.valueOf(5.0)); // 初始评分5.0

        shipMapper.insert(ship);

        log.info("船舶添加成功：船舶ID={}, 船东ID={}, 船舶名称={}", ship.getId(), ownerId, ship.getShipName());

        return getShipDetail(ship.getId());
    }

    /**
     * 修改船舶
     */
    @Transactional
    public ShipVO updateShip(Long shipId, Long ownerId, ShipDTO dto) {
        // 验证船舶存在且属于该船东
        Ship ship = shipMapper.selectById(shipId);
        if (ship == null) {
            throw new BusinessException("船舶不存在");
        }
        if (!ship.getOwnerId().equals(ownerId)) {
            throw new BusinessException("无权修改此船舶");
        }

        // 更新船舶信息
        BeanUtils.copyProperties(dto, ship);
        ship.setId(shipId);
        shipMapper.updateById(ship);

        log.info("船舶修改成功：船舶ID={}, 船东ID={}", shipId, ownerId);

        return getShipDetail(shipId);
    }

    /**
     * 删除船舶
     */
    @Transactional
    public void deleteShip(Long shipId, Long ownerId) {
        // 验证船舶存在且属于该船东
        Ship ship = shipMapper.selectById(shipId);
        if (ship == null) {
            throw new BusinessException("船舶不存在");
        }
        if (!ship.getOwnerId().equals(ownerId)) {
            throw new BusinessException("无权删除此船舶");
        }

        // 检查船舶状态
        if ("RENTED".equals(ship.getStatus())) {
            throw new BusinessException("船舶正在租赁中，无法删除");
        }

        shipMapper.deleteById(shipId);

        log.info("船舶删除成功：船舶ID={}, 船东ID={}", shipId, ownerId);
    }

    /**
     * 我的船舶列表
     */
    public IPage<ShipListVO> getMyShips(Long ownerId, Integer page, Integer size, String shipType) {
        Page<Ship> pageParam = new Page<>(page, size);
        IPage<Ship> shipPage;

        if (shipType != null && !shipType.isEmpty()) {
            shipPage = shipMapper.selectByOwnerIdAndType(pageParam, ownerId, shipType);
        } else {
            shipPage = shipMapper.selectByOwnerId(pageParam, ownerId);
        }

        return shipPage.convert(this::convertToShipListVO);
    }

    /**
     * 船舶详情
     */
    public ShipVO getShipDetail(Long shipId) {
        Ship ship = shipMapper.selectById(shipId);
        if (ship == null) {
            throw new BusinessException("船舶不存在");
        }

        ShipVO shipVO = new ShipVO();
        BeanUtils.copyProperties(ship, shipVO);

        // 获取船东信息
        User owner = userMapper.selectById(ship.getOwnerId());
        if (owner != null) {
            shipVO.setOwnerName(owner.getUsername());
            shipVO.setOwnerCreditScore(owner.getCreditScore());
        }

        return shipVO;
    }

    /**
     * 出租中的船舶
     */
    public IPage<ShipListVO> getRentedShips(Long ownerId, Integer page, Integer size) {
        Page<Ship> pageParam = new Page<>(page, size);
        IPage<Ship> shipPage = shipMapper.selectByOwnerId(pageParam, ownerId);

        // 过滤出租中的船舶
        return shipPage.convert(ship -> {
            if ("RENTED".equals(ship.getStatus())) {
                return convertToShipListVO(ship);
            }
            return null;
        });
    }

    /**
     * 搜索船舶
     */
    public IPage<ShipListVO> searchShips(ShipSearchDTO dto) {
        Page<Ship> pageParam = new Page<>(dto.getPage(), dto.getSize());
        IPage<Ship> shipPage = shipMapper.searchShips(
            pageParam,
            dto.getShipType(),
            dto.getMinRent(),
            dto.getMaxRent(),
            dto.getMinTonnage(),
            dto.getMaxTonnage(),
            dto.getKeyword(),
            dto.getSortBy()
        );

        return shipPage.convert(this::convertToShipListVO);
    }

    /**
     * 推荐船舶（高评分）
     */
    public List<ShipListVO> getRecommendedShips(Integer limit) {
        List<Ship> ships = shipMapper.selectTopRatedShips(limit);
        return ships.stream()
            .map(this::convertToShipListVO)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 可租赁船舶
     */
    public IPage<ShipListVO> getAvailableShips(Integer page, Integer size) {
        Page<Ship> pageParam = new Page<>(page, size);
        IPage<Ship> shipPage = shipMapper.selectAvailableShips(pageParam);

        return shipPage.convert(this::convertToShipListVO);
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
