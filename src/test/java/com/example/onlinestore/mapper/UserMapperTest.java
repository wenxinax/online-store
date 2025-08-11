package com.example.onlinestore.mapper;

import com.example.onlinestore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("用户数据访问层测试")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private User testUser;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setToken("test-token-123");
        testUser.setTokenExpireTime(now.plusDays(1));
        testUser.setCreatedAt(now);
        testUser.setUpdatedAt(now);
    }

    @Nested
    @DisplayName("用户查询操作")
    class UserQueryTests {

        @Test
        @DisplayName("根据用户名查找用户 - 存在的用户")
        void whenFindByExistingUsername_thenReturnUser() {
            // Given - 先插入一个用户
            userMapper.insertUser(testUser);

            // When
            User found = userMapper.findByUsername("testuser");

            // Then
            assertNotNull(found);
            assertEquals("testuser", found.getUsername());
            assertEquals("test-token-123", found.getToken());
            assertNotNull(found.getId());
        }

        @Test
        @DisplayName("根据用户名查找用户 - 不存在的用户")
        void whenFindByNonExistingUsername_thenReturnNull() {
            // When
            User found = userMapper.findByUsername("nonexistent");

            // Then
            assertNull(found);
        }

        @Test
        @DisplayName("查找所有用户")
        void whenFindAll_thenReturnAllUsers() {
            // Given - 插入多个用户
            userMapper.insertUser(testUser);

            User user2 = new User();
            user2.setUsername("testuser2");
            user2.setToken("test-token-456");
            user2.setTokenExpireTime(now.plusDays(1));
            user2.setCreatedAt(now);
            user2.setUpdatedAt(now);
            userMapper.insertUser(user2);

            // When
            List<User> users = userMapper.findAll();

            // Then
            assertNotNull(users);
            assertEquals(2, users.size());
        }

        @Test
        @DisplayName("分页查询用户")
        void whenFindAllWithPagination_thenReturnPagedResults() {
            // Given - 插入多个用户
            for (int i = 1; i <= 5; i++) {
                User user = new User();
                user.setUsername("user" + i);
                user.setToken("token-" + i);
                user.setTokenExpireTime(now.plusDays(1));
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                userMapper.insertUser(user);
            }

            // When - 查询第一页，每页3条
            List<User> firstPage = userMapper.findAllWithPagination(0, 3);

            // Then
            assertNotNull(firstPage);
            assertEquals(3, firstPage.size());

            // When - 查询第二页，每页3条
            List<User> secondPage = userMapper.findAllWithPagination(3, 3);

            // Then
            assertNotNull(secondPage);
            assertEquals(2, secondPage.size());
        }

        @Test
        @DisplayName("统计用户总数")
        void whenCountTotal_thenReturnCorrectCount() {
            // Given - 插入测试数据
            userMapper.insertUser(testUser);

            User user2 = new User();
            user2.setUsername("testuser2");
            user2.setToken("test-token-456");
            user2.setTokenExpireTime(now.plusDays(1));
            user2.setCreatedAt(now);
            user2.setUpdatedAt(now);
            userMapper.insertUser(user2);

            // When
            long count = userMapper.countTotal();

            // Then
            assertEquals(2L, count);
        }

        @Test
        @DisplayName("空数据库统计总数")
        void whenCountTotalWithEmptyDatabase_thenReturnZero() {
            // When
            long count = userMapper.countTotal();

            // Then
            assertEquals(0L, count);
        }
    }

    @Nested
    @DisplayName("用户插入操作")
    class UserInsertTests {

        @Test
        @DisplayName("插入新用户成功")
        void whenInsertUser_thenUserIsCreated() {
            // When
            userMapper.insertUser(testUser);

            // Then
            assertNotNull(testUser.getId());
            User found = userMapper.findByUsername("testuser");
            assertNotNull(found);
            assertEquals("testuser", found.getUsername());
        }

        @Test
        @DisplayName("插入用户时自动生成ID")
        void whenInsertUser_thenIdIsGenerated() {
            // Given
            assertNull(testUser.getId());

            // When
            userMapper.insertUser(testUser);

            // Then
            assertNotNull(testUser.getId());
            assertTrue(testUser.getId() > 0);
        }

        @Test
        @DisplayName("插入多个用户")
        void whenInsertMultipleUsers_thenAllAreCreated() {
            // Given
            User user1 = createUser("user1", "token1");
            User user2 = createUser("user2", "token2");
            User user3 = createUser("user3", "token3");

            // When
            userMapper.insertUser(user1);
            userMapper.insertUser(user2);
            userMapper.insertUser(user3);

            // Then
            assertEquals(3L, userMapper.countTotal());
        }
    }

    @Nested
    @DisplayName("用户更新操作")
    class UserUpdateTests {

        @Test
        @DisplayName("更新用户Token成功")
        void whenUpdateUserToken_thenTokenIsUpdated() {
            // Given - 先插入用户
            userMapper.insertUser(testUser);
            Long userId = testUser.getId();
            String newToken = "new-token-123";
            LocalDateTime newExpireTime = now.plusDays(2);

            // When - 更新token
            testUser.setToken(newToken);
            testUser.setTokenExpireTime(newExpireTime);
            testUser.setUpdatedAt(now.plusMinutes(1));
            int affectedRows = userMapper.updateUserToken(testUser);

            // Then
            assertEquals(1, affectedRows);
            User updated = userMapper.findByUsername("testuser");
            assertEquals(newToken, updated.getToken());
            assertEquals(newExpireTime, updated.getTokenExpireTime());
            assertTrue(updated.getUpdatedAt().isAfter(updated.getCreatedAt()));
        }

        @Test
        @DisplayName("更新不存在的用户")
        void whenUpdateNonExistentUser_thenReturnZero() {
            // Given
            testUser.setId(999L); // 不存在的用户ID
            testUser.setToken("new-token");

            // When
            int affectedRows = userMapper.updateUserToken(testUser);

            // Then
            assertEquals(0, affectedRows);
        }

        @Test
        @DisplayName("批量更新多个用户Token")
        void whenUpdateMultipleUserTokens_thenAllAreUpdated() {
            // Given - 插入多个用户
            User user1 = createUser("user1", "token1");
            User user2 = createUser("user2", "token2");
            userMapper.insertUser(user1);
            userMapper.insertUser(user2);

            // When - 更新两个用户的token
            user1.setToken("new-token1");
            user1.setTokenExpireTime(now.plusDays(2));
            user1.setUpdatedAt(now.plusMinutes(1));

            user2.setToken("new-token2");
            user2.setTokenExpireTime(now.plusDays(2));
            user2.setUpdatedAt(now.plusMinutes(1));

            int affected1 = userMapper.updateUserToken(user1);
            int affected2 = userMapper.updateUserToken(user2);

            // Then
            assertEquals(1, affected1);
            assertEquals(1, affected2);

            User updated1 = userMapper.findByUsername("user1");
            User updated2 = userMapper.findByUsername("user2");
            assertEquals("new-token1", updated1.getToken());
            assertEquals("new-token2", updated2.getToken());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("用户名包含特殊字符")
        void whenUsernameContainsSpecialCharacters_thenHandleCorrectly() {
            // Given
            testUser.setUsername("user@test.com");

            // When
            userMapper.insertUser(testUser);

            // Then
            User found = userMapper.findByUsername("user@test.com");
            assertNotNull(found);
            assertEquals("user@test.com", found.getUsername());
        }

        @Test
        @DisplayName("极长的用户名")
        void whenUsernameIsVeryLong_thenHandleCorrectly() {
            // Given - 创建一个很长的用户名（但在数据库限制内）
            String longUsername = "a".repeat(50); // 假设用户名最大50字符
            testUser.setUsername(longUsername);

            // When
            userMapper.insertUser(testUser);

            // Then
            User found = userMapper.findByUsername(longUsername);
            assertNotNull(found);
            assertEquals(longUsername, found.getUsername());
        }

        @Test
        @DisplayName("Token为null的情况")
        void whenTokenIsNull_thenHandleGracefully() {
            // Given
            testUser.setToken(null);

            // When
            userMapper.insertUser(testUser);

            // Then
            User found = userMapper.findByUsername("testuser");
            assertNotNull(found);
            assertNull(found.getToken());
        }

        @Test
        @DisplayName("过期时间为null的情况")
        void whenTokenExpireTimeIsNull_thenHandleGracefully() {
            // Given
            testUser.setTokenExpireTime(null);

            // When
            userMapper.insertUser(testUser);

            // Then
            User found = userMapper.findByUsername("testuser");
            assertNotNull(found);
            assertNull(found.getTokenExpireTime());
        }

        @Test
        @DisplayName("分页查询越界")
        void whenPaginationExceedsTotalRecords_thenReturnEmptyList() {
            // Given - 只插入1个用户
            userMapper.insertUser(testUser);

            // When - 查询第10页
            List<User> results = userMapper.findAllWithPagination(100, 10);

            // Then
            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("分页大小为0")
        void whenPageSizeIsZero_thenReturnEmptyList() {
            // Given
            userMapper.insertUser(testUser);

            // When
            List<User> results = userMapper.findAllWithPagination(0, 0);

            // Then
            assertNotNull(results);
            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("数据完整性测试")
    class DataIntegrityTests {

        @Test
        @DisplayName("验证创建和更新时间的设置")
        void whenInsertUser_thenTimestampsAreSetCorrectly() {
            // Given
            LocalDateTime beforeInsert = LocalDateTime.now();

            // When
            userMapper.insertUser(testUser);

            // Then
            User found = userMapper.findByUsername("testuser");
            assertNotNull(found.getCreatedAt());
            assertNotNull(found.getUpdatedAt());
            assertTrue(found.getCreatedAt().isAfter(beforeInsert.minusSeconds(1)));
            assertTrue(found.getUpdatedAt().isAfter(beforeInsert.minusSeconds(1)));
        }

        @Test
        @DisplayName("更新时只更新UpdatedAt字段")
        void whenUpdateUser_thenOnlyUpdatedAtChanges() {
            // Given - 插入用户
            userMapper.insertUser(testUser);
            LocalDateTime originalCreatedAt = testUser.getCreatedAt();
            
            // Wait a moment to ensure timestamp difference
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // When - 更新用户
            testUser.setToken("updated-token");
            testUser.setUpdatedAt(LocalDateTime.now());
            userMapper.updateUserToken(testUser);

            // Then
            User updated = userMapper.findByUsername("testuser");
            assertEquals(originalCreatedAt, updated.getCreatedAt());
            assertTrue(updated.getUpdatedAt().isAfter(originalCreatedAt));
        }
    }

    // 辅助方法
    private User createUser(String username, String token) {
        User user = new User();
        user.setUsername(username);
        user.setToken(token);
        user.setTokenExpireTime(now.plusDays(1));
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return user;
    }
}