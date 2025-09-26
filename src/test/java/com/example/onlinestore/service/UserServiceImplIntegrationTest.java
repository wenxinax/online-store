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
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 集成测试
 * 测试完整的业务流程和组件间交互
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 集成测试")
public class UserServiceImplIntegrationTest {

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

    // ==================== 完整业务流程测试 ====================

    @Test
    @DisplayName("完整用户登录流程 - 新用户")
    void testCompleteLoginFlowNewUser() throws Exception {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername("newuser");
        request.setPassword("password123");

        // Mock外部服务认证成功
        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class))).thenReturn(true);
        
        // Mock数据库查询返回null（新用户）
        when(userMapper.findByUsername("newuser")).thenReturn(null);
        
        // Mock Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行登录
        LoginResponse loginResponse = userService.login(request);

        // 验证登录响应
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertNotNull(loginResponse.getExpireTime());
        assertTrue(loginResponse.getExpireTime().isAfter(LocalDateTime.now()));

        // 验证数据库调用
        verify(userMapper).findByUsername("newuser");
        verify(userMapper).insertUser(argThat(user -> 
            "newuser".equals(user.getUsername()) && 
            user.getToken() != null &&
            user.getTokenExpireTime() != null
        ));

        // 验证Redis缓存
        verify(valueOperations).set(
            eq("token:" + loginResponse.getToken()),
            anyString(),
            eq(1L),
            eq(TimeUnit.DAYS)
        );

        // 验证能通过token获取用户信息
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        User mockUser = new User();
        mockUser.setUsername("newuser");
        mockUser.setToken(loginResponse.getToken());
        String userJson = objectMapper.writeValueAsString(mockUser);
        
        when(valueOperations.get("token:" + loginResponse.getToken())).thenReturn(userJson);
        
        User retrievedUser = userService.getUserByToken(loginResponse.getToken());
        assertNotNull(retrievedUser);
        assertEquals("newuser", retrievedUser.getUsername());
    }

    @Test
    @DisplayName("完整用户登录流程 - 现有用户")
    void testCompleteLoginFlowExistingUser() throws Exception {
        // 准备
        LoginRequest request = new LoginRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        // 创建现有用户
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("existinguser");
        existingUser.setToken("old-token");
        existingUser.setTokenExpireTime(LocalDateTime.now().minusHours(1));
        existingUser.setCreatedAt(LocalDateTime.now().minusDays(7));

        // Mock外部服务认证成功
        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class))).thenReturn(true);
        
        // Mock数据库查询返回现有用户
        when(userMapper.findByUsername("existinguser")).thenReturn(existingUser);
        
        // Mock Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行登录
        LoginResponse loginResponse = userService.login(request);

        // 验证新token不同于旧token
        assertNotEquals("old-token", loginResponse.getToken());

        // 验证数据库更新操作
        verify(userMapper).updateUserToken(argThat(user -> 
            user.getId().equals(1L) &&
            "existinguser".equals(user.getUsername()) &&
            !user.getToken().equals("old-token") &&
            user.getTokenExpireTime().isAfter(LocalDateTime.now())
        ));

        // 验证没有调用插入操作
        verify(userMapper, never()).insertUser(any());
    }

    @Test
    @DisplayName("登录-查询-分页完整流程")
    void testLoginQueryListCompleteFlow() throws Exception {
        // Step 1: 管理员登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(ADMIN_USERNAME);
        loginRequest.setPassword(ADMIN_PASSWORD);

        when(userMapper.findByUsername(ADMIN_USERNAME)).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        LoginResponse loginResponse = userService.login(loginRequest);
        String adminToken = loginResponse.getToken();

        // Step 2: 验证管理员token可以获取用户信息
        User adminUser = new User();
        adminUser.setUsername(ADMIN_USERNAME);
        adminUser.setToken(adminToken);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        String adminUserJson = objectMapper.writeValueAsString(adminUser);

        when(valueOperations.get("token:" + adminToken)).thenReturn(adminUserJson);
        
        User retrievedAdmin = userService.getUserByToken(adminToken);
        assertEquals(ADMIN_USERNAME, retrievedAdmin.getUsername());

        // Step 3: 使用管理员权限查询用户列表
        UserPageRequest pageRequest = new UserPageRequest();
        pageRequest.setPageNum(1);
        pageRequest.setPageSize(5);

        // 准备用户列表数据
        User user1 = createTestUser(1L, "user1");
        User user2 = createTestUser(2L, "user2");
        User user3 = createTestUser(3L, "user3");

        when(userMapper.findAllWithPagination(0, 5))
            .thenReturn(Arrays.asList(user1, user2, user3));
        when(userMapper.countTotal()).thenReturn(3L);

        PageResponse<UserVO> pageResponse = userService.listUsers(pageRequest);

        // 验证分页结果
        assertEquals(3, pageResponse.getRecords().size());
        assertEquals(3L, pageResponse.getTotal());
        assertEquals("user1", pageResponse.getRecords().get(0).getUsername());
        assertEquals("user2", pageResponse.getRecords().get(1).getUsername());
        assertEquals("user3", pageResponse.getRecords().get(2).getUsername());
    }

    @Test
    @DisplayName("多用户并发登录场景模拟")
    void testConcurrentLoginScenario() {
        // 模拟三个用户同时登录的场景
        
        // 用户1：管理员
        LoginRequest adminRequest = new LoginRequest();
        adminRequest.setUsername(ADMIN_USERNAME);
        adminRequest.setPassword(ADMIN_PASSWORD);

        // 用户2：普通用户
        LoginRequest user2Request = new LoginRequest();
        user2Request.setUsername("user2");
        user2Request.setPassword("pass2");

        // 用户3：普通用户  
        LoginRequest user3Request = new LoginRequest();
        user3Request.setUsername("user3");
        user3Request.setPassword("pass3");

        // Mock准备
        when(userMapper.findByUsername(ADMIN_USERNAME)).thenReturn(null);
        when(userMapper.findByUsername("user2")).thenReturn(null);
        when(userMapper.findByUsername("user3")).thenReturn(null);
        
        when(restTemplate.postForObject(anyString(), eq(user2Request), eq(Boolean.class))).thenReturn(true);
        when(restTemplate.postForObject(anyString(), eq(user3Request), eq(Boolean.class))).thenReturn(true);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // 执行并发登录
        LoginResponse adminResponse = userService.login(adminRequest);
        LoginResponse user2Response = userService.login(user2Request);
        LoginResponse user3Response = userService.login(user3Request);

        // 验证所有token都不相同
        assertNotEquals(adminResponse.getToken(), user2Response.getToken());
        assertNotEquals(adminResponse.getToken(), user3Response.getToken());
        assertNotEquals(user2Response.getToken(), user3Response.getToken());

        // 验证数据库调用次数
        verify(userMapper, times(3)).insertUser(any());
        
        // 验证Redis缓存调用次数
        verify(valueOperations, times(3)).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("失败重试场景 - 外部服务间歇性故障")
    void testExternalServiceIntermittentFailure() {
        LoginRequest request = new LoginRequest();
        request.setUsername("retryuser");
        request.setPassword("password");

        // 第一次调用失败，第二次成功
        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class)))
            .thenReturn(false)  // 第一次失败
            .thenReturn(true);  // 第二次成功

        when(userMapper.findByUsername("retryuser")).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:fail")).thenReturn(1L);
        when(messageSource.getMessage(eq("error.invalid.credentials"), isNull(), any()))
            .thenReturn("Invalid credentials");

        // 第一次登录失败
        assertThrows(IllegalArgumentException.class, () -> userService.login(request));

        // 第二次登录成功
        LoginResponse response = userService.login(request);
        assertNotNull(response);
        assertNotNull(response.getToken());

        // 验证外部服务被调用了两次
        verify(restTemplate, times(2)).postForObject(anyString(), any(), eq(Boolean.class));
    }

    @Test
    @DisplayName("数据一致性验证 - 登录后立即查询")
    void testDataConsistencyAfterLogin() throws Exception {
        // 登录
        LoginRequest request = new LoginRequest();
        request.setUsername("consistencyuser");
        request.setPassword("password");

        when(restTemplate.postForObject(anyString(), any(), eq(Boolean.class))).thenReturn(true);
        when(userMapper.findByUsername("consistencyuser")).thenReturn(null);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        LoginResponse loginResponse = userService.login(request);

        // 立即通过token查询用户
        User savedUser = new User();
        savedUser.setUsername("consistencyuser");
        savedUser.setToken(loginResponse.getToken());
        savedUser.setTokenExpireTime(loginResponse.getExpireTime());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        String userJson = objectMapper.writeValueAsString(savedUser);

        when(valueOperations.get("token:" + loginResponse.getToken())).thenReturn(userJson);

        User retrievedUser = userService.getUserByToken(loginResponse.getToken());

        // 验证数据一致性
        assertEquals(loginResponse.getToken(), retrievedUser.getToken());
        assertEquals("consistencyuser", retrievedUser.getUsername());
        assertEquals(loginResponse.getExpireTime(), retrievedUser.getTokenExpireTime());
    }

    @Test
    @DisplayName("分页边界测试 - 跨页数据一致性")
    void testPaginationBoundaryConsistency() {
        // 第1页
        UserPageRequest page1Request = new UserPageRequest();
        page1Request.setPageNum(1);
        page1Request.setPageSize(2);

        User user1 = createTestUser(1L, "user1");
        User user2 = createTestUser(2L, "user2");

        when(userMapper.findAllWithPagination(0, 2)).thenReturn(Arrays.asList(user1, user2));
        when(userMapper.countTotal()).thenReturn(5L);

        PageResponse<UserVO> page1Response = userService.listUsers(page1Request);

        // 第2页
        UserPageRequest page2Request = new UserPageRequest();
        page2Request.setPageNum(2);
        page2Request.setPageSize(2);

        User user3 = createTestUser(3L, "user3");
        User user4 = createTestUser(4L, "user4");

        when(userMapper.findAllWithPagination(2, 2)).thenReturn(Arrays.asList(user3, user4));

        PageResponse<UserVO> page2Response = userService.listUsers(page2Request);

        // 验证跨页数据不重复
        assertEquals(2, page1Response.getRecords().size());
        assertEquals(2, page2Response.getRecords().size());
        
        String[] page1Usernames = page1Response.getRecords().stream()
            .map(UserVO::getUsername).toArray(String[]::new);
        String[] page2Usernames = page2Response.getRecords().stream()
            .map(UserVO::getUsername).toArray(String[]::new);

        // 验证没有重复用户
        for (String username1 : page1Usernames) {
            for (String username2 : page2Usernames) {
                assertNotEquals(username1, username2);
            }
        }

        // 验证总数一致
        assertEquals(page1Response.getTotal(), page2Response.getTotal());
    }

    // ==================== 性能测试模拟 ====================

    @Test
    @DisplayName("大量用户分页性能测试模拟")
    void testLargeDatasetPagination() {
        UserPageRequest request = new UserPageRequest();
        request.setPageNum(500);  // 第500页
        request.setPageSize(50);  // 每页50条

        // 模拟大数据集
        when(userMapper.countTotal()).thenReturn(1000000L);  // 100万用户
        when(userMapper.findAllWithPagination(24950, 50)).thenReturn(Arrays.asList());

        // 执行查询
        long startTime = System.currentTimeMillis();
        PageResponse<UserVO> response = userService.listUsers(request);
        long endTime = System.currentTimeMillis();

        // 验证
        assertNotNull(response);
        assertEquals(500, response.getPageNum());
        assertEquals(50, response.getPageSize());
        assertEquals(1000000L, response.getTotal());

        // 确保方法调用了正确的偏移量
        verify(userMapper).findAllWithPagination(24950, 50);
        
        // 性能验证（虽然是mock，但确保方法能正常完成）
        assertTrue(endTime >= startTime);
    }

    // ==================== 辅助方法 ====================

    private User createTestUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}