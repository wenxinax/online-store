package com.example.onlinestore.service.impl;

import com.example.onlinestore.dto.LoginRequest;
import com.example.onlinestore.dto.LoginResponse;
import com.example.onlinestore.dto.PageResponse;
import com.example.onlinestore.dto.UserPageRequest;
import com.example.onlinestore.dto.UserVO;
import com.example.onlinestore.mapper.UserMapper;
import com.example.onlinestore.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务实现类测试")
class UserServiceImplTest {

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

    private LoginRequest loginRequest;
    private User testUser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 设置配置属性
        ReflectionTestUtils.setField(userService, "adminUsername", "admin");
        ReflectionTestUtils.setField(userService, "adminPassword", "admin123");
        ReflectionTestUtils.setField(userService, "userServiceBaseUrl", "http://user-service");

        // 准备测试数据
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setToken("test-token");
        testUser.setTokenExpireTime(LocalDateTime.now().plusDays(1));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        objectMapper = new ObjectMapper();

        // Mock Redis operations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock MessageSource
        when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any(Locale.class)))
                .thenReturn("用户名或密码错误");
    }

    @Nested
    @DisplayName("管理员登录测试")
    class AdminLoginTests {

        @Test
        @DisplayName("管理员登录成功")
        void whenAdminLoginWithCorrectCredentials_thenReturnSuccess() {
            // Given
            loginRequest.setUsername("admin");
            loginRequest.setPassword("admin123");

            User adminUser = new User();
            adminUser.setUsername("admin");
            when(userMapper.findByUsername("admin")).thenReturn(adminUser);

            // When
            LoginResponse response = userService.login(loginRequest);

            // Then
            assertNotNull(response);
            assertNotNull(response.getToken());
            assertNotNull(response.getExpireTime());
            assertTrue(response.getExpireTime().isAfter(LocalDateTime.now()));

            verify(userMapper).updateUserToken(any(User.class));
            verify(valueOperations).set(anyString(), anyString(), eq(1L), eq(TimeUnit.DAYS));
        }

        @Test
        @DisplayName("管理员登录失败 - 密码错误")
        void whenAdminLoginWithWrongPassword_thenThrowException() {
            // Given
            loginRequest.setUsername("admin");
            loginRequest.setPassword("wrongpassword");

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(loginRequest)
            );
            assertEquals("用户名或密码错误", exception.getMessage());

            // 验证记录失败次数
            verify(valueOperations).increment("login:fail");
        }

        @Test
        @DisplayName("管理员用户不存在时创建新用户")
        void whenAdminUserNotExists_thenCreateNewUser() {
            // Given
            loginRequest.setUsername("admin");
            loginRequest.setPassword("admin123");
            when(userMapper.findByUsername("admin")).thenReturn(null);

            // When
            LoginResponse response = userService.login(loginRequest);

            // Then
            assertNotNull(response);
            verify(userMapper).insertUser(any(User.class));
            verify(valueOperations).set(anyString(), anyString(), eq(1L), eq(TimeUnit.DAYS));
        }
    }

    @Nested
    @DisplayName("普通用户登录测试")
    class RegularUserLoginTests {

        @Test
        @DisplayName("普通用户登录成功")
        void whenRegularUserLoginSucceeds_thenReturnToken() {
            // Given
            when(restTemplate.postForObject(anyString(), any(LoginRequest.class), eq(Boolean.class)))
                    .thenReturn(true);
            when(userMapper.findByUsername("testuser")).thenReturn(testUser);

            // When
            LoginResponse response = userService.login(loginRequest);

            // Then
            assertNotNull(response);
            assertNotNull(response.getToken());
            assertNotNull(response.getExpireTime());
            assertTrue(response.getExpireTime().isAfter(LocalDateTime.now()));

            verify(userMapper).updateUserToken(any(User.class));
            verify(valueOperations).set(anyString(), anyString(), eq(1L), eq(TimeUnit.DAYS));
        }

        @Test
        @DisplayName("普通用户登录失败 - 认证服务返回false")
        void whenExternalAuthServiceReturnsFalse_thenThrowException() {
            // Given
            when(restTemplate.postForObject(anyString(), any(LoginRequest.class), eq(Boolean.class)))
                    .thenReturn(false);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(loginRequest)
            );
            assertEquals("用户名或密码错误", exception.getMessage());

            // 验证记录失败次数
            verify(valueOperations).increment("login:fail");
        }

        @Test
        @DisplayName("普通用户登录失败 - 认证服务返回null")
        void whenExternalAuthServiceReturnsNull_thenThrowException() {
            // Given
            when(restTemplate.postForObject(anyString(), any(LoginRequest.class), eq(Boolean.class)))
                    .thenReturn(null);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(loginRequest)
            );
            assertEquals("用户名或密码错误", exception.getMessage());
        }

        @Test
        @DisplayName("普通用户首次登录 - 创建新用户")
        void whenRegularUserFirstLogin_thenCreateNewUser() {
            // Given
            when(restTemplate.postForObject(anyString(), any(LoginRequest.class), eq(Boolean.class)))
                    .thenReturn(true);
            when(userMapper.findByUsername("testuser")).thenReturn(null);

            // When
            LoginResponse response = userService.login(loginRequest);

            // Then
            assertNotNull(response);
            verify(userMapper).insertUser(any(User.class));
            verify(valueOperations).set(anyString(), anyString(), eq(1L), eq(TimeUnit.DAYS));
        }

        @Test
        @DisplayName("外部认证服务异常")
        void whenExternalAuthServiceThrows_thenThrowException() {
            // Given
            when(restTemplate.postForObject(anyString(), any(LoginRequest.class), eq(Boolean.class)))
                    .thenThrow(new RestClientException("Service unavailable"));

            // When & Then
            assertThrows(RestClientException.class, () -> userService.login(loginRequest));
            
            // 验证记录失败次数
            verify(valueOperations).increment("login:fail");
        }
    }

    @Nested
    @DisplayName("Token相关测试")
    class TokenTests {

        @Test
        @DisplayName("通过Token获取用户信息成功")
        void whenGetUserByValidToken_thenReturnUser() throws Exception {
            // Given
            String token = "valid-token";
            String userJson = objectMapper.writeValueAsString(testUser);
            when(valueOperations.get("token:valid-token")).thenReturn(userJson);

            // When
            User result = userService.getUserByToken(token);

            // Then
            assertNotNull(result);
            assertEquals(testUser.getUsername(), result.getUsername());
            assertEquals(testUser.getId(), result.getId());
        }

        @Test
        @DisplayName("无效Token - Redis中不存在")
        void whenGetUserByInvalidToken_thenReturnNull() {
            // Given
            String token = "invalid-token";
            when(valueOperations.get("token:invalid-token")).thenReturn(null);

            // When
            User result = userService.getUserByToken(token);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Token解析失败 - JSON格式错误")
        void whenTokenParsingFails_thenReturnNull() {
            // Given
            String token = "valid-token";
            when(valueOperations.get("token:valid-token")).thenReturn("invalid-json");

            // When
            User result = userService.getUserByToken(token);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("Redis操作异常")
        void whenRedisThrows_thenReturnNull() {
            // Given
            String token = "valid-token";
            when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis connection failed"));

            // When
            User result = userService.getUserByToken(token);

            // Then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("分页查询测试")
    class PaginationTests {

        @Test
        @DisplayName("分页查询用户列表成功")
        void whenListUsers_thenReturnPagedResults() {
            // Given
            UserPageRequest request = new UserPageRequest();
            request.setPageNum(1);
            request.setPageSize(10);

            List<User> users = Arrays.asList(testUser, createTestUser(2L, "user2"));
            when(userMapper.findAllWithPagination(0, 10)).thenReturn(users);
            when(userMapper.countTotal()).thenReturn(2L);

            // When
            PageResponse<UserVO> response = userService.listUsers(request);

            // Then
            assertNotNull(response);
            assertEquals(2, response.getRecords().size());
            assertEquals(2L, response.getTotal());
            assertEquals(1, response.getPageNum());
            assertEquals(10, response.getPageSize());

            // 验证VO转换
            UserVO firstUser = response.getRecords().get(0);
            assertEquals(testUser.getId(), firstUser.getId());
            assertEquals(testUser.getUsername(), firstUser.getUsername());
        }

        @Test
        @DisplayName("分页查询 - 第二页")
        void whenListUsersSecondPage_thenCalculateOffsetCorrectly() {
            // Given
            UserPageRequest request = new UserPageRequest();
            request.setPageNum(2);
            request.setPageSize(5);

            List<User> users = Arrays.asList(createTestUser(6L, "user6"));
            when(userMapper.findAllWithPagination(5, 5)).thenReturn(users);
            when(userMapper.countTotal()).thenReturn(10L);

            // When
            PageResponse<UserVO> response = userService.listUsers(request);

            // Then
            assertEquals(1, response.getRecords().size());
            assertEquals(10L, response.getTotal());
            assertEquals(2, response.getPageNum());
            assertEquals(5, response.getPageSize());
        }

        @Test
        @DisplayName("分页查询 - 空结果")
        void whenListUsersEmptyResult_thenReturnEmptyPage() {
            // Given
            UserPageRequest request = new UserPageRequest();
            request.setPageNum(1);
            request.setPageSize(10);

            when(userMapper.findAllWithPagination(0, 10)).thenReturn(Arrays.asList());
            when(userMapper.countTotal()).thenReturn(0L);

            // When
            PageResponse<UserVO> response = userService.listUsers(request);

            // Then
            assertNotNull(response);
            assertTrue(response.getRecords().isEmpty());
            assertEquals(0L, response.getTotal());
        }
    }

    @Nested
    @DisplayName("Redis缓存测试")
    class RedisCacheTests {

        @Test
        @DisplayName("Redis缓存失败不影响登录流程")
        void whenRedisCacheFails_thenContinueLogin() {
            // Given
            loginRequest.setUsername("admin");
            loginRequest.setPassword("admin123");
            when(userMapper.findByUsername("admin")).thenReturn(testUser);
            doThrow(new RuntimeException("Redis error")).when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

            // When
            LoginResponse response = userService.login(loginRequest);

            // Then
            assertNotNull(response);
            assertNotNull(response.getToken());
            verify(userMapper).updateUserToken(any(User.class));
        }

        @Test
        @DisplayName("登录失败次数记录")
        void whenLoginFails_thenIncrementFailCounter() {
            // Given
            loginRequest.setUsername("admin");
            loginRequest.setPassword("wrongpassword");
            when(valueOperations.increment("login:fail")).thenReturn(1L);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> userService.login(loginRequest));
            
            verify(valueOperations).increment("login:fail");
            verify(redisTemplate).expire("login:fail", 1, TimeUnit.DAYS);
        }

        @Test
        @DisplayName("登录失败次数记录 - 不是第一次失败")
        void whenMultipleLoginFailures_thenOnlyIncrementCounter() {
            // Given
            loginRequest.setUsername("admin");
            loginRequest.setPassword("wrongpassword");
            when(valueOperations.increment("login:fail")).thenReturn(5L);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> userService.login(loginRequest));
            
            verify(valueOperations).increment("login:fail");
            verify(redisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
        }
    }

    @Nested
    @DisplayName("边界条件和异常测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("用户对象转换 - null用户")
        void whenConvertNullUser_thenReturnNull() throws Exception {
            // 使用反射调用私有方法进行测试
            User result = (User) ReflectionTestUtils.invokeMethod(userService, "convertToVO", (User) null);
            assertNull(result);
        }

        @Test
        @DisplayName("JSON序列化异常处理")
        void whenJsonSerializationFails_thenContinueLogin() {
            // Given
            loginRequest.setUsername("admin");
            loginRequest.setPassword("admin123");
            
            User circularUser = spy(testUser);
            when(userMapper.findByUsername("admin")).thenReturn(circularUser);
            
            // 模拟JSON序列化失败的情况
            UserServiceImpl spyService = spy(userService);
            doThrow(new RuntimeException("JSON error")).when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

            // When
            LoginResponse response = spyService.login(loginRequest);

            // Then
            assertNotNull(response);
            assertNotNull(response.getToken());
        }
    }

    // 辅助方法
    private User createTestUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}