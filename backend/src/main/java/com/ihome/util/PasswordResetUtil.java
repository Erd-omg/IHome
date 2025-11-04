package com.ihome.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码重置工具类
 * 用于生成BCrypt加密的密码
 */
public class PasswordResetUtil {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成123456的BCrypt加密
        String studentPassword = encoder.encode("123456");
        System.out.println("学生密码(123456)的BCrypt加密: " + studentPassword);
        
        // 生成admin123的BCrypt加密
        String adminPassword = encoder.encode("admin123");
        System.out.println("管理员密码(admin123)的BCrypt加密: " + adminPassword);
        
        // 验证密码
        System.out.println("验证123456: " + encoder.matches("123456", studentPassword));
        System.out.println("验证admin123: " + encoder.matches("admin123", adminPassword));
    }
}


