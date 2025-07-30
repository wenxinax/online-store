package com.example.onlinestore.service.impl;

import com.example.onlinestore.dto.LoginRequest;
import com.example.onlinestore.dto.LoginResponse;
import com.example.onlinestore.dto.PageResponse;
import com.example.onlinestore.dto.UserPageRequest;
import com.example.onlinestore.dto.UserVO;
import com.example.onlinestore.model.User;
import com.example.onlinestore.mapper.UserMapper;
import com.example.onlinestore.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final ObjectMapper objectMapper;

    public UserServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Value("${admin.auth.username}")
    private String adminUsername;

    @Value("${admin.auth.password}")
    private String adminPassword;

    @Value("${service.user.base-url}")
    private String userServiceBaseUrl;

    private static final String AUTH_PATH = "/auth";
    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_EXPIRE_DAYS = 1;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MessageSource messageSource;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 检查用户是否被锁定
        checkUserLockStatus(request.getUsername());
        
        // 先检查是否是管理员用户
        if (adminUsername.equals(request.getUsername())) {
            // 如果是管理员，验证密码
            if (adminPassword.equals(request.getPassword())) {
                logger.info("管理员快速登录");
                return createLoginResponse(request.getUsername());
            } else {
                logger.warn("管理员密码错误");
                // 记录失败次数（按用户区分）
                recordFailedLogin(request.getUsername());
                throw new IllegalArgumentException(messageSource.getMessage(
                    "error.invalid.credentials", null, LocaleContextHolder.getLocale()));
            }
        }

        // 非管理员用户，调用user-service进行认证
        String authUrl = UriComponentsBuilder.fromHttpUrl(userServiceBaseUrl)
            .path(AUTH_PATH)
            .toUriString();
        Boolean isAuthenticated = restTemplate.postForObject(authUrl, request, Boolean.class);
        
        if (isAuthenticated == null || !isAuthenticated) {
            // 记录失败次数（按用户区分）
            recordFailedLogin(request.getUsername());
            throw new IllegalArgumentException(messageSource.getMessage(
                "error.invalid.credentials", null, LocaleContextHolder.getLocale()));
        }

        return createLoginResponse(request.getUsername());
    }

    private LoginResponse createLoginResponse(String username) {
        // 生成token
        String token = UUID.randomUUID().toString();
        LocalDateTime expireTime = LocalDateTime.now().plusDays(TOKEN_EXPIRE_DAYS);

        // 查找或创建用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            // 用户不存在，创建新用户
            user = new User();
            user.setUsername(username);
            user.setToken(token);
            user.setTokenExpireTime(expireTime);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insertUser(user);
            logger.info("创建新用户: {}", username);
        } else {
            // 更新现有用户的token
            user.setToken(token);
            user.setTokenExpireTime(expireTime);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateUserToken(user);
            logger.info("更新用户token: {}", username);
        }

        try {
            // 将用户信息转换为JSON并保存到Redis
            String redisKey = TOKEN_PREFIX + token;
            String userJson = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(redisKey, userJson, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
            logger.info("用户信息已缓存到Redis: {}", username);
        } catch (Exception e) {
            logger.error("缓存用户信息失败", e);
            // 继续处理，因为这不是致命错误
        }

        // 返回响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpireTime(expireTime);
        return response;
    }

    private UserVO convertToVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }

    @Override
    public PageResponse<UserVO> listUsers(UserPageRequest request) {
        // 计算分页参数
        int offset = (request.getPageNum() - 1) * request.getPageSize();
        int limit = request.getPageSize();

        // 查询数据
        List<User> users = userMapper.findAllWithPagination(offset, limit);
        long total = userMapper.countTotal();

        // 转换为VO
        List<UserVO> userVOs = users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建响应
        PageResponse<UserVO> response = new PageResponse<>();
        response.setRecords(userVOs);
        response.setTotal(total);
        response.setPageNum(request.getPageNum());
        response.setPageSize(request.getPageSize());

        return response;
    }

    @Override
    public User getUserByToken(String token) {
        try {
            String redisKey = TOKEN_PREFIX + token;
            String userJson = redisTemplate.opsForValue().get(redisKey);
            if (userJson == null) {
                logger.warn("无效的token: {}", token);
                return null;
            }
            return objectMapper.readValue(userJson, User.class);
        } catch (Exception e) {
            logger.error("从Redis获取用户信息失败", e);
            return null;
        }
    }

    /**
     * 记录登录失败次数，如果超过阈值可以用于后续风控。
     * 使用Lua脚本确保Redis操作的原子性，避免竞态条件。
     * 
     * @param username 用户名
     * @return 当前失败次数
     */
    private long recordFailedLogin(String username) {
        String key = "login:fail:" + username;
        // 使用Lua脚本原子性地递增计数器并设置过期时间
        String luaScript = 
            "local cnt = redis.call('INCR', KEYS[1]) " +
            "if cnt == 1 then " +
            "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
            "end " +
            "return cnt";
        
        Long cnt = (Long) redisTemplate.execute(
            (RedisCallback<Long>) connection -> 
                connection.eval(
                    luaScript.getBytes(), 
                    ReturnType.INTEGER, 
                    1, 
                    key.getBytes(), 
                    String.valueOf(TimeUnit.DAYS.toSeconds(1)).getBytes()));
        
        logger.debug("记录失败登录次数 {} -> {}", username, cnt);
        return cnt != null ? cnt : 0;
    }
    
    /**
     * 检查用户是否超过最大登录失败次数
     * 
     * @param username 用户名
     * @return 是否超过限制
     */
    private boolean isLoginAttemptsExceeded(String username) {
        String key = "login:fail:" + username;
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr != null) {
            try {
                long count = Long.parseLong(countStr);
                // 假设最大失败次数为5次
                return count >= 5;
            } catch (NumberFormatException e) {
                logger.warn("解析登录失败次数失败: {}", countStr);
            }
        }
        return false;
    }
    
    /**
     * 在登录前检查用户是否被锁定
     * 
     * @param username 用户名
     * @throws IllegalArgumentException 如果用户被锁定
     */
    private void checkUserLockStatus(String username) {
        String lockKey = "login:lock:" + username;
        String lockValue = redisTemplate.opsForValue().get(lockKey);
        if (lockValue != null) {
            logger.warn("用户 {} 被临时锁定", username);
            throw new IllegalArgumentException(messageSource.getMessage(
                "error.account.locked", null, LocaleContextHolder.getLocale()));
        }
        
        if (isLoginAttemptsExceeded(username)) {
            // 锁定用户30分钟
            redisTemplate.opsForValue().set(lockKey, "1", 30, TimeUnit.MINUTES);
            logger.warn("用户 {} 登录失败次数过多，已被锁定30分钟", username);
            // 同时清除失败计数器
            String failKey = "login:fail:" + username;
            redisTemplate.delete(failKey);
            throw new IllegalArgumentException(messageSource.getMessage(
                "error.account.locked", null, LocaleContextHolder.getLocale()));
        }
    }
} 