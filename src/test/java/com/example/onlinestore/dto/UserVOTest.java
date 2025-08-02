package com.example.onlinestore.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserVO DTO 测试
 */
@DisplayName("用户视图对象DTO测试")
public class UserVOTest {

    private UserVO userVO;

    @BeforeEach
    void setUp() {
        userVO = new UserVO();
    }

    @Nested
    @DisplayName("ID字段测试")
    class IdTests {

        @Test
        @DisplayName("设置和获取ID")
        void testIdGetterSetter() {
            // Arrange
            Long id = 123L;

            // Act
            userVO.setId(id);

            // Assert
            assertEquals(id, userVO.getId());
        }

        @Test
        @DisplayName("设置null ID")
        void testSetNullId() {
            // Act
            userVO.setId(null);

            // Assert
            assertNull(userVO.getId());
        }

        @Test
        @DisplayName("设置负数ID")
        void testSetNegativeId() {
            // Arrange
            Long negativeId = -1L;

            // Act
            userVO.setId(negativeId);

            // Assert
            assertEquals(negativeId, userVO.getId());
        }

        @Test
        @DisplayName("设置零ID")
        void testSetZeroId() {
            // Arrange
            Long zeroId = 0L;

            // Act
            userVO.setId(zeroId);

            // Assert
            assertEquals(zeroId, userVO.getId());
        }

        @Test
        @DisplayName("设置大整数ID")
        void testSetLargeId() {
            // Arrange
            Long largeId = Long.MAX_VALUE;

            // Act
            userVO.setId(largeId);

            // Assert
            assertEquals(largeId, userVO.getId());
        }
    }

    @Nested
    @DisplayName("Username字段测试")
    class UsernameTests {

        @Test
        @DisplayName("设置和获取用户名")
        void testUsernameGetterSetter() {
            // Arrange
            String username = "testuser";

            // Act
            userVO.setUsername(username);

            // Assert
            assertEquals(username, userVO.getUsername());
        }

        @Test
        @DisplayName("设置null用户名")
        void testSetNullUsername() {
            // Act
            userVO.setUsername(null);

            // Assert
            assertNull(userVO.getUsername());
        }

        @Test
        @DisplayName("设置空字符串用户名")
        void testSetEmptyUsername() {
            // Arrange
            String emptyUsername = "";

            // Act
            userVO.setUsername(emptyUsername);

            // Assert
            assertEquals(emptyUsername, userVO.getUsername());
        }

        @Test
        @DisplayName("设置包含特殊字符的用户名")
        void testUsernameWithSpecialCharacters() {
            // Arrange
            String specialUsername = "user@domain.com";

            // Act
            userVO.setUsername(specialUsername);

            // Assert
            assertEquals(specialUsername, userVO.getUsername());
        }

        @Test
        @DisplayName("设置长用户名")
        void testLongUsername() {
            // Arrange
            String longUsername = "a".repeat(255); // 255个字符的用户名

            // Act
            userVO.setUsername(longUsername);

            // Assert
            assertEquals(longUsername, userVO.getUsername());
        }

        @Test
        @DisplayName("设置包含中文的用户名")
        void testUsernameWithChinese() {
            // Arrange
            String chineseUsername = "测试用户";

            // Act
            userVO.setUsername(chineseUsername);

            // Assert
            assertEquals(chineseUsername, userVO.getUsername());
        }
    }

    @Nested
    @DisplayName("CreatedAt字段测试")
    class CreatedAtTests {

        @Test
        @DisplayName("设置和获取创建时间")
        void testCreatedAtGetterSetter() {
            // Arrange
            LocalDateTime createdAt = LocalDateTime.of(2023, 1, 1, 10, 30, 0);

            // Act
            userVO.setCreatedAt(createdAt);

            // Assert
            assertEquals(createdAt, userVO.getCreatedAt());
        }

        @Test
        @DisplayName("设置null创建时间")
        void testSetNullCreatedAt() {
            // Act
            userVO.setCreatedAt(null);

            // Assert
            assertNull(userVO.getCreatedAt());
        }

        @Test
        @DisplayName("设置当前时间作为创建时间")
        void testSetCurrentTimeAsCreatedAt() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            userVO.setCreatedAt(now);

            // Assert
            assertEquals(now, userVO.getCreatedAt());
        }

        @Test
        @DisplayName("设置过去时间作为创建时间")
        void testSetPastTimeAsCreatedAt() {
            // Arrange
            LocalDateTime pastTime = LocalDateTime.now().minusDays(30);

            // Act
            userVO.setCreatedAt(pastTime);

            // Assert
            assertEquals(pastTime, userVO.getCreatedAt());
            assertTrue(userVO.getCreatedAt().isBefore(LocalDateTime.now()));
        }

        @Test
        @DisplayName("设置未来时间作为创建时间")
        void testSetFutureTimeAsCreatedAt() {
            // Arrange
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

            // Act
            userVO.setCreatedAt(futureTime);

            // Assert
            assertEquals(futureTime, userVO.getCreatedAt());
            assertTrue(userVO.getCreatedAt().isAfter(LocalDateTime.now()));
        }
    }

    @Nested
    @DisplayName("UpdatedAt字段测试")
    class UpdatedAtTests {

        @Test
        @DisplayName("设置和获取更新时间")
        void testUpdatedAtGetterSetter() {
            // Arrange
            LocalDateTime updatedAt = LocalDateTime.of(2023, 6, 15, 14, 20, 30);

            // Act
            userVO.setUpdatedAt(updatedAt);

            // Assert
            assertEquals(updatedAt, userVO.getUpdatedAt());
        }

        @Test
        @DisplayName("设置null更新时间")
        void testSetNullUpdatedAt() {
            // Act
            userVO.setUpdatedAt(null);

            // Assert
            assertNull(userVO.getUpdatedAt());
        }

        @Test
        @DisplayName("设置当前时间作为更新时间")
        void testSetCurrentTimeAsUpdatedAt() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            userVO.setUpdatedAt(now);

            // Assert
            assertEquals(now, userVO.getUpdatedAt());
        }

        @Test
        @DisplayName("更新时间应该晚于或等于创建时间")
        void testUpdatedAtAfterCreatedAt() {
            // Arrange
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
            LocalDateTime updatedAt = LocalDateTime.now();

            // Act
            userVO.setCreatedAt(createdAt);
            userVO.setUpdatedAt(updatedAt);

            // Assert
            assertTrue(userVO.getUpdatedAt().isAfter(userVO.getCreatedAt()) || 
                      userVO.getUpdatedAt().isEqual(userVO.getCreatedAt()));
        }
    }

    @Nested
    @DisplayName("对象完整性测试")
    class ObjectIntegrityTests {

        @Test
        @DisplayName("创建完整的用户VO对象")
        void testCompleteUserVOObject() {
            // Arrange
            Long id = 1L;
            String username = "testuser";
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            LocalDateTime updatedAt = LocalDateTime.now();

            // Act
            userVO.setId(id);
            userVO.setUsername(username);
            userVO.setCreatedAt(createdAt);
            userVO.setUpdatedAt(updatedAt);

            // Assert
            assertEquals(id, userVO.getId());
            assertEquals(username, userVO.getUsername());
            assertEquals(createdAt, userVO.getCreatedAt());
            assertEquals(updatedAt, userVO.getUpdatedAt());
        }

        @Test
        @DisplayName("新创建的对象所有字段为null")
        void testNewObjectHasNullFields() {
            // Arrange
            UserVO newUserVO = new UserVO();

            // Assert
            assertNull(newUserVO.getId());
            assertNull(newUserVO.getUsername());
            assertNull(newUserVO.getCreatedAt());
            assertNull(newUserVO.getUpdatedAt());
        }

        @Test
        @DisplayName("测试对象字段独立性")
        void testFieldIndependence() {
            // Arrange
            Long id1 = 1L;
            Long id2 = 2L;
            String username1 = "user1";
            String username2 = "user2";

            // Act & Assert
            userVO.setId(id1);
            userVO.setUsername(username1);
            assertEquals(id1, userVO.getId());
            assertEquals(username1, userVO.getUsername());

            userVO.setId(id2);
            assertEquals(id2, userVO.getId());
            assertEquals(username1, userVO.getUsername()); // username should remain unchanged

            userVO.setUsername(username2);
            assertEquals(id2, userVO.getId()); // id should remain unchanged
            assertEquals(username2, userVO.getUsername());
        }
    }

    @Nested
    @DisplayName("业务场景测试")
    class BusinessScenarioTests {

        @Test
        @DisplayName("API响应中的用户信息场景")
        void testApiResponseUserInfoScenario() {
            // Arrange - 模拟从数据库查询到的用户信息
            Long userId = 123L;
            String username = "apiuser";
            LocalDateTime createdTime = LocalDateTime.now().minusMonths(3);
            LocalDateTime lastModified = LocalDateTime.now().minusDays(5);

            // Act - 构建要返回给前端的用户信息
            userVO.setId(userId);
            userVO.setUsername(username);
            userVO.setCreatedAt(createdTime);
            userVO.setUpdatedAt(lastModified);

            // Assert - 验证用户信息正确且不包含敏感信息（如密码、token等）
            assertNotNull(userVO.getId());
            assertNotNull(userVO.getUsername());
            assertNotNull(userVO.getCreatedAt());
            assertNotNull(userVO.getUpdatedAt());
            
            // 验证这是一个用于API响应的VO对象，不应包含敏感信息
            // 注意：UserVO确实不包含password和token字段，这是正确的设计
            assertEquals(userId, userVO.getId());
            assertEquals(username, userVO.getUsername());
            assertTrue(userVO.getCreatedAt().isBefore(LocalDateTime.now()));
            assertTrue(userVO.getUpdatedAt().isAfter(userVO.getCreatedAt()) || 
                      userVO.getUpdatedAt().isEqual(userVO.getCreatedAt()));
        }

        @Test
        @DisplayName("用户列表显示场景")
        void testUserListDisplayScenario() {
            // Arrange - 模拟用户列表中的一条记录
            Long userId = 456L;
            String displayName = "ListUser";
            LocalDateTime registrationTime = LocalDateTime.of(2023, 1, 15, 9, 30);
            LocalDateTime profileUpdateTime = LocalDateTime.of(2023, 6, 20, 16, 45);

            // Act
            userVO.setId(userId);
            userVO.setUsername(displayName);
            userVO.setCreatedAt(registrationTime);
            userVO.setUpdatedAt(profileUpdateTime);

            // Assert
            assertEquals(userId, userVO.getId());
            assertEquals(displayName, userVO.getUsername());
            assertEquals(registrationTime, userVO.getCreatedAt());
            assertEquals(profileUpdateTime, userVO.getUpdatedAt());
            
            // 验证时间逻辑合理
            assertTrue(userVO.getUpdatedAt().isAfter(userVO.getCreatedAt()));
        }

        @Test
        @DisplayName("最近注册用户场景")
        void testRecentRegisteredUserScenario() {
            // Arrange - 模拟最近注册的用户
            Long newUserId = 999L;
            String newUsername = "newuser";
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(5);

            // Act - 新用户的创建时间和更新时间可能相同
            userVO.setId(newUserId);
            userVO.setUsername(newUsername);
            userVO.setCreatedAt(recentTime);
            userVO.setUpdatedAt(recentTime); // 新用户创建时间和更新时间相同

            // Assert
            assertEquals(newUserId, userVO.getId());
            assertEquals(newUsername, userVO.getUsername());
            assertEquals(recentTime, userVO.getCreatedAt());
            assertEquals(recentTime, userVO.getUpdatedAt());
            assertEquals(userVO.getCreatedAt(), userVO.getUpdatedAt()); // 对于新用户，这两个时间应该相同
        }
    }
}