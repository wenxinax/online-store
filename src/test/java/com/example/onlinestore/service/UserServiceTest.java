package com.example.onlinestore.service;

import com.example.onlinestore.dto.LoginRequest;
import com.example.onlinestore.dto.LoginResponse;
import com.example.onlinestore.dto.PageResponse;
import com.example.onlinestore.dto.UserPageRequest;
import com.example.onlinestore.dto.UserVO;
import com.example.onlinestore.model.User;
import com.example.onlinestore.mapper.UserMapper;
import com.example.onlinestore.service.impl.UserServiceImpl;
import com.example.onlinestore.service.testutil.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

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
@DisplayName("UserService 单元测试")
public class UserServiceTest {

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
        // 设置配置项的值
        ReflectionTestUtils.setField(userService, "adminUsername", ADMIN_USERNAME);
        ReflectionTestUtils.setField(userService, "adminPassword", ADMIN_PASSWORD);
        ReflectionTestUtils.setField(userService, "userServiceBaseUrl", USER_SERVICE_BASE_URL);
    }

    @Test
    void whenAdminLoginWithNewUser_thenCreateUserAndReturnToken() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword(ADMIN_PASSWORD);

        // 设置mock行为：用户不存在
        when(userMapper.findByUsername(ADMIN_USERNAME)).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行测试
        LoginResponse response = userService.login(request);

        // 验证结果
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getExpireTime());
        
        // 验证调用
        verify(userMapper).findByUsername(ADMIN_USERNAME);
        verify(userMapper).insertUser(any(User.class));
        verify(userMapper, never()).updateUserToken(any(User.class));
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
        
        // 验证没有调用用户服务
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void whenAdminLoginWithExistingUser_thenUpdateTokenAndReturn() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword(ADMIN_PASSWORD);

        // 设置mock行为：用户已存在
        User existingUser = new User();
        existingUser.setUsername(ADMIN_USERNAME);
        existingUser.setToken("old-token");
        existingUser.setTokenExpireTime(LocalDateTime.now().minusDays(1));
        when(userMapper.findByUsername(ADMIN_USERNAME)).thenReturn(existingUser);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行测试
        LoginResponse response = userService.login(request);

        // 验证结果
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getExpireTime());
        assertNotEquals("old-token", response.getToken());
        
        // 验证调用
        verify(userMapper).findByUsername(ADMIN_USERNAME);
        verify(userMapper, never()).insertUser(any(User.class));
        verify(userMapper).updateUserToken(any(User.class));
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
        
        // 验证没有调用用户服务
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void whenNormalUserLoginWithNewUser_thenCreateUserAndReturnToken() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("normal_user");
        request.setPassword("password");

        // 设置mock行为：用户不存在，认证成功
        when(userMapper.findByUsername("normal_user")).thenReturn(null);
        when(restTemplate.postForObject(eq(USER_SERVICE_BASE_URL + "/auth"), any(), eq(Boolean.class)))
            .thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行测试
        LoginResponse response = userService.login(request);

        // 验证结果
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getExpireTime());
        
        // 验证调用
        verify(userMapper).findByUsername("normal_user");
        verify(userMapper).insertUser(any(User.class));
        verify(userMapper, never()).updateUserToken(any(User.class));
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
        verify(restTemplate).postForObject(eq(USER_SERVICE_BASE_URL + "/auth"), any(), eq(Boolean.class));

        // 验证插入的用户数据
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insertUser(userCaptor.capture());
        User insertedUser = userCaptor.getValue();
        assertEquals("normal_user", insertedUser.getUsername());
        assertEquals(response.getToken(), insertedUser.getToken());
        assertEquals(response.getExpireTime(), insertedUser.getTokenExpireTime());
    }

    @Test
    void whenNormalUserLoginWithExistingUser_thenUpdateTokenAndReturn() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("normal_user");
        request.setPassword("password");

        // 设置mock行为：用户已存在，认证成功
        User existingUser = new User();
        existingUser.setUsername("normal_user");
        existingUser.setToken("old-token");
        existingUser.setTokenExpireTime(LocalDateTime.now().minusDays(1));
        when(userMapper.findByUsername("normal_user")).thenReturn(existingUser);
        when(restTemplate.postForObject(eq(USER_SERVICE_BASE_URL + "/auth"), any(), eq(Boolean.class)))
            .thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行测试
        LoginResponse response = userService.login(request);

        // 验证结果
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getExpireTime());
        assertNotEquals("old-token", response.getToken());
        
        // 验证调用
        verify(userMapper).findByUsername("normal_user");
        verify(userMapper, never()).insertUser(any(User.class));
        verify(userMapper).updateUserToken(any(User.class));
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
        verify(restTemplate).postForObject(eq(USER_SERVICE_BASE_URL + "/auth"), any(), eq(Boolean.class));

        // 验证更新的用户数据
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateUserToken(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        assertEquals("normal_user", updatedUser.getUsername());
        assertEquals(response.getToken(), updatedUser.getToken());
        assertEquals(response.getExpireTime(), updatedUser.getTokenExpireTime());
    }

    @Test
    void whenAdminLoginWithWrongPassword_thenThrowException() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername(ADMIN_USERNAME);
        request.setPassword("wrong_password");

        // 设置错误消息
        when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
            .thenReturn("Invalid username or password");

        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> userService.login(request));
        assertEquals("Invalid username or password", exception.getMessage());
        
        // 验证调用
        verify(userMapper, never()).findByUsername(anyString());
        verify(userMapper, never()).insertUser(any(User.class));
        verify(userMapper, never()).updateUserToken(any(User.class));
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void whenNormalUserLoginWithWrongPassword_thenThrowException() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("normal_user");
        request.setPassword("wrong_password");

        // 设置mock行为
        when(restTemplate.postForObject(eq(USER_SERVICE_BASE_URL + "/auth"), any(), eq(Boolean.class)))
            .thenReturn(false);
        when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
            .thenReturn("Invalid username or password");

        // 执行测试并验证异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> userService.login(request));
        assertEquals("Invalid username or password", exception.getMessage());
        
        // 验证调用
        verify(userMapper, never()).findByUsername(anyString());
        verify(userMapper, never()).insertUser(any(User.class));
        verify(userMapper, never()).updateUserToken(any(User.class));
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
        verify(restTemplate).postForObject(eq(USER_SERVICE_BASE_URL + "/auth"), any(), eq(Boolean.class));
    }

    // ==================== listUsers 方法测试 ====================

    @Nested
    @DisplayName("listUsers 方法测试")
    class ListUsersTests {

        @Test
        @DisplayName("正常分页查询用户列表")
        void whenListUsersWithValidPagination_thenReturnPageResponse() {
            // 准备测试数据
            UserPageRequest request = UserPageRequestBuilder.builder()
                .withPageNum(1)
                .withPageSize(10)
                .build();

            List<User> users = Arrays.asList(
                UserTestDataBuilder.builder().withId(1L).withUsername("user1").build(),
                UserTestDataBuilder.builder().withId(2L).withUsername("user2").build()
            );

            // 设置mock行为
            when(userMapper.findAllWithPagination(0, 10)).thenReturn(users);
            when(userMapper.countTotal()).thenReturn(2L);

            // 执行测试
            PageResponse<UserVO> response = userService.listUsers(request);

            // 验证结果
            TestAssertions.assertPageResponse(response, 1, 10, 2L);
            assertEquals(2, response.getRecords().size());
            
            UserVO firstUser = response.getRecords().get(0);
            assertEquals(1L, firstUser.getId());
            assertEquals("user1", firstUser.getUsername());

            // 验证调用
            verify(userMapper).findAllWithPagination(0, 10);
            verify(userMapper).countTotal();
        }

        @Test
        @DisplayName("查询结果为空时的处理")
        void whenListUsersWithEmptyResult_thenReturnEmptyPageResponse() {
            // 准备测试数据
            UserPageRequest request = UserPageRequestBuilder.builder().build();

            // 设置mock行为
            when(userMapper.findAllWithPagination(anyInt(), anyInt())).thenReturn(Collections.emptyList());
            when(userMapper.countTotal()).thenReturn(0L);

            // 执行测试
            PageResponse<UserVO> response = userService.listUsers(request);

            // 验证结果
            TestAssertions.assertPageResponse(response, 1, 10, 0L);
            assertTrue(response.getRecords().isEmpty());

            // 验证调用
            verify(userMapper).findAllWithPagination(0, 10);
            verify(userMapper).countTotal();
        }

        @Test
        @DisplayName("大页码查询测试")
        void whenListUsersWithLargePageNum_thenReturnEmptyResult() {
            // 准备测试数据
            UserPageRequest request = UserPageRequestBuilder.builder()
                .withPageNum(999)
                .withPageSize(10)
                .build();

            // 设置mock行为
            when(userMapper.findAllWithPagination(9980, 10)).thenReturn(Collections.emptyList());
            when(userMapper.countTotal()).thenReturn(20L);

            // 执行测试
            PageResponse<UserVO> response = userService.listUsers(request);

            // 验证结果
            TestAssertions.assertPageResponse(response, 999, 10, 20L);
            assertTrue(response.getRecords().isEmpty());

            // 验证调用
            verify(userMapper).findAllWithPagination(9980, 10);
            verify(userMapper).countTotal();
        }

        @Test
        @DisplayName("偏移量计算验证测试")
        void whenListUsersWithDifferentPagination_thenCalculateOffsetCorrectly() {
            // 测试不同的分页参数组合
            testPaginationOffset(1, 10, 0);   // 第1页，每页10条：offset = 0
            testPaginationOffset(2, 10, 10);  // 第2页，每页10条：offset = 10
            testPaginationOffset(3, 5, 10);   // 第3页，每页5条：offset = 10
            testPaginationOffset(5, 20, 80);  // 第5页，每页20条：offset = 80
        }

        private void testPaginationOffset(int pageNum, int pageSize, int expectedOffset) {
            UserPageRequest request = UserPageRequestBuilder.builder()
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .build();

            when(userMapper.findAllWithPagination(expectedOffset, pageSize))
                .thenReturn(Collections.emptyList());
            when(userMapper.countTotal()).thenReturn(0L);

            userService.listUsers(request);

            verify(userMapper).findAllWithPagination(expectedOffset, pageSize);
        }

        @Test
        @DisplayName("用户实体到VO转换测试")
        void whenListUsers_thenConvertUserToVOCorrectly() {
            // 准备测试数据
            UserPageRequest request = UserPageRequestBuilder.builder().build();
            
            LocalDateTime now = LocalDateTime.now();
            User user = UserTestDataBuilder.builder()
                .withId(100L)
                .withUsername("testconvert")
                .withCreatedAt(now)
                .withUpdatedAt(now)
                .build();

            // 设置mock行为
            when(userMapper.findAllWithPagination(anyInt(), anyInt()))
                .thenReturn(Arrays.asList(user));
            when(userMapper.countTotal()).thenReturn(1L);

            // 执行测试
            PageResponse<UserVO> response = userService.listUsers(request);

            // 验证结果
            assertEquals(1, response.getRecords().size());
            UserVO vo = response.getRecords().get(0);
            TestAssertions.assertUserVO(vo, user);
        }
    }

    // ==================== getUserByToken 方法测试 ====================

    @Nested
    @DisplayName("getUserByToken 方法测试")
    class GetUserByTokenTests {

        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
            objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        @DisplayName("有效Token查询用户信息")
        void whenGetUserByValidToken_thenReturnUser() throws JsonProcessingException {
            // 准备测试数据
            String token = "valid-token-123";
            User expectedUser = UserTestDataBuilder.builder()
                .withUsername("testuser")
                .withToken(token)
                .build();
            String userJson = objectMapper.writeValueAsString(expectedUser);

            // 设置mock行为
            RedisTestHelper.setupRedisGetUser(valueOperations, token, userJson);

            // 执行测试
            User result = userService.getUserByToken(token);

            // 验证结果
            assertNotNull(result);
            assertEquals(expectedUser.getUsername(), result.getUsername());
            assertEquals(expectedUser.getToken(), result.getToken());

            // 验证调用
            verify(valueOperations).get("token:" + token);
        }

        @Test
        @DisplayName("无效Token查询测试")
        void whenGetUserByInvalidToken_thenReturnNull() {
            // 准备测试数据
            String token = "invalid-token";

            // 设置mock行为
            RedisTestHelper.setupRedisGetUserNull(valueOperations, token);

            // 执行测试
            User result = userService.getUserByToken(token);

            // 验证结果
            assertNull(result);

            // 验证调用
            verify(valueOperations).get("token:" + token);
        }

        @Test
        @DisplayName("Token过期测试")
        void whenGetUserByExpiredToken_thenReturnNull() {
            // 准备测试数据
            String token = "expired-token";

            // 设置mock行为：过期的token在Redis中不存在
            RedisTestHelper.setupRedisGetUserNull(valueOperations, token);

            // 执行测试
            User result = userService.getUserByToken(token);

            // 验证结果
            assertNull(result);

            // 验证调用
            verify(valueOperations).get("token:" + token);
        }

        @Test
        @DisplayName("JSON反序列化异常测试")
        void whenGetUserByTokenWithInvalidJson_thenReturnNull() {
            // 准备测试数据
            String token = "token-with-invalid-json";
            String invalidJson = "invalid-json-string";

            // 设置mock行为
            RedisTestHelper.setupRedisGetUser(valueOperations, token, invalidJson);

            // 执行测试
            User result = userService.getUserByToken(token);

            // 验证结果
            assertNull(result);

            // 验证调用
            verify(valueOperations).get("token:" + token);
        }

        @Test
        @DisplayName("Redis连接异常测试")
        void whenGetUserByTokenWithRedisException_thenReturnNull() {
            // 准备测试数据
            String token = "test-token";

            // 设置mock行为：Redis抛出异常
            when(valueOperations.get("token:" + token))
                .thenThrow(new RuntimeException("Redis连接异常"));

            // 执行测试
            User result = userService.getUserByToken(token);

            // 验证结果
            assertNull(result);

            // 验证调用
            verify(valueOperations).get("token:" + token);
        }
    }

    // ==================== login 方法异常场景增强测试 ====================

    @Nested
    @DisplayName("login 方法异常场景测试")
    class LoginExceptionTests {

        @BeforeEach
        void setUp() {
            lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            // 为计数器设置默认行为
            lenient().when(valueOperations.increment("login:fail")).thenReturn(1L);
            lenient().when(redisTemplate.expire("login:fail", 1, TimeUnit.DAYS)).thenReturn(true);
        }

        @Test
        @DisplayName("Redis缓存异常处理测试")
        void whenLoginWithRedisCacheException_thenContinueLogin() {
            // 准备测试数据
            LoginRequest request = LoginRequestBuilder.builder()
                .withUsername(ADMIN_USERNAME)
                .withPassword(ADMIN_PASSWORD)
                .build();

            // 设置mock行为
            when(userMapper.findByUsername(ADMIN_USERNAME)).thenReturn(null);
            RedisTestHelper.setupRedisExceptionBehavior(redisTemplate, valueOperations);

            // 执行测试
            LoginResponse response = userService.login(request);

            // 验证结果：登录成功但Redis缓存失败不影响主流程
            TestAssertions.assertLoginResponse(response);

            // 验证调用
            verify(userMapper).findByUsername(ADMIN_USERNAME);
            verify(userMapper).insertUser(any(User.class));
            verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
        }

        @Test
        @DisplayName("外部服务异常处理测试")
        void whenLoginWithExternalServiceException_thenThrowException() {
            // 准备测试数据
            LoginRequest request = LoginRequestBuilder.builder()
                .withUsername("normal_user")
                .withPassword("password")
                .build();

            // 设置mock行为：外部服务抛出网络异常
            when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class)))
                .thenThrow(new ResourceAccessException("网络连接超时"));

            // 执行测试并验证异常
            assertThrows(ResourceAccessException.class, () -> userService.login(request));

            // 验证调用
            verify(restTemplate).postForObject(anyString(), any(), eq(Boolean.class));
            verify(userMapper, never()).findByUsername(anyString());
        }

        @Test
        @DisplayName("失败登录计数测试 - 管理员密码错误")
        void whenAdminLoginFailure_thenRecordFailedLogin() {
            // 准备测试数据
            LoginRequest request = LoginRequestBuilder.builder()
                .withUsername(ADMIN_USERNAME)
                .withPassword("wrong_password")
                .build();

            // 设置mock行为
            when(valueOperations.increment("login:fail")).thenReturn(1L);  // 第一次失败
            when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
                .thenReturn("Invalid credentials");

            // 执行测试并验证异常
            assertThrows(IllegalArgumentException.class, () -> userService.login(request));

            // 验证失败登录计数调用
            verify(valueOperations).increment("login:fail");
            verify(redisTemplate).expire("login:fail", 1, TimeUnit.DAYS);
        }

        @Test
        @DisplayName("失败登录计数测试 - 普通用户认证失败")
        void whenNormalUserLoginFailure_thenRecordFailedLogin() {
            // 准备测试数据
            LoginRequest request = LoginRequestBuilder.builder()
                .withUsername("normal_user")
                .withPassword("wrong_password")
                .build();

            // 设置mock行为
            when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class)))
                .thenReturn(false);
            when(valueOperations.increment("login:fail")).thenReturn(2L);  // 第二次失败
            when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
                .thenReturn("Invalid credentials");

            // 执行测试并验证异常
            assertThrows(IllegalArgumentException.class, () -> userService.login(request));

            // 验证失败登录计数调用
            verify(valueOperations).increment("login:fail");
            // 第二次失败不会设置过期时间
            verify(redisTemplate, never()).expire("login:fail", 1, TimeUnit.DAYS);
        }

        @Test
        @DisplayName("外部服务返回null测试")
        void whenExternalServiceReturnsNull_thenThrowException() {
            // 准备测试数据
            LoginRequest request = LoginRequestBuilder.builder()
                .withUsername("normal_user")
                .withPassword("password")
                .build();

            // 设置mock行为：外部服务返回null
            when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class)))
                .thenReturn(null);
            when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
                .thenReturn("Invalid credentials");

            // 执行测试并验证异常
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> userService.login(request));
            assertEquals("Invalid credentials", exception.getMessage());

            // 验证调用
            verify(restTemplate).postForObject(anyString(), any(), eq(Boolean.class));
        }
    }

    // ==================== 边界条件和异常处理测试 ====================

    @Nested
    @DisplayName("边界条件和异常处理测试")
    class BoundaryAndExceptionTests {

        @Test
        @DisplayName("数据库查询异常测试")
        void whenDatabaseQueryException_thenThrowException() {
            // 准备测试数据
            UserPageRequest request = UserPageRequestBuilder.builder().build();

            // 设置mock行为：数据库查询抛出异常
            when(userMapper.findAllWithPagination(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("数据库连接异常"));

            // 执行测试并验证异常
            assertThrows(RuntimeException.class, () -> userService.listUsers(request));

            // 验证调用
            verify(userMapper).findAllWithPagination(anyInt(), anyInt());
            verify(userMapper, never()).countTotal();
        }

        @Test
        @DisplayName("用户插入异常测试")
        void whenUserInsertException_thenThrowException() {
            // 准备测试数据
            LoginRequest request = LoginRequestBuilder.builder()
                .withUsername(ADMIN_USERNAME)
                .withPassword(ADMIN_PASSWORD)
                .build();

            // 设置mock行为
            when(userMapper.findByUsername(ADMIN_USERNAME)).thenReturn(null);
            doThrow(new RuntimeException("数据库插入失败")).when(userMapper).insertUser(any(User.class));

            // 执行测试并验证异常
            assertThrows(RuntimeException.class, () -> userService.login(request));

            // 验证调用
            verify(userMapper).findByUsername(ADMIN_USERNAME);
            verify(userMapper).insertUser(any(User.class));
        }

        @Test
        @DisplayName("convertToVO方法null处理测试")
        void whenConvertNullUserToVO_thenReturnNull() {
            // 准备测试数据
            UserPageRequest request = UserPageRequestBuilder.builder().build();

            // 设置mock行为：返回包含null的用户列表
            when(userMapper.findAllWithPagination(anyInt(), anyInt()))
                .thenReturn(Arrays.asList((User) null));
            when(userMapper.countTotal()).thenReturn(1L);

            // 执行测试
            PageResponse<UserVO> response = userService.listUsers(request);

            // 验证结果：null用户应该被转换为null VO
            assertEquals(1, response.getRecords().size());
            assertNull(response.getRecords().get(0));
        }
    }
} 