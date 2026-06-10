package com.yupe.siyun.util;

import org.springframework.stereotype.Component;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * 密码加密工具类
 */
@Component
public class SafeUtil {

    private static final String ALGORITHM = "SHA-256";

    /**
     * 对密码进行加密，使用盐值增强安全性
     * @param password 原始密码
     * @return 加密后的密码(格式: salt$hash)
     */
    public String transPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new MyException(ErrorType.WRONG_INFO, "密码不能为空");
        }
        
        // 生成随机盐值
        String salt = UUID.randomUUID().toString();
        
        // 将密码和盐值进行哈希处理
        String hash = hashPassword(password, salt);
        
        // 返回 salt$hash 的格式
        return salt + "$" + hash;
    }

    /**
     * 验证密码是否正确
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码 (格式: salt$hash)
     * @return 密码是否匹配
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        try {
            // 解析存储的密码，提取盐值和哈希值
            String[] parts = encodedPassword.split("\\$");
            if (parts.length != 2) {
                return false;
            }
            
            String salt = parts[0];
            String storedHash = parts[1];
            
            // 用相同的盐值对输入密码进行哈希
            String inputHash = hashPassword(rawPassword, salt);
            
            // 比较哈希值
            return storedHash.equals(inputHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 使用盐值对密码进行SHA-256哈希
     * @param password 密码
     * @param salt 盐值
     * @return 哈希值
     */
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            String input = password + salt;
            byte[] messageDigest = md.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败: " + e.getMessage());
        }
    }
}

