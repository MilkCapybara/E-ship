package com.fandesunstar.eship.service;

import com.fandesunstar.eship.common.exception.BusinessException;
import com.fandesunstar.eship.dto.ChangePasswordDTO;
import com.fandesunstar.eship.dto.LoginDTO;
import com.fandesunstar.eship.dto.RegisterDTO;
import com.fandesunstar.eship.dto.ResetPasswordDTO;
import com.fandesunstar.eship.entity.LoginLog;
import com.fandesunstar.eship.entity.User;
import com.fandesunstar.eship.mapper.LoginLogMapper;
import com.fandesunstar.eship.mapper.UserMapper;
import com.fandesunstar.eship.utils.PasswordUtil;
import com.fandesunstar.eship.vo.LoginVO;
import com.fandesunstar.eship.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户服务类
 */
@Slf4j
@Service
public class UserService {

    private final UserMapper userMapper;
    private final VerificationCodeService verificationCodeService;
    private final LoginLogMapper loginLogMapper;

    public UserService(UserMapper userMapper, VerificationCodeService verificationCodeService, LoginLogMapper loginLogMapper) {
        this.userMapper = userMapper;
        this.verificationCodeService = verificationCodeService;
        this.loginLogMapper = loginLogMapper;
    }

    /**
     * 用户注册
     */
    @Transactional
    public UserVO register(RegisterDTO registerDTO) {
        // 1. 验证验证码
        boolean codeValid = verificationCodeService.verifyCode(
            registerDTO.getEmail(),
            registerDTO.getCode(),
            "REGISTER"
        );
        if (!codeValid) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 2. 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 3. 检查邮箱是否已注册
        User existingEmail = userMapper.selectByEmail(registerDTO.getEmail());
        if (existingEmail != null) {
            throw new BusinessException("邮箱已被注册");
        }

        // 4. 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setRole(registerDTO.getRole());
        user.setCompanyName(registerDTO.getCompanyName());
        user.setCreditScore(100); // 初始信用分100
        user.setStatus("ACTIVE");
        user.setFailedLoginAttempts(0);

        userMapper.insert(user);

        log.info("用户注册成功：用户名={}, 邮箱={}, 角色={}", user.getUsername(), user.getEmail(), user.getRole());

        // 5. 返回用户信息
        return convertToUserVO(user);
    }

    /**
     * 用户登录
     */
    @Transactional
    public LoginVO login(LoginDTO loginDTO, String ipAddress) {
        // 1. 验证验证码
        User user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        boolean codeValid = verificationCodeService.verifyCode(
            user.getEmail(),
            loginDTO.getCode(),
            "LOGIN"
        );
        if (!codeValid) {
            // 记录登录失败
            recordLoginLog(user.getId(), user.getUsername(), ipAddress, "FAILED", "验证码错误");
            throw new BusinessException("验证码错误或已过期");
        }

        // 2. 检查账号状态
        if ("LOCKED".equals(user.getStatus())) {
            if (user.getLockUntil() != null && LocalDateTime.now().isBefore(user.getLockUntil())) {
                throw new BusinessException("账号已被锁定，请稍后再试");
            } else {
                // 锁定时间已过，解锁账号
                userMapper.unlockUser(user.getId());
                user.setStatus("ACTIVE");
            }
        }

        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 3. 验证密码
        if (!PasswordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            // 增加失败次数
            int attempts = user.getFailedLoginAttempts() + 1;
            userMapper.updateFailedLoginAttempts(user.getId(), attempts);

            // 如果失败5次，锁定1小时
            if (attempts >= 5) {
                LocalDateTime lockUntil = LocalDateTime.now().plusHours(1);
                userMapper.lockUser(user.getId(), lockUntil);
                recordLoginLog(user.getId(), user.getUsername(), ipAddress, "FAILED", "密码错误，账号已锁定");
                throw new BusinessException("密码错误次数过多，账号已被锁定1小时");
            }

            recordLoginLog(user.getId(), user.getUsername(), ipAddress, "FAILED", "密码错误");
            throw new BusinessException("用户名或密码错误");
        }

        // 4. 登录成功，重置失败次数
        userMapper.resetFailedLoginAttempts(user.getId());

        // 5. 生成Token（这里使用UUID模拟，实际项目应使用JWT）
        String token = UUID.randomUUID().toString().replace("-", "");

        // 6. 记录登录日志
        recordLoginLog(user.getId(), user.getUsername(), ipAddress, "SUCCESS", "登录成功");

        log.info("用户登录成功：用户名={}, IP={}", user.getUsername(), ipAddress);

        // 7. 返回登录信息
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(convertToUserVO(user));

        return loginVO;
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        // 1. 验证验证码
        boolean codeValid = verificationCodeService.verifyCode(
            changePasswordDTO.getEmail(),
            changePasswordDTO.getCode(),
            "RESET_PASSWORD"
        );
        if (!codeValid) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 2. 查询用户
        User user = userMapper.selectByUsername(changePasswordDTO.getUsername());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 验证邮箱是否匹配
        if (!user.getEmail().equals(changePasswordDTO.getEmail())) {
            throw new BusinessException("邮箱与用户名不匹配");
        }

        // 4. 更新密码
        user.setPassword(PasswordUtil.encode(changePasswordDTO.getNewPassword()));
        userMapper.updateById(user);

        // 5. 重置失败次数和解锁账号
        userMapper.resetFailedLoginAttempts(user.getId());
        if ("LOCKED".equals(user.getStatus())) {
            userMapper.unlockUser(user.getId());
        }

        log.info("密码修改成功：用户名={}, 邮箱={}", user.getUsername(), user.getEmail());
    }

    /**
     * 根据用户名获取用户信息
     */
    public UserVO getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToUserVO(user);
    }

    /**
     * 通过用户名获取邮箱
     */
    public String getEmailByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user.getEmail();
    }

    /**
     * 重置密码（忘记密码）
     */
    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // 1. 验证验证码
        boolean codeValid = verificationCodeService.verifyCode(
            resetPasswordDTO.getEmail(),
            resetPasswordDTO.getCode(),
            "RESET_PASSWORD"
        );
        if (!codeValid) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 2. 查询用户
        User user = userMapper.selectByEmail(resetPasswordDTO.getEmail());
        if (user == null) {
            throw new BusinessException("该邮箱未注册");
        }

        // 3. 更新密码
        user.setPassword(PasswordUtil.encode(resetPasswordDTO.getNewPassword()));
        userMapper.updateById(user);

        // 4. 重置失败次数和解锁账号
        userMapper.resetFailedLoginAttempts(user.getId());
        if ("LOCKED".equals(user.getStatus())) {
            userMapper.unlockUser(user.getId());
        }

        log.info("密码重置成功：邮箱={}", user.getEmail());
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(Long userId, String username, String ipAddress, String status, String message) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setIpAddress(ipAddress);
            loginLog.setStatus(status);
            loginLog.setFailReason(message);
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("记录登录日志失败：用户ID={}, 错误={}", userId, e.getMessage());
        }
    }

    /**
     * 转换为UserVO
     */
    private UserVO convertToUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}
