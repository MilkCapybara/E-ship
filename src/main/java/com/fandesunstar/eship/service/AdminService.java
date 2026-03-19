package com.fandesunstar.eship.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fandesunstar.eship.common.exception.BusinessException;
import com.fandesunstar.eship.entity.Contract;
import com.fandesunstar.eship.entity.Ship;
import com.fandesunstar.eship.entity.User;
import com.fandesunstar.eship.mapper.ContractMapper;
import com.fandesunstar.eship.mapper.ShipMapper;
import com.fandesunstar.eship.mapper.UserMapper;
import com.fandesunstar.eship.vo.ContractListVO;
import com.fandesunstar.eship.vo.ShipListVO;
import com.fandesunstar.eship.vo.StatisticsVO;
import com.fandesunstar.eship.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务类
 */
@Slf4j
@Service
public class AdminService {

    private final UserMapper userMapper;
    private final ShipMapper shipMapper;
    private final ContractMapper contractMapper;

    public AdminService(UserMapper userMapper, ShipMapper shipMapper, ContractMapper contractMapper) {
        this.userMapper = userMapper;
        this.shipMapper = shipMapper;
        this.contractMapper = contractMapper;
    }

    /**
     * 查询所有用户
     */
    public IPage<UserVO> getAllUsers(Integer page, Integer size, String role) {
        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (role != null && !role.isEmpty()) {
            queryWrapper.eq("role", role);
        }

        IPage<User> userPage = userMapper.selectPage(pageParam, queryWrapper);
        return userPage.convert(this::convertToUserVO);
    }

    /**
     * 更新用户状态
     */
    @Transactional
    public void updateUserStatus(Long userId, String status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setStatus(status);
        userMapper.updateById(user);

        log.info("更新用户状态成功：用户ID={}, 新状态={}", userId, status);
    }

    /**
     * 更新信用评分
     */
    @Transactional
    public void updateCreditScore(Long userId, Integer creditScore) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setCreditScore(creditScore);
        userMapper.updateById(user);

        log.info("更新信用评分成功：用户ID={}, 新评分={}", userId, creditScore);
    }

    /**
     * 用户详情
     */
    public UserVO getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return convertToUserVO(user);
    }

    /**
     * 查询所有船舶
     */
    public IPage<ShipListVO> getAllShips(Integer page, Integer size) {
        Page<Ship> pageParam = new Page<>(page, size);
        IPage<Ship> shipPage = shipMapper.selectPage(pageParam, null);

        return shipPage.convert(this::convertToShipListVO);
    }

    /**
     * 更新船舶状态
     */
    @Transactional
    public void updateShipStatus(Long shipId, String status) {
        Ship ship = shipMapper.selectById(shipId);
        if (ship == null) {
            throw new BusinessException("船舶不存在");
        }

        shipMapper.updateStatus(shipId, status);

        log.info("更新船舶状态成功：船舶ID={}, 新状态={}", shipId, status);
    }

    /**
     * 查询所有合约
     */
    public IPage<ContractListVO> getAllContracts(Integer page, Integer size) {
        Page<Contract> pageParam = new Page<>(page, size);
        IPage<Contract> contractPage = contractMapper.selectAllContracts(pageParam);

        return contractPage.convert(this::convertToContractListVO);
    }

    /**
     * 强制终止合约
     */
    @Transactional
    public void forceTerminateContract(Long contractId, String reason) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new BusinessException("合约不存在");
        }

        // 更新合约状态为已取消
        contractMapper.updateStatus(contractId, "CANCELLED");

        // 如果合约是进行中，需要更新船舶状态为可用
        if ("IN_PROGRESS".equals(contract.getStatus()) || "APPROVED".equals(contract.getStatus())) {
            shipMapper.updateStatus(contract.getShipId(), "AVAILABLE");
        }

        log.info("强制终止合约成功：合约ID=, 原因={}", contractId, reason);
    }

    /**
     * 获取平台统计数据
     */
    public StatisticsVO getStatistics() {
        StatisticsVO statistics = new StatisticsVO();

        // 总用户数
        Long totalUsers = userMapper.selectCount(null);
        statistics.setTotalUsers(totalUsers);

        // 总船舶数
        Long totalShips = shipMapper.countTotal();
        statistics.setTotalShips(totalShips);

        // 活跃合约数
        Long activeContracts = contractMapper.countActiveContracts();
        statistics.setActiveContracts(activeContracts);

        // 总交易额
        BigDecimal totalAmount = contractMapper.sumTotalAmount();
        statistics.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);

        // 按角色统计用户数
        Map<String, Long> usersByRole = new HashMap<>();
        usersByRole.put("SHIP_OWNER", userMapper.selectCount(new QueryWrapper<User>().eq("role", "SHIP_OWNER")));
        usersByRole.put("RENTER", userMapper.selectCount(new QueryWrapper<User>().eq("role", "RENTER")));
        usersByRole.put("ADMIN", userMapper.selectCount(new QueryWrapper<User>().eq("role", "ADMIN")));
        statistics.setUsersByRole(usersByRole);

        // 按类型统计船舶数
        Map<String, Long> shipsByType = new HashMap<>();
        shipsByType.put("CONTAINER", shipMapper.countByType("CONTAINER"));
        shipsByType.put("BULK", shipMapper.countByType("BULK"));
        shipsByType.put("TANKER", shipMapper.countByType("TANKER"));
        shipsByType.put("PASSENGER", shipMapper.countByType("PASSENGER"));
        statistics.setShipsByType(shipsByType);

        // 月度统计数据（当前年份）
        int currentYear = LocalDate.now().getYear();
        List<Map<String, Object>> monthlyStats = contractMapper.selectMonthlyStatistics(currentYear);
        statistics.setMonthlyStatistics(monthlyStats);

        return statistics;
    }

    /**
     * 转换为UserVO
     */
    private UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 转换为ShipListVO
     */
    private ShipListVO convertToShipListVO(Ship ship) {
        ShipListVO vo = new ShipListVO();
        BeanUtils.copyProperties(ship, vo);

        // 获取船东名称
        User owner = userMapper.selectById(ship.getOwnerId());
        if (owner != null) {
            vo.setOwnerName(owner.getUsername());
        }

        return vo;
    }

    /**
     * 转换为ContractListVO
     */
    private ContractListVO convertToContractListVO(Contract contract) {
        ContractListVO vo = new ContractListVO();
        BeanUtils.copyProperties(contract, vo);

        // 获取船舶名称和船东名称
        Ship ship = shipMapper.selectById(contract.getShipId());
        if (ship != null) {
            vo.setShipName(ship.getShipName());

            // 获取船东名称
            User owner = userMapper.selectById(ship.getOwnerId());
            if (owner != null) {
                vo.setOwnerName(owner.getUsername());
            }
        }

        // 获取租家名称
        User renter = userMapper.selectById(contract.getRenterId());
        if (renter != null) {
            vo.setRenterName(renter.getUsername());
        }

        return vo;
    }
}
