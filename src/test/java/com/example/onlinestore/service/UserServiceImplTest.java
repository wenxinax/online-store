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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 完整单元测试")
public class UserServiceImplTest {

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

    // ==================== login 方法测试 ====================

    @Test
    @DisplayName("管理员新用户登录成功")
    void testAdminLoginNewUser() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword(ADMIN_PASSWORD);

        when(userMapper.findByUsername(ADMIN_USERNAME)).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行
        LoginResponse response = userService.login(request);

        // 验证
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getExpireTime());

        verify(userMapper).findByUsername(ADMIN_USERNAME);
        verify(userMapper).insertUser(any(User.class));
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("管理员现有用户登录成功")
    void testAdminLoginExistingUser() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword(ADMIN_PASSWORD);

        User existingUser = new User();
        existingUser.setUsername(ADMIN_USERNAME);
        existingUser.setToken("old-token");
        
        when(userMapper.findByUsername(ADMIN_USERNAME)).thenReturn(existingUser);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行
        LoginResponse response = userService.login(request);

        // 验证
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotEquals("old-token", response.getToken());

        verify(userMapper).updateUserToken(any(User.class));
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("普通用户登录成功")
    void testNormalUserLoginSuccess() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername("normal_user");
        request.setPassword("password");

        when(userMapper.findByUsername("normal_user")).thenReturn(null);
        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class))).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行
        LoginResponse response = userService.login(request);

        // 验证
        assertNotNull(response);
        verify(restTemplate).postForObject(anyString(), any(), eq(Boolean.class));
        verify(userMapper).insertUser(any(User.class));
    }

    @Test
    @DisplayName("管理员密码错误")
    void testAdminLoginWrongPassword() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword("wrong_password");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:fail")).thenReturn(1L);
        when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
            .thenReturn("Invalid credentials");

        // 执行 & 验证
        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
        verify(valueOperations).increment("login:fail");
        verify(redisTemplate).expire("login:fail", 1, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("普通用户认证失败")
    void testNormalUserLoginFailure() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername("normal_user");
        request.setPassword("wrong_password");

        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class))).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:fail")).thenReturn(2L);
        when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
            .thenReturn("Invalid credentials");

        // 执行 & 验证
        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
        verify(restTemplate).postForObject(anyString(), any(), eq(Boolean.class));
        verify(valueOperations).increment("login:fail");
    }

    // ==================== listUsers 方法测试 ====================

    @Test
    @DisplayName("分页查询用户列表")
    void testListUsersSuccess() {
        // 准备
        UserPageRequest request = new UserPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());

        List<User> users = Arrays.asList(user1, user2);

        when(userMapper.findAllWithPagination(0, 10)).thenReturn(users);
        when(userMapper.countTotal()).thenReturn(2L);

        // 执行
        PageResponse<UserVO> response = userService.listUsers(request);

        // 验证
        assertNotNull(response);
        assertEquals(2, response.getRecords().size());
        assertEquals(2L, response.getTotal());
        assertEquals(1, response.getPageNum());
        assertEquals(10, response.getPageSize());

        UserVO firstVO = response.getRecords().get(0);
        assertEquals(1L, firstVO.getId());
        assertEquals("user1", firstVO.getUsername());

        verify(userMapper).findAllWithPagination(0, 10);
        verify(userMapper).countTotal();
    }

    @Test
    @DisplayName("空结果分页查询")
    void testListUsersEmpty() {
        // 准备
        UserPageRequest request = new UserPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        when(userMapper.findAllWithPagination(0, 10)).thenReturn(Collections.emptyList());
        when(userMapper.countTotal()).thenReturn(0L);

        // 执行
        PageResponse<UserVO> response = userService.listUsers(request);

        // 验证
        assertNotNull(response);
        assertTrue(response.getRecords().isEmpty());
        assertEquals(0L, response.getTotal());
    }

    @Test
    @DisplayName("分页偏移量计算验证")
    void testListUsersPaginationOffset() {
        // 准备不同的分页参数
        UserPageRequest request1 = new UserPageRequest();
        request1.setPageNum(2);
        request1.setPageSize(10);

        UserPageRequest request2 = new UserPageRequest();
        request2.setPageNum(3);
        request2.setPageSize(5);

        when(userMapper.findAllWithPagination(anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(userMapper.countTotal()).thenReturn(0L);

        // 执行
        userService.listUsers(request1);
        userService.listUsers(request2);

        // 验证偏移量计算
        verify(userMapper).findAllWithPagination(10, 10); // 第2页，每页10条：offset = 10
        verify(userMapper).findAllWithPagination(10, 5);  // 第3页，每页5条：offset = 10
    }

    // ==================== getUserByToken 方法测试 ====================

    @Test
    @DisplayName("有效Token获取用户")
    void testGetUserByValidToken() throws Exception {
        // 准备
        String token = "valid-token-123";
        User expectedUser = new User();
        expectedUser.setId(1L);
        expectedUser.setUsername("testuser");
        expectedUser.setToken(token);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String userJson = objectMapper.writeValueAsString(expectedUser);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token:" + token)).thenReturn(userJson);

        // 执行
        User result = userService.getUserByToken(token);

        // 验证
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(token, result.getToken());

        verify(valueOperations).get("token:" + token);
    }

    @Test
    @DisplayName("无效Token返回null")
    void testGetUserByInvalidToken() {
        // 准备
        String token = "invalid-token";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token:" + token)).thenReturn(null);

        // 执行
        User result = userService.getUserByToken(token);

        // 验证
        assertNull(result);
        verify(valueOperations).get("token:" + token);
    }

    @Test
    @DisplayName("JSON反序列化异常返回null")
    void testGetUserByTokenInvalidJson() {
        // 准备
        String token = "token-with-invalid-json";
        String invalidJson = "invalid-json";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token:" + token)).thenReturn(invalidJson);

        // 执行
        User result = userService.getUserByToken(token);

        // 验证
        assertNull(result);
    }

    @Test
    @DisplayName("Redis异常返回null")
    void testGetUserByTokenRedisException() {
        // 准备
        String token = "test-token";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token:" + token)).thenThrow(new RuntimeException("Redis异常"));

        // 执行
        User result = userService.getUserByToken(token);

        // 验证
        assertNull(result);
    }

    // ==================== 异常处理测试 ====================

    @Test  
    @DisplayName("Redis缓存异常不影响登录流程")
    void testLoginWithRedisCacheException() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword(ADMIN_PASSWORD);

        when(userMapper.findByUsername(ADMIN_USERNAME)).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Redis异常")).when(valueOperations)
            .set(anyString(), anyString(), anyLong(), any());

        // 执行
        LoginResponse response = userService.login(request);

        // 验证：登录成功但Redis缓存失败不影响主流程
        assertNotNull(response);
        assertNotNull(response.getToken());

        verify(userMapper).insertUser(any(User.class));
    }

    @Test
    @DisplayName("数据库查询异常")
    void testDatabaseQueryException() {
        // 准备
        UserPageRequest request = new UserPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        when(userMapper.findAllWithPagination(anyInt(), anyInt()))
            .thenThrow(new RuntimeException("数据库连接异常"));

        // 执行 & 验证
        assertThrows(RuntimeException.class, () -> userService.listUsers(request));
    }

    @Test
    @DisplayName("外部服务返回null的处理")
    void testExternalServiceReturnsNull() {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername("normal_user");
        request.setPassword("password");

        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class))).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:fail")).thenReturn(1L);
        when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
            .thenReturn("Invalid credentials");

        // 执行 & 验证
        assertThrows(IllegalArgumentException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("convertToVO方法null处理")
    void testConvertNullUserToVO() {
        // 准备
        UserPageRequest request = new UserPageRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        // 返回包含null的用户列表
        when(userMapper.findAllWithPagination(anyInt(), anyInt()))
            .thenReturn(Arrays.asList((User) null));
        when(userMapper.countTotal()).thenReturn(1L);

        // 执行
        PageResponse<UserVO> response = userService.listUsers(request);

        // 验证：null用户应该被转换为null VO
        assertEquals(1, response.getRecords().size());
        assertNull(response.getRecords().get(0));
    }
}