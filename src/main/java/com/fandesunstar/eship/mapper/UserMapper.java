package com.fandesunstar.eship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fandesunstar.eship.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    User selectByEmail(@Param("email") String email);

    /**
     * 更新登录失败次数
     */
    int updateFailedLoginAttempts(@Param("userId") Long userId, @Param("attempts") Integer attempts);

    /**
     * 锁定用户账号
     */
    int lockUser(@Param("userId") Long userId, @Param("lockUntil") java.time.LocalDateTime lockUntil);

    /**
     * 解锁用户账号
     */
    int unlockUser(@Param("userId") Long userId);

    /**
     * 重置登录失败次数
     */
    int resetFailedLoginAttempts(@Param("userId") Long userId);

    /**
     * 根据角色查询用户列表
     */
    List<User> selectByRole(@Param("role") String role);

    /**
     * 查询信用评分高的租家（用于船东推荐）
     */
    List<User> selectTopRentersByCredit(@Param("limit") Integer limit);

    /**
     * 更新用户信用评分
     */
    int updateCreditScore(@Param("userId") Long userId, @Param("creditScore") Integer creditScore);

    /**
     * 更新用户状态
     */
    int updateStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 统计用户数量（按角色）
     */
    Long countByRole(@Param("role") String role);

    /**
     * 统计总用户数
     */
    Long countTotal();
}
