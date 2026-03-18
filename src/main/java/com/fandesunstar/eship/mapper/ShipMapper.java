package com.fandesunstar.eship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fandesunstar.eship.entity.Ship;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 船舶Mapper接口
 */
@Mapper
public interface ShipMapper extends BaseMapper<Ship> {

    /**
     * 根据船东ID查询船舶列表（分页）
     */
    IPage<Ship> selectByOwnerId(Page<Ship> page, @Param("ownerId") Long ownerId);

    /**
     * 根据船东ID和船舶类型查询船舶列表（分页）
     */
    IPage<Ship> selectByOwnerIdAndType(Page<Ship> page,
                                       @Param("ownerId") Long ownerId,
                                       @Param("shipType") String shipType);

    /**
     * 查询可租赁的船舶（分页）
     */
    IPage<Ship> selectAvailableShips(Page<Ship> page);

    /**
     * 多条件搜索船舶（分页）
     */
    IPage<Ship> searchShips(Page<Ship> page,
                           @Param("shipType") String shipType,
                           @Param("minRent") BigDecimal minRent,
                           @Param("maxRent") BigDecimal maxRent,
                           @Param("minTonnage") BigDecimal minTonnage,
                           @Param("maxTonnage") BigDecimal maxTonnage,
                           @Param("keyword") String keyword,
                           @Param("sortBy") String sortBy);

    /**
     * 根据评分排序查询船舶（用于推荐）
     */
    List<Ship> selectTopRatedShips(@Param("limit") Integer limit);

    /**
     * 更新船舶状态
     */
    int updateStatus(@Param("shipId") Long shipId, @Param("status") String status);

    /**
     * 更新船舶评分
     */
    int updateRating(@Param("shipId") Long shipId, @Param("rating") BigDecimal rating);

    /**
     * 统计船东的船舶数量
     */
    Long countByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 统计总船舶数量
     */
    Long countTotal();

    /**
     * 统计各类型船舶数量
     */
    Long countByType(@Param("shipType") String shipType);

    /**
     * 查询热门船舶类型统计
     */
    List<java.util.Map<String, Object>> selectShipTypeStatistics();

    /**
     * 统计船东的船舶数量（按状态）
     */
    Long countByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") String status);

    /**
     * 统计船东的船舶类型分布
     */
    List<java.util.Map<String, Object>> countByOwnerIdGroupByType(@Param("ownerId") Long ownerId);

    /**
     * 统计租家收藏的船舶数量
     */
    Long countFavoritesByRenterId(@Param("renterId") Long renterId);
}
