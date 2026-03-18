package com.fandesunstar.eship.service;

import com.fandesunstar.eship.entity.VerificationCode;
import com.fandesunstar.eship.mapper.VerificationCodeMapper;
import com.fandesunstar.eship.utils.VerificationCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 验证码服务类
 */
@Slf4j
@Service
public class VerificationCodeService {

    private final VerificationCodeMapper verificationCodeMapper;
    private final MailService mailService;

    public VerificationCodeService(VerificationCodeMapper verificationCodeMapper, MailService mailService) {
        this.verificationCodeMapper = verificationCodeMapper;
        this.mailService = mailService;
    }

    /**
     * 发送验证码
     *
     * @param email 邮箱
     * @param type  验证码类型：REGISTER/LOGIN/RESET_PASSWORD
     */
    @Transactional
    public void sendVerificationCode(String email, String type) {
        // 生成验证码
        String code = VerificationCodeUtil.generateCode();

        // 设置过期时间（10分钟）
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(10);

        // 保存到数据库
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setType(type);
        verificationCode.setExpireAt(expireAt);
        verificationCode.setUsed(false);

        verificationCodeMapper.insert(verificationCode);

        // 发送邮件
        mailService.sendVerificationCodeHtml(email, code);

        log.info("验证码发送成功：邮箱={}, 类型={}, 验证码={}", email, type, code);
    }

    /**
     * 验证验证码
     *
     * @param email 邮箱
     * @param code  验证码
     * @param type  验证码类型
     * @return 是否验证成功
     */
    @Transactional
    public boolean verifyCode(String email, String code, String type) {
        // 查询最新的未使用的验证码
        VerificationCode verificationCode = verificationCodeMapper.selectLatestUnusedCode(email, type);

        if (verificationCode == null) {
            log.warn("验证码不存在：邮箱={}, 类型={}", email, type);
            return false;
        }

        // 检查是否已使用
        if (verificationCode.getUsed()) {
            log.warn("验证码已使用：邮箱={}, 类型={}", email, type);
            return false;
        }

        // 检查是否过期
        if (LocalDateTime.now().isAfter(verificationCode.getExpireAt())) {
            log.warn("验证码已过期：邮箱={}, 类型={}", email, type);
            return false;
        }

        // 检查验证码是否正确
        if (!code.equals(verificationCode.getCode())) {
            log.warn("验证码错误：邮箱={}, 类型={}, 输入={}, 正确={}", email, type, code, verificationCode.getCode());
            return false;
        }

        // 标记为已使用
        verificationCodeMapper.markAsUsed(verificationCode.getId());

        log.info("验证码验证成功：邮箱={}, 类型={}", email, type);
        return true;
    }

    /**
     * 删除过期的验证码
     */
    @Transactional
    public void deleteExpiredCodes() {
        int count = verificationCodeMapper.deleteExpiredCodes();
        log.info("删除过期验证码：数量={}", count);
    }
}
