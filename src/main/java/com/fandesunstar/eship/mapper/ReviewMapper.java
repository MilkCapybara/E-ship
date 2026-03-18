package com.fandesunstar.eship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fandesunstar.eship.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评价Mapper接口
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /**
     * 查询用户收到的评价
     */
    List<Review> selectByRevieweeId(@Param("revieweeId") Long revieweeId);

    /**
     * 查询合约的评价
     */
    Review selectByContractId(@Param("contractId") Long contractId);

    /**
     * 计算用户的平均评分
     */
    Double calculateAverageRating(@Param("revieweeId") Long revieweeId);
}
