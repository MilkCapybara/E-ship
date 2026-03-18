package com.fandesunstar.eship.utils;

import java.util.Random;

/**
 * 验证码工具类
 */
public class VerificationCodeUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    /**
     * 生成指定长度的验证码
     */
    public static String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    /**
     * 生成4位验证码
     */
    public static String generateCode() {
        return generateCode(4);
    }
}
