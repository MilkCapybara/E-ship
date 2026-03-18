package com.fandesunstar.eship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fandesunstar.eship.entity.Contract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 合约Mapper接口
 */
@Mapper
public interface ContractMapper extends BaseMapper<Contract> {

    /**
     * 查询船东的合约信箱（待审核的合约）
     */
    IPage<Contract> selectOwnerInbox(Page<Contract> page, @Param("ownerId") Long ownerId);

    /**
     * 查询船东的所有合约（分页）
     */
    IPage<Contract> selectByOwnerId(Page<Contract> page, @Param("ownerId") Long ownerId, @Param("status") String status);

    /**
     * 查询租家的所有合约（分页）
     */
    IPage<Contract> selectByRenterId(Page<Contract> page, @Param("renterId") Long renterId);

    /**
     * 查询进行中的合约（按到期日期排序）
     */
    IPage<Contract> selectInProgressContracts(Page<Contract> page, @Param("ownerId") Long ownerId);

    /**
     * 根据船舶ID查询合约列表
     */
    List<Contract> selectByShipId(@Param("shipId") Long shipId);

    /**
     * 查询即将到期的合约（7天内）
     */
    List<Contract> selectExpiringContracts(@Param("days") Integer days);

    /**
     * 更新合约状态
     */
    int updateStatus(@Param("contractId") Long contractId, @Param("status") String status);

    /**
     * 审核合约（同意）
     */
    int approveContract(@Param("contractId") Long contractId);

    /**
     * 审核合约（拒绝）
     */
    int rejectContract(@Param("contractId") Long contractId, @Param("rejectReason") String rejectReason);

    /**
     * 统计船东的合约数量（按状态）
     */
    Long countByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") String status);

    /**
     * 统计租家的合约数量（按状态）
     */
    Long countByRenterIdAndStatus(@Param("renterId") Long renterId, @Param("status") String status);

    /**
     * 统计活跃合约数量
     */
    Long countActiveContracts();

    /**
     * 统计总交易额
     */
    BigDecimal sumTotalAmount();

    /**
     * 查询交易量统计（按月）
     */
    List<java.util.Map<String, Object>> selectMonthlyStatistics(@Param("year") Integer year);

    /**
     * 查询所有合约（管理员用，分页）
     */
    IPage<Contract> selectAllContracts(Page<Contract> page);

    /**
     * 统计船东的总收入
     */
    BigDecimal sumTotalAmountByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 统计租家的总支出
     */
    BigDecimal sumTotalAmountByRenterId(@Param("renterId") Long renterId);

    /**
     * 查询船东的月度收入趋势
     */
    List<java.util.Map<String, Object>> getMonthlyIncomeByOwnerId(@Param("ownerId") Long ownerId, @Param("months") Integer months);

    /**
     * 查询租家的月度支出趋势
     */
    List<java.util.Map<String, Object>> getMonthlyExpenseByRenterId(@Param("renterId") Long renterId, @Param("months") Integer months);

    /**
     * 统计合约总数
     */
    Long countTotal();
}
