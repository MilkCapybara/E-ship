package com.fandesunstar.eship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fandesunstar.eship.entity.ShipFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 船舶收藏Mapper接口
 */
@Mapper
public interface ShipFavoriteMapper extends BaseMapper<ShipFavorite> {

    /**
     * 查询用户收藏的船舶ID列表
     */
    List<Long> selectShipIdsByUserId(@Param("userId") Long userId);

    /**
     * 检查是否已收藏
     */
    ShipFavorite selectByUserIdAndShipId(@Param("userId") Long userId, @Param("shipId") Long shipId);

    /**
     * 取消收藏
     */
    int deleteByUserIdAndShipId(@Param("userId") Long userId, @Param("shipId") Long shipId);
}
