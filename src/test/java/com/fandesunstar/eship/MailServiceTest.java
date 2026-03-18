package com.fandesunstar.eship;

import com.fandesunstar.eship.service.MailService;
import com.fandesunstar.eship.utils.VerificationCodeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 邮件服务测试类
 */
@SpringBootTest
public class MailServiceTest {

    @Autowired
    private MailService mailService;

    /**
     * 测试发送简单文本邮件
     */
    @Test
    public void testSendSimpleMail() {
        String to = "202310121257@stu.shmtu.edu.cn";
        String subject = "E-ship 测试邮件";
        String content = "这是一个测试邮件";

        mailService.sendSimpleMail(to, subject, content);
        System.out.println("测试邮件发送成功！");
    }

    /**
     * 测试发送HTML邮件
     */
    @Test
    public void testSendHtmlMail() {
        String to = "202310121257@stu.shmtu.edu.cn";
        String subject = "E-ship HTML测试邮件";
        String content = "<h1 style='color: #1890ff;'>这是一个HTML测试邮件</h1>" +
                        "<p>欢迎使用E-ship船舶租赁平台！</p>";

        mailService.sendHtmlMail(to, subject, content);
        System.out.println("HTML测试邮件发送成功！");
    }

    /**
     * 测试发送验证码邮件（文本版）
     */
    @Test
    public void testSendVerificationCode() {
        String to = "202310121257@stu.shmtu.edu.cn";
        String code = VerificationCodeUtil.generateCode();

        mailService.sendVerificationCode(to, code);
        System.out.println("验证码邮件发送成功！验证码：" + code);
    }

    /**
     * 测试发送验证码邮件（HTML版）
     */
    @Test
    public void testSendVerificationCodeHtml() {
        String to = "202310121257@stu.shmtu.edu.cn";
        String code = VerificationCodeUtil.generateCode();

        mailService.sendVerificationCodeHtml(to, code);
        System.out.println("HTML验证码邮件发送成功！验证码：" + code);
    }
}
