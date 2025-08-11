package com.example.onlinestore.service;

/**
 * 密码服务接口
 * 提供密码加密和验证功能
 */
public interface PasswordService {
    
    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    String encodePassword(String rawPassword);
    
    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean matches(String rawPassword, String encodedPassword);
}