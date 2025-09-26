package com.example.onlinestore.service.testutil;

import com.example.onlinestore.model.User;

import java.time.LocalDateTime;

/**
 * 用户测试数据构建器
 */
public class UserTestDataBuilder {
    
    private Long id = 1L;
    private String username = "testuser";
    private String token = "test-token-123";
    private LocalDateTime tokenExpireTime = LocalDateTime.now().plusDays(1);
    private LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
    private LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

    public static UserTestDataBuilder builder() {
        return new UserTestDataBuilder();
    }

    public UserTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public UserTestDataBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public UserTestDataBuilder withToken(String token) {
        this.token = token;
        return this;
    }

    public UserTestDataBuilder withTokenExpireTime(LocalDateTime tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
        return this;
    }

    public UserTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public UserTestDataBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public User build() {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setToken(token);
        user.setTokenExpireTime(tokenExpireTime);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);
        return user;
    }
}