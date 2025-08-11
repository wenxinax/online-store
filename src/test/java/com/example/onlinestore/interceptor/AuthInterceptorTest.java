package com.example.onlinestore.interceptor;

import com.example.onlinestore.context.UserContext;
import com.example.onlinestore.model.User;
import com.example.onlinestore.service.UserService;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("认证拦截器测试")
class AuthInterceptorTest {

    @Mock
    private UserService userService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Object handler;
    private User testUser;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        handler = new Object();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setToken("valid-token");
        testUser.setTokenExpireTime(LocalDateTime.now().plusHours(1));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        
        // 设置默认的消息源行为
        when(messageSource.getMessage(eq("error.unauthorized"), isNull(), any(Locale.class)))
            .thenReturn("Unauthorized access");
        
        // 清除用户上下文
        UserContext.clear();
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Nested
    @DisplayName("preHandle方法测试")
    class PreHandleTests {

        @Test
        @DisplayName("有效Token - 认证成功")
        void whenValidToken_thenAuthenticationSucceeds() throws Exception {
            // Given
            request.addHeader("X-Token", "valid-token");
            when(userService.getUserByToken("valid-token")).thenReturn(testUser);

            // When
            boolean result = authInterceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);
            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            assertEquals(testUser, UserContext.getCurrentUser());
        }

        @Test
        @DisplayName("缺少Token头 - 返回401")
        void whenMissingToken_thenReturnUnauthorized() throws Exception {
            // Given - 不设置X-Token头

            // When
            boolean result = authInterceptor.preHandle(request, response, handler);

            // Then
            assertFalse(result);
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
            assertEquals("Unauthorized access", response.getContentAsString());
            assertNull(UserContext.getCurrentUser());
        }

        @Test
        @DisplayName("空Token值 - 返回401")
        void whenEmptyToken_thenReturnUnauthorized() throws Exception {
            // Given
            request.addHeader("X-Token", "");

            // When
            boolean result = authInterceptor.preHandle(request, response, handler);

            // Then
            assertFalse(result);
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
            assertEquals("Unauthorized access", response.getContentAsString());
        }

        @Test
        @DisplayName("null Token值 - 返回401")
        void whenNullToken_thenReturnUnauthorized() throws Exception {
            // Given
            request.addHeader("X-Token", (String) null);

            // When
            boolean result = authInterceptor.preHandle(request, response, handler);

            // Then
            assertFalse(result);
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        }

        @Test
        @DisplayName("无效Token - 用户不存在")
        void whenInvalidToken_thenReturnUnauthorized() throws Exception {
            // Given
            request.addHeader("X-Token", "invalid-token");
            when(userService.getUserByToken("invalid-token")).thenReturn(null);

            // When
            boolean result = authInterceptor.preHandle(request, response, handler);

            // Then
            assertFalse(result);
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
            assertEquals("Unauthorized access", response.getContentAsString());
            assertNull(UserContext.getCurrentUser());
        }

        @Test
        @DisplayName("UserService异常 - 处理异常")
        void whenUserServiceThrowsException_thenHandleGracefully() throws Exception {
            // Given
            request.addHeader("X-Token", "valid-token");
            when(userService.getUserByToken("valid-token"))
                    .thenThrow(new RuntimeException("Service unavailable"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                authInterceptor.preHandle(request, response, handler);
            });
        }

        @Test
        @DisplayName("多种Token格式支持")
        void whenDifferentTokenFormats_thenHandleCorrectly() throws Exception {
            // Test UUID-like token
            String uuidToken = "550e8400-e29b-41d4-a716-446655440000";
            request.addHeader("X-Token", uuidToken);
            when(userService.getUserByToken(uuidToken)).thenReturn(testUser);

            boolean result1 = authInterceptor.preHandle(request, response, handler);
            assertTrue(result1);

            // Reset
            UserContext.clear();
            response = new MockHttpServletResponse();

            // Test simple token
            String simpleToken = "abc123";
            request = new MockHttpServletRequest();
            request.addHeader("X-Token", simpleToken);
            when(userService.getUserByToken(simpleToken)).thenReturn(testUser);

            boolean result2 = authInterceptor.preHandle(request, response, handler);
            assertTrue(result2);
        }
    }

    @Nested
    @DisplayName("afterCompletion方法测试")
    class AfterCompletionTests {

        @Test
        @DisplayName("正常完成请求 - 清理用户上下文")
        void whenRequestCompletes_thenClearUserContext() throws Exception {
            // Given - 设置用户上下文
            UserContext.setCurrentUser(testUser);
            assertNotNull(UserContext.getCurrentUser());

            // When
            authInterceptor.afterCompletion(request, response, handler, null);

            // Then
            assertNull(UserContext.getCurrentUser());
        }

        @Test
        @DisplayName("请求完成时有异常 - 仍然清理上下文")
        void whenRequestCompletesWithException_thenStillClearUserContext() throws Exception {
            // Given
            UserContext.setCurrentUser(testUser);
            Exception ex = new RuntimeException("Request processing error");

            // When
            authInterceptor.afterCompletion(request, response, handler, ex);

            // Then
            assertNull(UserContext.getCurrentUser());
        }

        @Test
        @DisplayName("用户上下文为空时 - 清理操作不抛异常")
        void whenUserContextIsEmpty_thenClearDoesNotThrow() throws Exception {
            // Given - 确保用户上下文为空
            UserContext.clear();
            assertNull(UserContext.getCurrentUser());

            // When & Then - 不应该抛出异常
            assertDoesNotThrow(() -> {
                authInterceptor.afterCompletion(request, response, handler, null);
            });
        }

        @Test
        @DisplayName("多次调用afterCompletion - 不出错")
        void whenAfterCompletionCalledMultipleTimes_thenNoError() throws Exception {
            // Given
            UserContext.setCurrentUser(testUser);

            // When - 多次调用
            authInterceptor.afterCompletion(request, response, handler, null);
            authInterceptor.afterCompletion(request, response, handler, null);
            authInterceptor.afterCompletion(request, response, handler, null);

            // Then
            assertNull(UserContext.getCurrentUser());
        }
    }

    @Nested
    @DisplayName("国际化消息测试")
    class InternationalizationTests {

        @Test
        @DisplayName("英文错误消息")
        void whenEnglishLocale_thenReturnEnglishMessage() throws Exception {
            // Given
            LocaleContextHolder.setLocale(Locale.ENGLISH);
            when(messageSource.getMessage(eq("error.unauthorized"), isNull(), eq(Locale.ENGLISH)))
                    .thenReturn("Access denied");

            // When
            boolean result = authInterceptor.preHandle(request, response, handler);

            // Then
            assertFalse(result);
            assertEquals("Access denied", response.getContentAsString());
        }

        @Test
        @DisplayName("中文错误消息")
        void whenChineseLocale_thenReturnChineseMessage() throws Exception {
            // Given
            LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE);
            when(messageSource.getMessage(eq("error.unauthorized"), isNull(), eq(Locale.SIMPLIFIED_CHINESE)))
                    .thenReturn("访问被拒绝");

            // When
            boolean result = authInterceptor.preHandle(request, response, handler);

            // Then
            assertFalse(result);
            assertEquals("访问被拒绝", response.getContentAsString());
        }

        @Test
        @DisplayName("默认语言环境")
        void whenDefaultLocale_thenReturnDefaultMessage() throws Exception {
            // Given
            LocaleContextHolder.setLocale(null);
            // MessageSource 应该使用默认的 Locale

            // When
            boolean result = authInterceptor.preHandle(request, response, handler);

            // Then
            assertFalse(result);
            verify(messageSource).getMessage(eq("error.unauthorized"), isNull(), any(Locale.class));
        }
    }

    @Nested
    @DisplayName("完整工作流测试")
    class WorkflowTests {

        @Test
        @DisplayName("完整的成功认证流程")
        void testCompleteSuccessfulAuthenticationFlow() throws Exception {
            // Given
            request.addHeader("X-Token", "valid-token");
            when(userService.getUserByToken("valid-token")).thenReturn(testUser);

            // When - preHandle
            boolean preHandleResult = authInterceptor.preHandle(request, response, handler);

            // Then - 验证preHandle结果
            assertTrue(preHandleResult);
            assertEquals(testUser, UserContext.getCurrentUser());

            // When - afterCompletion
            authInterceptor.afterCompletion(request, response, handler, null);

            // Then - 验证清理结果
            assertNull(UserContext.getCurrentUser());
        }

        @Test
        @DisplayName("完整的失败认证流程")
        void testCompleteFailedAuthenticationFlow() throws Exception {
            // Given - 无Token

            // When - preHandle
            boolean preHandleResult = authInterceptor.preHandle(request, response, handler);

            // Then - 验证preHandle结果
            assertFalse(preHandleResult);
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
            assertNull(UserContext.getCurrentUser());

            // When - afterCompletion (通常不会被调用，但测试健壮性)
            authInterceptor.afterCompletion(request, response, handler, null);

            // Then - 确认清理操作不出错
            assertNull(UserContext.getCurrentUser());
        }

        @Test
        @DisplayName("Token过期场景模拟")
        void testExpiredTokenScenario() throws Exception {
            // Given
            String expiredToken = "expired-token";
            request.addHeader("X-Token", expiredToken);
            when(userService.getUserByToken(expiredToken)).thenReturn(null); // 模拟过期Token

            // When
            boolean result = authInterceptor.preHandle(request, response, handler);

            // Then
            assertFalse(result);
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
            assertNull(UserContext.getCurrentUser());
        }
    }

    @Nested
    @DisplayName("并发安全性测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("多线程环境下用户上下文隔离")
        void testUserContextIsolationInMultiThreads() throws InterruptedException {
            // Given
            User user1 = new User();
            user1.setId(1L);
            user1.setUsername("user1");

            User user2 = new User();
            user2.setId(2L);
            user2.setUsername("user2");

            // When - 并发设置和获取用户上下文
            Thread thread1 = new Thread(() -> {
                UserContext.setCurrentUser(user1);
                try {
                    Thread.sleep(100); // 模拟处理时间
                    assertEquals(user1, UserContext.getCurrentUser());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    UserContext.clear();
                }
            });

            Thread thread2 = new Thread(() -> {
                UserContext.setCurrentUser(user2);
                try {
                    Thread.sleep(100); // 模拟处理时间
                    assertEquals(user2, UserContext.getCurrentUser());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    UserContext.clear();
                }
            });

            // Then
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

            // 最终确认都已清理
            assertNull(UserContext.getCurrentUser());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("极长Token处理")
        void whenVeryLongToken_thenHandleCorrectly() throws Exception {
            // Given
            String longToken = "a".repeat(1000);
            request.addHeader("X-Token", longToken);
            when(userService.getUserByToken(longToken)).thenReturn(testUser);

            // When
            boolean result = authInterceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);
            assertEquals(testUser, UserContext.getCurrentUser());
        }

        @Test
        @DisplayName("特殊字符Token处理")
        void whenSpecialCharacterToken_thenHandleCorrectly() throws Exception {
            // Given
            String specialToken = "token-with-special-chars!@#$%^&*()";
            request.addHeader("X-Token", specialToken);
            when(userService.getUserByToken(specialToken)).thenReturn(testUser);

            // When
            boolean result = authInterceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("重复Token头处理")
        void whenMultipleTokenHeaders_thenUseFirst() throws Exception {
            // Given
            request.addHeader("X-Token", "first-token");
            request.addHeader("X-Token", "second-token");
            when(userService.getUserByToken("first-token")).thenReturn(testUser);

            // When
            boolean result = authInterceptor.preHandle(request, response, handler);

            // Then
            assertTrue(result);
            verify(userService).getUserByToken("first-token");
            verify(userService, never()).getUserByToken("second-token");
        }
    }
}