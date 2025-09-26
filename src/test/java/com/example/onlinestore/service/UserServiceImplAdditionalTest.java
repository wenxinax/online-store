package com.example.onlinestore.service;

import com.example.onlinestore.dto.LoginRequest;
import com.example.onlinestore.dto.LoginResponse;
import com.example.onlinestore.dto.PageResponse;
import com.example.onlinestore.dto.UserPageRequest;
import com.example.onlinestore.dto.UserVO;
import com.example.onlinestore.model.User;
import com.example.onlinestore.mapper.UserMapper;
import com.example.onlinestore.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 补充测试用例
 * 主要测试边界情况、异常场景和私有方法逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 补充单元测试")
public class UserServiceImplAdditionalTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserMapper userMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private UserServiceImpl userService;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";
    private static final String USER_SERVICE_BASE_URL = "http://user-service";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "adminUsername", ADMIN_USERNAME);
        ReflectionTestUtils.setField(userService, "adminPassword", ADMIN_PASSWORD);
        ReflectionTestUtils.setField(userService, "userServiceBaseUrl", USER_SERVICE_BASE_URL);
    }

    // ==================== login 方法边界情况测试 ====================

    @Test
    @DisplayName("空用户名登录")
    void testLoginWithEmptyUsername() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("password");

        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class))).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:fail")).thenReturn(1L);
        when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
            .thenReturn("Invalid credentials");

        // 执行 & 验证
        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("空密码登录")
    void testLoginWithEmptyPassword() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("");

        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class))).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:fail")).thenReturn(1L);
        when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
            .thenReturn("Invalid credentials");

        // 执行 & 验证
        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("null请求对象登录")
    void testLoginWithNullRequest() {
        // 执行 & 验证
        assertThrows(RuntimeException.class, () -> userService.login(null));
    }

    @Test
    @DisplayName("外部服务调用异常")
    void testLoginWithRestTemplateException() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class)))
            .thenThrow(new RestClientException("服务不可用"));

        // 执行 & 验证
        assertThrows(RestClientException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("登录失败次数记录 - 第一次失败")
    void testRecordFailedLoginFirstTime() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class))).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:fail")).thenReturn(1L);
        when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
            .thenReturn("Invalid credentials");

        // 执行 & 验证
        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
        
        // 验证设置了过期时间
        verify(redisTemplate).expire("login:fail", 1, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("登录失败次数记录 - 非第一次失败")
    void testRecordFailedLoginMultipleTimes() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class))).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:fail")).thenReturn(5L); // 第5次失败
        when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
            .thenReturn("Invalid credentials");

        // 执行 & 验证
        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
        
        // 验证没有再次设置过期时间
        verify(redisTemplate, never()).expire(eq("login:fail"), anyLong(), any());
    }

    @Test
    @DisplayName("数据库插入用户异常")
    void testLoginWithDatabaseInsertException() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword(ADMIN_PASSWORD);

        when(userMapper.findByUsername(ADMIN_USERNAME)).thenReturn(null);
        doThrow(new RuntimeException("数据库异常")).when(userMapper).insertUser(any(User.class));

        // 执行 & 验证
        assertThrows(RuntimeException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("数据库更新用户Token异常")
    void testLoginWithDatabaseUpdateException() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword(ADMIN_PASSWORD);

        User existingUser = new User();
        existingUser.setUsername(ADMIN_USERNAME);
        
        when(userMapper.findByUsername(ADMIN_USERNAME)).thenReturn(existingUser);
        doThrow(new RuntimeException("数据库更新异常")).when(userMapper).updateUserToken(any(User.class));

        // 执行 & 验证
        assertThrows(RuntimeException.class, () -> userService.login(request));
    }

    // ==================== listUsers 方法边界情况测试 ====================

    @Test
    @DisplayName("极限分页参数测试")
    void testListUsersWithLargePageSize() {
        // 准备
        UserPageRequest request = new UserPageRequest();
        request.setPageNum(999);
        request.setPageSize(100);

        when(userMapper.findAllWithPagination(99800, 100)).thenReturn(Arrays.asList());
        when(userMapper.countTotal()).thenReturn(100000L);

        // 执行
        PageResponse<UserVO> response = userService.listUsers(request);

        // 验证
        assertNotNull(response);
        assertEquals(999, response.getPageNum());
        assertEquals(100, response.getPageSize());
        assertEquals(100000L, response.getTotal());
    }

    @Test
    @DisplayName("用户数据完整性验证")
    void testListUsersDataIntegrity() {
        // 准备
        UserPageRequest request = new UserPageRequest();
        request.setPageNum(1);
        request.setPageSize(1);

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId(123L);
        user.setUsername("integrity_test_user");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        when(userMapper.findAllWithPagination(0, 1)).thenReturn(Arrays.asList(user));
        when(userMapper.countTotal()).thenReturn(1L);

        // 执行
        PageResponse<UserVO> response = userService.listUsers(request);

        // 验证
        assertEquals(1, response.getRecords().size());
        UserVO vo = response.getRecords().get(0);
        assertEquals(123L, vo.getId());
        assertEquals("integrity_test_user", vo.getUsername());
        assertEquals(now, vo.getCreatedAt());
        assertEquals(now, vo.getUpdatedAt());
    }

    @Test
    @DisplayName("countTotal异常处理")
    void testListUsersCountTotalException() {
        // 准备
        UserPageRequest request = new UserPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        when(userMapper.findAllWithPagination(0, 10)).thenReturn(Arrays.asList());
        when(userMapper.countTotal()).thenThrow(new RuntimeException("计数查询异常"));

        // 执行 & 验证
        assertThrows(RuntimeException.class, () -> userService.listUsers(request));
    }

    // ==================== getUserByToken 方法边界情况测试 ====================

    @Test
    @DisplayName("空Token字符串")
    void testGetUserByEmptyToken() {
        // 准备
        String token = "";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token:")).thenReturn(null);

        // 执行
        User result = userService.getUserByToken(token);

        // 验证
        assertNull(result);
        verify(valueOperations).get("token:");
    }

    @Test
    @DisplayName("null Token")
    void testGetUserByNullToken() {
        // 准备
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token:null")).thenReturn(null);

        // 执行
        User result = userService.getUserByToken(null);

        // 验证
        assertNull(result);
    }

    @Test
    @DisplayName("包含特殊字符的Token")
    void testGetUserByTokenWithSpecialCharacters() {
        // 准备
        String token = "token-with-special-chars!@#$%^&*()";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token:" + token)).thenReturn(null);

        // 执行
        User result = userService.getUserByToken(token);

        // 验证
        assertNull(result);
        verify(valueOperations).get("token:" + token);
    }

    @Test
    @DisplayName("JSON序列化用户数据验证")
    void testGetUserByTokenJsonSerialization() throws Exception {
        // 准备
        String token = "serialization-test-token";
        LocalDateTime now = LocalDateTime.now();
        
        User expectedUser = new User();
        expectedUser.setId(999L);
        expectedUser.setUsername("json_test_user");
        expectedUser.setToken(token);
        expectedUser.setTokenExpireTime(now.plusDays(1));
        expectedUser.setCreatedAt(now.minusDays(1));
        expectedUser.setUpdatedAt(now);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String userJson = objectMapper.writeValueAsString(expectedUser);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token:" + token)).thenReturn(userJson);

        // 执行
        User result = userService.getUserByToken(token);

        // 验证
        assertNotNull(result);
        assertEquals(999L, result.getId());
        assertEquals("json_test_user", result.getUsername());
        assertEquals(token, result.getToken());
        assertEquals(expectedUser.getTokenExpireTime(), result.getTokenExpireTime());
        assertEquals(expectedUser.getCreatedAt(), result.getCreatedAt());
        assertEquals(expectedUser.getUpdatedAt(), result.getUpdatedAt());
    }

    // ==================== convertToVO 方法详细测试 ====================

    @Test
    @DisplayName("用户转VO字段完整性验证")
    void testConvertToVOFieldMapping() {
        // 准备
        UserPageRequest request = new UserPageRequest();
        request.setPageNum(1);
        request.setPageSize(1);

        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2023, 6, 15, 14, 30, 0);

        User user = new User();
        user.setId(42L);
        user.setUsername("field_mapping_test");
        user.setToken("should-not-appear-in-vo");
        user.setTokenExpireTime(LocalDateTime.now());
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        when(userMapper.findAllWithPagination(0, 1)).thenReturn(Arrays.asList(user));
        when(userMapper.countTotal()).thenReturn(1L);

        // 执行
        PageResponse<UserVO> response = userService.listUsers(request);

        // 验证
        UserVO vo = response.getRecords().get(0);
        assertEquals(42L, vo.getId());
        assertEquals("field_mapping_test", vo.getUsername());
        assertEquals(createdAt, vo.getCreatedAt());
        assertEquals(updatedAt, vo.getUpdatedAt());
        
        // 验证VO不包含敏感信息（token等）
        // 通过反射检查VO是否有token字段
        assertFalse(hasField(vo.getClass(), "token"));
        assertFalse(hasField(vo.getClass(), "tokenExpireTime"));
    }

    @Test
    @DisplayName("混合null和正常用户的转换")
    void testConvertMixedUsersToVO() {
        // 准备
        UserPageRequest request = new UserPageRequest();
        request.setPageNum(1);
        request.setPageSize(3);

        User validUser = new User();
        validUser.setId(1L);
        validUser.setUsername("valid_user");
        validUser.setCreatedAt(LocalDateTime.now());
        validUser.setUpdatedAt(LocalDateTime.now());

        List<User> mixedUsers = Arrays.asList(validUser, null, validUser);

        when(userMapper.findAllWithPagination(0, 3)).thenReturn(mixedUsers);
        when(userMapper.countTotal()).thenReturn(3L);

        // 执行
        PageResponse<UserVO> response = userService.listUsers(request);

        // 验证
        assertEquals(3, response.getRecords().size());
        assertNotNull(response.getRecords().get(0));
        assertNull(response.getRecords().get(1));
        assertNotNull(response.getRecords().get(2));
    }

    // ==================== 服务初始化和配置测试 ====================

    @Test
    @DisplayName("ObjectMapper配置验证")
    void testObjectMapperConfiguration() {
        // 通过反射获取ObjectMapper实例
        ObjectMapper objectMapper = (ObjectMapper) ReflectionTestUtils.getField(userService, "objectMapper");
        
        // 验证
        assertNotNull(objectMapper);
        
        // 验证JavaTimeModule已注册（通过序列化LocalDateTime测试）
        LocalDateTime now = LocalDateTime.now();
        assertDoesNotThrow(() -> objectMapper.writeValueAsString(now));
    }

    @Test
    @DisplayName("配置属性注入验证")
    void testConfigurationProperties() {
        // 验证配置属性已正确注入
        String adminUsername = (String) ReflectionTestUtils.getField(userService, "adminUsername");
        String adminPassword = (String) ReflectionTestUtils.getField(userService, "adminPassword");
        String userServiceBaseUrl = (String) ReflectionTestUtils.getField(userService, "userServiceBaseUrl");

        assertEquals(ADMIN_USERNAME, adminUsername);
        assertEquals(ADMIN_PASSWORD, adminPassword);
        assertEquals(USER_SERVICE_BASE_URL, userServiceBaseUrl);
    }

    // ==================== 辅助方法 ====================

    /**
     * 检查类是否包含指定字段名的字段
     */
    private boolean hasField(Class<?> clazz, String fieldName) {
        try {
            clazz.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}