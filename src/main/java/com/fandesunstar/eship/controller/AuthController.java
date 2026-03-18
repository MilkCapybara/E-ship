package com.fandesunstar.eship.controller;

import com.fandesunstar.eship.common.result.Result;
import com.fandesunstar.eship.dto.ChangePasswordDTO;
import com.fandesunstar.eship.dto.LoginDTO;
import com.fandesunstar.eship.dto.RegisterDTO;
import com.fandesunstar.eship.dto.ResetPasswordDTO;
import com.fandesunstar.eship.dto.SendCodeDTO;
import com.fandesunstar.eship.service.UserService;
import com.fandesunstar.eship.service.VerificationCodeService;
import com.fandesunstar.eship.vo.LoginVO;
import com.fandesunstar.eship.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final VerificationCodeService verificationCodeService;

    public AuthController(UserService userService, VerificationCodeService verificationCodeService) {
        this.userService = userService;
        this.verificationCodeService = verificationCodeService;
    }

    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Result<String> sendCode(@Valid @RequestBody SendCodeDTO sendCodeDTO) {
        verificationCodeService.sendVerificationCode(sendCodeDTO.getEmail(), sendCodeDTO.getType());
        return Result.success("验证码已发送，请查收邮件", null);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        UserVO userVO = userService.register(registerDTO);
        return Result.success("注册成功", userVO);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String ipAddress = getIpAddress(request);
        LoginVO loginVO = userService.login(loginDTO, ipAddress);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(changePasswordDTO);
        return Result.success("密码修改成功", null);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user-info")
    public Result<UserVO> getUserInfo(@RequestParam String username) {
        UserVO userVO = userService.getUserByUsername(username);
        return Result.success(userVO);
    }

    /**
     * 通过用户名获取邮箱（用于发送登录验证码）
     */
    @GetMapping("/get-email")
    public Result<String> getEmailByUsername(@RequestParam String username) {
        String email = userService.getEmailByUsername(username);
        return Result.success("获取成功", email);
    }

    /**
     * 重置密码（忘记密码）
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        userService.resetPassword(resetPasswordDTO);
        return Result.success("密码重置成功", null);
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
