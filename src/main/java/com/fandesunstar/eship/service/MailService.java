package com.fandesunstar.eship.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 邮件服务类
 */
@Slf4j
@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送简单文本邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public void sendSimpleMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("简单邮件发送成功：收件人={}, 主题={}", to, subject);
        } catch (Exception e) {
            log.error("简单邮件发送失败：收件人={}, 错误信息={}", to, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送HTML邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content HTML内容
     */
    public void sendHtmlMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("HTML邮件发送成功：收件人={}, 主题={}", to, subject);
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败：收件人={}, 错误信息={}", to, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送带附件的邮件
     *
     * @param to           收件人邮箱
     * @param subject      邮件主题
     * @param content      邮件内容
     * @param attachmentName 附件名称
     * @param attachmentData 附件数据
     */
    public void sendMailWithAttachment(String to, String subject, String content,
                                      String attachmentName, byte[] attachmentData) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            // 添加附件
            helper.addAttachment(attachmentName, new ByteArrayResource(attachmentData));

            mailSender.send(message);
            log.info("带附件邮件发送成功：收件人={}, 主题={}, 附件={}", to, subject, attachmentName);
        } catch (MessagingException e) {
            log.error("带附件邮件发送失败：收件人={}, 错误信息={}", to, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送验证码邮件
     *
     * @param to   收件人邮箱
     * @param code 验证码
     */
    public void sendVerificationCode(String to, String code) {
        String subject = "E-ship 验证码";
        String content = String.format(
            "您好！\n\n" +
            "您的验证码是：%s\n\n" +
            "验证码有效期为10分钟，请尽快使用。\n\n" +
            "如果这不是您的操作，请忽略此邮件。\n\n" +
            "E-ship 船舶租赁平台",
            code
        );
        sendSimpleMail(to, subject, content);
    }

    /**
     * 发送验证码邮件（HTML版本）
     *
     * @param to   收件人邮箱
     * @param code 验证码
     */
    public void sendVerificationCodeHtml(String to, String code) {
        String subject = "E-ship 验证码";
        String content = String.format(
            "<html>" +
            "<body style='font-family: Arial, sans-serif;'>" +
            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>" +
            "<h2 style='color: #1890ff;'>E-ship 船舶租赁平台</h2>" +
            "<p>您好！</p>" +
            "<p>您的验证码是：</p>" +
            "<div style='background-color: #f5f5f5; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #1890ff; border-radius: 5px;'>" +
            "%s" +
            "</div>" +
            "<p style='color: #999; font-size: 14px; margin-top: 20px;'>验证码有效期为10分钟，请尽快使用。</p>" +
            "<p style='color: #999; font-size: 14px;'>如果这不是您的操作，请忽略此邮件。</p>" +
            "<hr style='border: none; border-top: 1px solid #ddd; margin: 20px 0;'>" +
            "<p style='color: #999; font-size: 12px; text-align: center;'>E-ship 船舶租赁平台 © 2026</p>" +
            "</div>" +
            "</body>" +
            "</html>",
            code
        );
        sendHtmlMail(to, subject, content);
    }

    /**
     * 发送合约PDF邮件
     *
     * @param to           收件人邮箱
     * @param contractNo   合约编号
     * @param shipName     船舶名称
     * @param pdfData      PDF数据
     */
    public void sendContractPdf(String to, String contractNo, String shipName, byte[] pdfData) {
        String subject = "【E-ship】船舶租赁合同 - " + shipName;
        String content = String.format(
            "<html>" +
            "<body style='font-family: Arial, sans-serif;'>" +
            "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>" +
            "<h2 style='color: #1890ff;'>E-ship 船舶租赁平台</h2>" +
            "<p>尊敬的用户，您好！</p>" +
            "<p>您的船舶租赁合同已生成，详情如下：</p>" +
            "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 15px 0;'>" +
            "<p style='margin: 5px 0;'><strong>合同编号：</strong>%s</p>" +
            "<p style='margin: 5px 0;'><strong>船舶名称：</strong>%s</p>" +
            "<p style='margin: 5px 0;'><strong>生成时间：</strong>%s</p>" +
            "</div>" +
            "<p>请查收附件中的合同PDF文件，请妥善保管。</p>" +
            "<p style='color: #ff4d4f; font-weight: bold;'>重要提示：</p>" +
            "<ul style='color: #666;'>" +
            "<li>请仔细阅读合同条款，确保理解所有内容</li>" +
            "<li>合同已具备法律效力，请按约履行</li>" +
            "<li>如有疑问，请及时联系平台客服</li>" +
            "</ul>" +
            "<hr style='border: none; border-top: 1px solid #ddd; margin: 20px 0;'>" +
            "<p style='color: #999; font-size: 12px;'>E-ship 船舶租赁平台</p>" +
            "<p style='color: #999; font-size: 12px;'>客服邮箱：188043648@qq.com</p>" +
            "<p style='color: #999; font-size: 12px; text-align: center;'>© 2026 E-ship. All rights reserved.</p>" +
            "</div>" +
            "</body>" +
            "</html>",
            contractNo,
            shipName,
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        String fileName = "船舶租赁合同_" + contractNo + ".pdf";
        sendMailWithAttachment(to, subject, content, fileName, pdfData);
    }
}
