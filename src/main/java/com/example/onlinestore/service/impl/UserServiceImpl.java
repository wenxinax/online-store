package com.example.onlinestore.service.impl;

import com.example.onlinestore.dto.LoginRequest;
import com.example.onlinestore.dto.LoginResponse;
import com.example.onlinestore.model.User;
import com.example.onlinestore.service.UserService;
import com.example.onlinestore.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    /**
     * 用户登录
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        String username = request.getUsername().trim();
        String password = request.getPassword();

        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        // 检查是否为管理员登录
        if (adminUsername.equals(username)) {
            if (adminPassword.equals(password)) {
                logger.info("管理员登录成功");
                return createLoginResponse(request.getUsername());
            } else {
                logger.warn("管理员密码错误");
                // 记录失败次数（全局的，不区分用户）
                recordFailedLogin(request.getUsername());
                throw new IllegalArgumentException(messageSource.getMessage(
                    "error.invalid.credentials", null, LocaleContextHolder.getLocale()));
            }
        }

        // 普通用户验证逻辑
        try {
            // 调用认证服务
            String authUrl = authServiceUrl + "/authenticate";
            Boolean isAuthenticated = restTemplate.postForObject(authUrl, request, Boolean.class);
            
            if (isAuthenticated == null || !isAuthenticated) {
                // 记录失败次数（全局的，不区分用户）
                recordFailedLogin(request.getUsername());
                throw new IllegalArgumentException(messageSource.getMessage(
                    "error.invalid.credentials", null, LocaleContextHolder.getLocale()));
            }

            return createLoginResponse(request.getUsername());
        } catch (Exception e) {
            logger.error("登录验证失败: {}", e.getMessage());
            throw new IllegalArgumentException(messageSource.getMessage(
                "error.invalid.credentials", null, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * 创建登录响应
     */
    private LoginResponse createLoginResponse(String username) {
        // 生成JWT token
        String token = JwtUtils.generateToken(username);
        
        // 保存token到Redis
        String tokenKey = "user:token:" + username;
        redisTemplate.opsForValue().set(tokenKey, token, 24, TimeUnit.HOURS);
        
        // 记录登录时间
        String loginTimeKey = "user:login_time:" + username;
        redisTemplate.opsForValue().set(loginTimeKey, LocalDateTime.now().toString(), 30, TimeUnit.DAYS);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(username);
        response.setMessage("登录成功");
        
        logger.info("用户 {} 登录成功", username);
        return response;
    }

    /**
     * 用户登出
     */
    @Override
    public void logout(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        String tokenKey = "user:token:" + username.trim();
        redisTemplate.delete(tokenKey);
        logger.info("用户 {} 已登出", username);
    }

    /**
     * 根据用户名获取用户信息
     */
    @Override
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String userKey = "user:info:" + username.trim();
        User user = (User) redisTemplate.opsForValue().get(userKey);
        
        if (user == null) {
            // 从数据库或其他服务获取用户信息的逻辑
            logger.debug("用户 {} 不存在或信息已过期", username);
        }
        
        return user;
    }

    /**
     * 根据token获取用户信息
     */
    @Override
    public User getUserByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            // 从JWT token中解析用户名
            String username = JwtUtils.extractUsername(token);
            
            if (username != null) {
                // 验证token是否在Redis中存在（用于登出控制）
                String tokenKey = "user:token:" + username;
                String storedToken = (String) redisTemplate.opsForValue().get(tokenKey);
                
                if (token.equals(storedToken)) {
                    return getUserByUsername(username);
                }
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 记录登录失败次数，按用户进行统计，用于后续风控。
     */
    private void recordFailedLogin(String username) {
        String key = "login:fail:" + username;
        Long cnt = redisTemplate.opsForValue().increment(key);
        if (cnt != null && cnt == 1) {
            // 设置过期时间
            redisTemplate.expire(key, 1, TimeUnit.DAYS);
        }
        logger.debug("记录用户 {} 登录失败次数: {}", username, cnt);
        
        // 可选：添加风控逻辑
        if (cnt != null && cnt >= 5) {
            logger.warn("用户 {} 登录失败次数过多: {}", username, cnt);
            // 这里可以添加账户锁定或其他风控措施
        }
    }
}