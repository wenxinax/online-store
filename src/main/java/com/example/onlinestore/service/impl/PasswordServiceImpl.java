package com.example.onlinestore.service.impl;

import com.example.onlinestore.service.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 密码服务实现类
 * 使用Spring Security的BCrypt密码编码器
 */
@Service
public class PasswordServiceImpl implements PasswordService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordServiceImpl.class);
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public String encodePassword(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            throw new IllegalArgumentException("原始密码不能为空");
        }
        
        try {
            String encodedPassword = passwordEncoder.encode(rawPassword);
            logger.debug("密码加密成功");
            return encodedPassword;
        } catch (Exception e) {
            logger.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(encodedPassword)) {
            logger.debug("密码验证失败：密码为空");
            return false;
        }
        
        try {
            boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
            logger.debug("密码验证结果: {}", matches ? "成功" : "失败");
            return matches;
        } catch (Exception e) {
            logger.error("密码验证过程中发生异常", e);
            return false;
        }
    }
}