package com.fandesunstar.eship.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 测试方法：生成管理员密码
     */
    public static void main(String[] args) {
        String adminPassword = "twhd#7?n*Csf";
        String encoded = encode(adminPassword);
        System.out.println("管理员密码加密结果：");
        System.out.println(encoded);
        System.out.println();
        System.out.println("验证密码：" + matches(adminPassword, encoded));
    }
}
