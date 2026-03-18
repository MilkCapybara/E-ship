package com.fandesunstar.eship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fandesunstar.eship.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录日志Mapper接口
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    /**
     * 查询用户的登录日志
     */
    List<LoginLog> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 统计用户登录失败次数（最近1小时内）
     */
    Long countRecentFailedAttempts(@Param("username") String username, @Param("hours") Integer hours);
}
