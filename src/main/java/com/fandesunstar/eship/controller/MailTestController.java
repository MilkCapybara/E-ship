package com.fandesunstar.eship.controller;

import com.fandesunstar.eship.common.result.Result;
import com.fandesunstar.eship.service.MailService;
import com.fandesunstar.eship.utils.VerificationCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/test/mail")
public class MailTestController {

    @Autowired
    private MailService mailService;

    /**
     * 发送简单测试邮件
     * 访问：http://localhost:3473/api/test/mail/send-simple
     */
    @GetMapping("/send-simple")
    public Result<String> sendSimpleMail() {
        try {
            String to = "202310121257@stu.shmtu.edu.cn";
            String subject = "E-ship 测试邮件";
            String content = "这是一个测试邮件";

            mailService.sendSimpleMail(to, subject, content);
            return Result.success("邮件发送成功！收件人：" + to);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            return Result.error("邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送HTML测试邮件
     * 访问：http://localhost:3473/api/test/mail/send-html
     */
    @GetMapping("/send-html")
    public Result<String> sendHtmlMail() {
        try {
            String to = "202310121257@stu.shmtu.edu.cn";
            String subject = "E-ship HTML测试邮件";
            String content = "<h1 style='color: #1890ff;'>这是一个HTML测试邮件</h1>" +
                           "<p>欢迎使用E-ship船舶租赁平台！</p>" +
                           "<p style='color: #999;'>这是一封测试邮件，用于验证邮件服务是否正常工作。</p>";

            mailService.sendHtmlMail(to, subject, content);
            return Result.success("HTML邮件发送成功！收件人：" + to);
        } catch (Exception e) {
            log.error("HTML邮件发送失败", e);
            return Result.error("HTML邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送验证码邮件（文本版）
     * 访问：http://localhost:3473/api/test/mail/send-code
     */
    @GetMapping("/send-code")
    public Result<String> sendVerificationCode() {
        try {
            String to = "202310121257@stu.shmtu.edu.cn";
            String code = VerificationCodeUtil.generateCode();

            mailService.sendVerificationCode(to, code);
            return Result.success("验证码邮件发送成功！验证码：" + code + "，收件人：" + to);
        } catch (Exception e) {
            log.error("验证码邮件发送失败", e);
            return Result.error("验证码邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送验证码邮件（HTML版）
     * 访问：http://localhost:3473/api/test/mail/send-code-html
     */
    @GetMapping("/send-code-html")
    public Result<String> sendVerificationCodeHtml() {
        try {
            String to = "202310121257@stu.shmtu.edu.cn";
            String code = VerificationCodeUtil.generateCode();

            mailService.sendVerificationCodeHtml(to, code);
            return Result.success("HTML验证码邮件发送成功！验证码：" + code + "，收件人：" + to);
        } catch (Exception e) {
            log.error("HTML验证码邮件发送失败", e);
            return Result.error("HTML验证码邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送邮件到指定邮箱
     * 访问：http://localhost:3473/api/test/mail/send?to=xxx@xxx.com&content=测试内容
     */
    @GetMapping("/send")
    public Result<String> sendMailToCustom(
            @RequestParam String to,
            @RequestParam(defaultValue = "这是一个测试邮件") String content) {
        try {
            String subject = "E-ship 测试邮件";
            mailService.sendSimpleMail(to, subject, content);
            return Result.success("邮件发送成功！收件人：" + to + "，内容：" + content);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            return Result.error("邮件发送失败：" + e.getMessage());
        }
    }
}
