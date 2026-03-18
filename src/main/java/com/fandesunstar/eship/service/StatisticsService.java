package com.fandesunstar.eship.service;

import com.fandesunstar.eship.mapper.ContractMapper;
import com.fandesunstar.eship.mapper.ShipMapper;
import com.fandesunstar.eship.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 数据统计服务
 */
@Slf4j
@Service
public class StatisticsService {

    private final ContractMapper contractMapper;
    private final ShipMapper shipMapper;
    private final UserMapper userMapper;

    public StatisticsService(ContractMapper contractMapper, ShipMapper shipMapper, UserMapper userMapper) {
        this.contractMapper = contractMapper;
        this.shipMapper = shipMapper;
        this.userMapper = userMapper;
    }

    /**
     * 获取船东统计数据
     */
    public Map<String, Object> getOwnerStatistics(Long ownerId) {
        Map<String, Object> result = new HashMap<>();

        // 船舶总数
        Long totalShips = shipMapper.countByOwnerId(ownerId);
        result.put("totalShips", totalShips);

        // 出租中的船舶数
        Long rentedShips = shipMapper.countByOwnerIdAndStatus(ownerId, "RENTED");
        result.put("rentedShips", rentedShips);

        // 可租赁船舶数
        Long availableShips = shipMapper.countByOwnerIdAndStatus(ownerId, "AVAILABLE");
        result.put("availableShips", availableShips);

        // 待审核合约数
        Long pendingContracts = contractMapper.countByOwnerIdAndStatus(ownerId, "PENDING");
        result.put("pendingContracts", pendingContracts);

        // 进行中合约数
        Long activeContracts = contractMapper.countByOwnerIdAndStatus(ownerId, "APPROVED");
        result.put("activeContracts", activeContracts);

        // 总收入（已完成和进行中的合约）
        BigDecimal totalIncome = contractMapper.sumTotalAmountByOwnerId(ownerId);
        result.put("totalIncome", totalIncome != null ? totalIncome : BigDecimal.ZERO);

        // 合约状态分布
        List<Map<String, Object>> contractStatusDistribution = new ArrayList<>();
        contractStatusDistribution.add(createStatusMap("待审核", pendingContracts));
        contractStatusDistribution.add(createStatusMap("进行中", activeContracts));
        Long completedContracts = contractMapper.countByOwnerIdAndStatus(ownerId, "COMPLETED");
        contractStatusDistribution.add(createStatusMap("已完成", completedContracts));
        Long rejectedContracts = contractMapper.countByOwnerIdAndStatus(ownerId, "REJECTED");
        contractStatusDistribution.add(createStatusMap("已拒绝", rejectedContracts));
        result.put("contractStatusDistribution", contractStatusDistribution);

        // 船舶类型分布
        List<Map<String, Object>> shipTypeDistribution = shipMapper.countByOwnerIdGroupByType(ownerId);
        result.put("shipTypeDistribution", shipTypeDistribution);

        // 月度收入趋势（最近6个月）
        List<Map<String, Object>> monthlyIncome = contractMapper.getMonthlyIncomeByOwnerId(ownerId, 6);
        result.put("monthlyIncome", monthlyIncome);

        return result;
    }

    /**
     * 获取租家统计数据
     */
    public Map<String, Object> getRenterStatistics(Long renterId) {
        Map<String, Object> result = new HashMap<>();

        // 总合约数
        Long totalContracts = contractMapper.countByRenterIdAndStatus(renterId, null);
        result.put("totalContracts", totalContracts);

        // 待审核合约数
        Long pendingContracts = contractMapper.countByRenterIdAndStatus(renterId, "PENDING");
        result.put("pendingContracts", pendingContracts);

        // 进行中合约数
        Long activeContracts = contractMapper.countByRenterIdAndStatus(renterId, "APPROVED");
        result.put("activeContracts", activeContracts);

        // 已完成合约数
        Long completedContracts = contractMapper.countByRenterIdAndStatus(renterId, "COMPLETED");
        result.put("completedContracts", completedContracts);

        // 总支出
        BigDecimal totalExpense = contractMapper.sumTotalAmountByRenterId(renterId);
        result.put("totalExpense", totalExpense != null ? totalExpense : BigDecimal.ZERO);

        // 收藏船舶数
        Long favoriteShips = shipMapper.countFavoritesByRenterId(renterId);
        result.put("favoriteShips", favoriteShips);

        // 合约状态分布
        List<Map<String, Object>> contractStatusDistribution = new ArrayList<>();
        contractStatusDistribution.add(createStatusMap("待审核", pendingContracts));
        contractStatusDistribution.add(createStatusMap("进行中", activeContracts));
        contractStatusDistribution.add(createStatusMap("已完成", completedContracts));
        Long rejectedContracts = contractMapper.countByRenterIdAndStatus(renterId, "REJECTED");
        contractStatusDistribution.add(createStatusMap("已拒绝", rejectedContracts));
        result.put("contractStatusDistribution", contractStatusDistribution);

        // 月度支出趋势（最近6个月）
        List<Map<String, Object>> monthlyExpense = contractMapper.getMonthlyExpenseByRenterId(renterId, 6);
        result.put("monthlyExpense", monthlyExpense);

        return result;
    }

    /**
     * 获取管理员统计数据
     */
    public Map<String, Object> getAdminStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 用户总数
        Long totalUsers = userMapper.countTotal();
        result.put("totalUsers", totalUsers);

        // 船东数量
        Long ownerCount = userMapper.countByRole("SHIP_OWNER");
        result.put("ownerCount", ownerCount);

        // 租家数量
        Long renterCount = userMapper.countByRole("RENTER");
        result.put("renterCount", renterCount);

        // 船舶总数
        Long totalShips = shipMapper.countTotal();
        result.put("totalShips", totalShips);

        // 合约总数
        Long totalContracts = contractMapper.countTotal();
        result.put("totalContracts", totalContracts);

        // 活跃合约数
        Long activeContracts = contractMapper.countActiveContracts();
        result.put("activeContracts", activeContracts);

        // 平台总交易额
        BigDecimal totalAmount = contractMapper.sumTotalAmount();
        result.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);

        // 船舶类型分布
        List<Map<String, Object>> shipTypeDistribution = shipMapper.selectShipTypeStatistics();
        result.put("shipTypeDistribution", shipTypeDistribution);

        // 用户角色分布
        List<Map<String, Object>> userRoleDistribution = new ArrayList<>();
        userRoleDistribution.add(createStatusMap("船东", ownerCount));
        userRoleDistribution.add(createStatusMap("租家", renterCount));
        result.put("userRoleDistribution", userRoleDistribution);

        // 月度交易统计（最近6个月）
        List<Map<String, Object>> monthlyStatistics = contractMapper.selectMonthlyStatistics(
            java.time.LocalDate.now().getYear()
        );
        result.put("monthlyStatistics", monthlyStatistics);

        return result;
    }

    /**
     * 创建状态映射
     */
    private Map<String, Object> createStatusMap(String name, Long value) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("value", value);
        return map;
    }
}
