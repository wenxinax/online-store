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

    // 失败登录阈值配置
    private static final int USER_FAIL_THRESHOLD = 5;
    private static final int GLOBAL_FAIL_THRESHOLD = 100;

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
        // 先检查是否是管理员用户
        if (adminUsername.equals(request.getUsername())) {
            // 如果是管理员，验证密码
            if (adminPassword.equals(request.getPassword())) {
                logger.info("管理员快速登录");
                return createLoginResponse(request.getUsername());
            } else {
                logger.warn("管理员密码错误");
                // 记录失败次数
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
            // 记录失败次数
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
     * 记录登录失败次数，支持用户维度和全局维度统计，并实现风控阈值检查。
     * 
     * @param username 登录失败的用户名
     */
    private void recordFailedLogin(String username) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("记录失败登录时用户名为空，跳过记录");
            return;
        }

        try {
            // 用户维度的失败计数
            String userKey = "login:fail:user:" + username;
            // 全局失败计数（用于整体监控）
            String globalKey = "login:fail:global";
            
            Long userFailCount = redisTemplate.opsForValue().increment(userKey);
            Long globalFailCount = redisTemplate.opsForValue().increment(globalKey);
            
            // 设置过期时间
            if (userFailCount != null && userFailCount == 1) {
                // 用户级别失败计数1小时过期，防止长期锁定用户
                redisTemplate.expire(userKey, 1, TimeUnit.HOURS);
            }
            if (globalFailCount != null && globalFailCount == 1) {
                // 全局计数1天过期，用于整体安全监控
                redisTemplate.expire(globalKey, 1, TimeUnit.DAYS);
            }
            
            // 用户级别风控检查
            if (userFailCount != null && userFailCount >= USER_FAIL_THRESHOLD) {
                logger.warn("用户 {} 登录失败次数达到 {} 次，建议进行风控处理", username, userFailCount);
                // 可以考虑抛出特定异常或触发其他风控机制
                // 例如：账户临时锁定、要求验证码、通知安全团队等
            }
            
            // 全局级别风控检查
            if (globalFailCount != null && globalFailCount >= GLOBAL_FAIL_THRESHOLD) {
                logger.error("全局登录失败次数达到 {} 次，系统可能遭受攻击，建议立即检查", globalFailCount);
                // 可以触发系统级别的安全响应
            }
            
            logger.debug("记录失败登录次数 用户:{} 次数:{} 全局次数:{}", username, userFailCount, globalFailCount);
            
        } catch (Exception e) {
            logger.error("记录失败登录次数时发生异常，用户: {}", username, e);
            // 失败记录不应该影响正常的登录流程，所以这里只记录日志
        }
    }
}