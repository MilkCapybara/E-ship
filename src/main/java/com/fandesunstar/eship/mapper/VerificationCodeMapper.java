package com.fandesunstar.eship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fandesunstar.eship.entity.VerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 验证码Mapper接口
 */
@Mapper
public interface VerificationCodeMapper extends BaseMapper<VerificationCode> {

    /**
     * 查询最新的未使用验证码
     */
    VerificationCode selectLatestUnusedCode(@Param("email") String email, @Param("type") String type);

    /**
     * 标记验证码为已使用
     */
    int markAsUsed(@Param("id") Long id);

    /**
     * 删除过期的验证码
     */
    int deleteExpiredCodes();

    /**
     * 查询10分钟内是否已发送验证码
     */
    VerificationCode selectRecentCode(@Param("email") String email, @Param("type") String type);
}
