package com.example.onlinestore.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoginResponse DTO 测试
 */
@DisplayName("登录响应DTO测试")
public class LoginResponseTest {

    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginResponse = new LoginResponse();
    }

    @Nested
    @DisplayName("Token字段测试")
    class TokenTests {

        @Test
        @DisplayName("设置和获取token")
        void testTokenGetterSetter() {
            // Arrange
            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

            // Act
            loginResponse.setToken(token);

            // Assert
            assertEquals(token, loginResponse.getToken());
        }

        @Test
        @DisplayName("设置null token")
        void testSetNullToken() {
            // Act
            loginResponse.setToken(null);

            // Assert
            assertNull(loginResponse.getToken());
        }

        @Test
        @DisplayName("设置空字符串token")
        void testSetEmptyToken() {
            // Arrange
            String emptyToken = "";

            // Act
            loginResponse.setToken(emptyToken);

            // Assert
            assertEquals(emptyToken, loginResponse.getToken());
        }

        @Test
        @DisplayName("设置长token字符串")
        void testLongTokenString() {
            // Arrange
            String longToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

            // Act
            loginResponse.setToken(longToken);

            // Assert
            assertEquals(longToken, loginResponse.getToken());
        }
    }

    @Nested
    @DisplayName("ExpireTime字段测试")
    class ExpireTimeTests {

        @Test
        @DisplayName("设置和获取过期时间")
        void testExpireTimeGetterSetter() {
            // Arrange
            LocalDateTime expireTime = LocalDateTime.of(2024, 12, 31, 23, 59, 59);

            // Act
            loginResponse.setExpireTime(expireTime);

            // Assert
            assertEquals(expireTime, loginResponse.getExpireTime());
        }

        @Test
        @DisplayName("设置null过期时间")
        void testSetNullExpireTime() {
            // Act
            loginResponse.setExpireTime(null);

            // Assert
            assertNull(loginResponse.getExpireTime());
        }

        @Test
        @DisplayName("设置当前时间作为过期时间")
        void testSetCurrentTimeAsExpireTime() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            loginResponse.setExpireTime(now);

            // Assert
            assertEquals(now, loginResponse.getExpireTime());
        }

        @Test
        @DisplayName("设置未来时间作为过期时间")
        void testSetFutureTimeAsExpireTime() {
            // Arrange
            LocalDateTime futureTime = LocalDateTime.now().plusHours(2);

            // Act
            loginResponse.setExpireTime(futureTime);

            // Assert
            assertEquals(futureTime, loginResponse.getExpireTime());
            assertTrue(loginResponse.getExpireTime().isAfter(LocalDateTime.now()));
        }

        @Test
        @DisplayName("设置过去时间作为过期时间")
        void testSetPastTimeAsExpireTime() {
            // Arrange
            LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

            // Act
            loginResponse.setExpireTime(pastTime);

            // Assert
            assertEquals(pastTime, loginResponse.getExpireTime());
            assertTrue(loginResponse.getExpireTime().isBefore(LocalDateTime.now()));
        }
    }

    @Nested
    @DisplayName("对象完整性测试")
    class ObjectIntegrityTests {

        @Test
        @DisplayName("创建完整的登录响应对象")
        void testCompleteLoginResponse() {
            // Arrange
            String token = "test-token-123";
            LocalDateTime expireTime = LocalDateTime.now().plusHours(2);

            // Act
            loginResponse.setToken(token);
            loginResponse.setExpireTime(expireTime);

            // Assert
            assertEquals(token, loginResponse.getToken());
            assertEquals(expireTime, loginResponse.getExpireTime());
        }

        @Test
        @DisplayName("新创建的对象字段为null")
        void testNewObjectHasNullFields() {
            // Arrange
            LoginResponse newResponse = new LoginResponse();

            // Assert
            assertNull(newResponse.getToken());
            assertNull(newResponse.getExpireTime());
        }

        @Test
        @DisplayName("测试对象字段独立性")
        void testFieldIndependence() {
            // Arrange
            String token1 = "token1";
            String token2 = "token2";
            LocalDateTime expireTime1 = LocalDateTime.now().plusHours(1);
            LocalDateTime expireTime2 = LocalDateTime.now().plusHours(2);

            // Act & Assert
            loginResponse.setToken(token1);
            loginResponse.setExpireTime(expireTime1);
            assertEquals(token1, loginResponse.getToken());
            assertEquals(expireTime1, loginResponse.getExpireTime());

            loginResponse.setToken(token2);
            assertEquals(token2, loginResponse.getToken());
            assertEquals(expireTime1, loginResponse.getExpireTime()); // expireTime should remain unchanged

            loginResponse.setExpireTime(expireTime2);
            assertEquals(token2, loginResponse.getToken()); // token should remain unchanged
            assertEquals(expireTime2, loginResponse.getExpireTime());
        }
    }

    @Nested
    @DisplayName("业务场景测试")
    class BusinessScenarioTests {

        @Test
        @DisplayName("创建典型的成功登录响应")
        void testTypicalSuccessLoginResponse() {
            // Arrange
            String expectedToken = "jwt-token-example";
            LocalDateTime expectedExpireTime = LocalDateTime.now().plusHours(24);

            // Act
            loginResponse.setToken(expectedToken);
            loginResponse.setExpireTime(expectedExpireTime);

            // Assert
            assertNotNull(loginResponse.getToken());
            assertNotNull(loginResponse.getExpireTime());
            assertFalse(loginResponse.getToken().isEmpty());
            assertTrue(loginResponse.getExpireTime().isAfter(LocalDateTime.now()));
        }

        @Test
        @DisplayName("验证token过期时间在未来")
        void testTokenExpirationInFuture() {
            // Arrange
            LocalDateTime futureTime = LocalDateTime.now().plusMinutes(30);

            // Act
            loginResponse.setExpireTime(futureTime);

            // Assert
            assertTrue(loginResponse.getExpireTime().isAfter(LocalDateTime.now()));
        }
    }
}